#!/usr/bin/env python3
"""
Proxiya mitmproxy addon

Данный аддон перехватывает HTTP трафик и отправляет его в контроллер Proxiya
через gRPC API.
Также отправляет служебные уведомления в контроллер - о запуске, остановке, ошибках и выполняет периодические уведомления (heartbeat).
"""

from __future__ import annotations
import asyncio
import logging
import os
import sys
from typing import Optional

# Добавляем текущую директорию в путь для импорта сгенерированных модулей
current_dir = os.path.dirname(os.path.abspath(__file__))
generated_dir = os.path.join(current_dir, 'generated')
if generated_dir not in sys.path:
    sys.path.insert(0, generated_dir)

import grpc
from mitmproxy import http, ctx

GRPC_AVAILABLE = False
# Импорты прото-биндингов
try:
    # Импорты протоколов данных
    from data.http_data_pb2 import HttpRequest, HttpResponse, HttpUrl, HttpContent
    from data.interception_data_pb2 import (
        InterceptedRequestNotification, InterceptedResponseNotification,
        RequestProcessingResult, ResponseProcessingResult
    )
    
    # Импорты протоколов управления
    from control.proxy_instance_control_service_pb2 import (
        ProxyStartedNotification, ProxyStoppedNotification, 
        ProxyErrorNotification, HeartbeatNotification, ProxyStats
    )
    
    # Импорты gRPC сервисов
    from data.proxy_instance_data_service_pb2_grpc import ProxyInstanceDataServiceStub
    from control.proxy_instance_control_service_pb2_grpc import ProxyInstanceControlServiceStub
    
    # Дополнительные импорты
    from google.protobuf.empty_pb2 import Empty
    from google.protobuf.timestamp_pb2 import Timestamp
    
    GRPC_AVAILABLE = True
    
except ImportError as e:
    # Fallback для разработки - создадим заглушки
    print(f"Warning: gRPC stubs not found ({e}). Using mock implementations.")
    
    class HttpRequest:
        def __init__(self, **kwargs):
            for k, v in kwargs.items():
                setattr(self, k, v)
    
    class HttpResponse:
        def __init__(self, **kwargs):
            for k, v in kwargs.items():
                setattr(self, k, v)
    
    class HttpUrl:
        def __init__(self, **kwargs):
            for k, v in kwargs.items():
                setattr(self, k, v)
    
    class HttpContent:
        def __init__(self, **kwargs):
            for k, v in kwargs.items():
                setattr(self, k, v)
    
    class InterceptedRequestNotification:
        def __init__(self, **kwargs):
            for k, v in kwargs.items():
                setattr(self, k, v)
    
    class InterceptedResponseNotification:
        def __init__(self, **kwargs):
            for k, v in kwargs.items():
                setattr(self, k, v)
    
    class ProxyStartedNotification:
        def __init__(self, **kwargs):
            for k, v in kwargs.items():
                setattr(self, k, v)
    
    class ProxyStoppedNotification:
        def __init__(self, **kwargs):
            for k, v in kwargs.items():
                setattr(self, k, v)

    class ProxyErrorNotification:
        def __init__(self, **kwargs):
            for k, v in kwargs.items():
                setattr(self, k, v)
    
    class HeartbeatNotification:
        def __init__(self, **kwargs):
            for k, v in kwargs.items():
                setattr(self, k, v)
    
    class ProxyStats:
        def __init__(self, **kwargs):
            for k, v in kwargs.items():
                setattr(self, k, v)
    
    class RequestProcessingResult:
        def __init__(self, **kwargs):
            for k, v in kwargs.items():
                setattr(self, k, v)
    
    class ResponseProcessingResult:
        def __init__(self, **kwargs):
            for k, v in kwargs.items():
                setattr(self, k, v)
    
    class Empty:
        pass
    
    class Timestamp:
        def __init__(self, **kwargs):
            for k, v in kwargs.items():
                setattr(self, k, v)
    
    class ProxyInstanceDataServiceStub:
        def __init__(self, channel):
            pass
        
        def OnInterceptRequest(self, request):
            return RequestProcessingResult()
        
        def OnInterceptResponse(self, request):
            return ResponseProcessingResult()
    
    class ProxyInstanceControlServiceStub:
        def __init__(self, channel):
            pass
        
        def NotifyProxyStarted(self, request):
            return Empty()
        
        def NotifyProxyStopped(self, request):
            return Empty()

        def NotifyProxyError(self, request):
            return Empty()
        
        def Heartbeat(self, request):
            return Empty()


class ProxiyaAddon:
    """
    Основной класс аддона для mitmproxy
    """
    
    def __init__(self):
        self.proxy_id: Optional[str] = None
        self.controller_host: str = "localhost"
        self.controller_port: int = 50051
        self.use_tls: bool = False
        self.heartbeat_interval: int = 2  # секунды
        
        # gRPC соединения
        self.data_channel: Optional[grpc.aio.Channel] = None
        self.data_stub: Optional[ProxyInstanceDataServiceStub] = None

        self.control_channel: Optional[grpc.aio.Channel] = None
        self.control_stub: Optional[ProxyInstanceControlServiceStub] = None
        
        # Задачи
        self.heartbeat_task: Optional[asyncio.Task] = None
        
        # Настройка логирования
        self._setup_logging()
    
    def _setup_logging(self):
        """Настройка логирования"""
        logging.basicConfig(
            level=logging.INFO,
            format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
        )
        self.logger = logging.getLogger('proxiya_addon')
    
    def load(self, loader):
        """Загрузка аддона - вызывается при старте mitmproxy"""
        self.logger.info("Loading Proxiya addon...")
        self._load_config()
    
    def _load_config(self):
        """Загрузка конфигурации из переменных окружения"""
        self.proxy_id = os.getenv('PROXIYA_PROXY_ID', 'default')
        self.controller_host = os.getenv('PROXIYA_CONTROLLER_HOST', 'localhost')
        self.controller_port = int(os.getenv('PROXIYA_CONTROLLER_PORT', '50051'))
        self.use_tls = os.getenv('PROXIYA_USE_TLS', 'false').lower() == 'true'
        self.heartbeat_interval = int(os.getenv('PROXIYA_HEARTBEAT_INTERVAL', '2'))
        
        self.logger.info(f"Configuration loaded: proxy_id={self.proxy_id}, "
                        f"controller={self.controller_host}:{self.controller_port}, "
                        f"tls={self.use_tls}")
    
    def running(self):
        """Вызывается когда mitmproxy запущен"""
        self.logger.info("Proxiya addon is running...")
        
        # Получаем текущий event loop
        try:
            loop = asyncio.get_running_loop()
        except RuntimeError:
            loop = asyncio.new_event_loop()
            asyncio.set_event_loop(loop)
        
        # Запускаем асинхронные задачи
        loop.create_task(self._initialize_async())
        
        self.logger.info(f"Proxy {self.proxy_id} is initializing...")
    
    async def _connect_to_controller(self):
        """Подключение к контроллеру через gRPC"""
        try:
            # Создаем каналы
            target = f"{self.controller_host}:{self.controller_port}"
            
            if self.use_tls:
                # TODO: Добавить поддержку TLS
                credentials = grpc.ssl_channel_credentials()
                self.data_channel = grpc.aio.secure_channel(target, credentials)
                self.control_channel = grpc.aio.secure_channel(target, credentials)
            else:
                self.data_channel = grpc.aio.insecure_channel(target)
                self.control_channel = grpc.aio.insecure_channel(target)
            
            # Создаем стабы
            self.data_stub = ProxyInstanceDataServiceStub(self.data_channel)
            self.control_stub = ProxyInstanceControlServiceStub(self.control_channel)
            
            self.logger.info(f"Connected to controller at {target}")
            return True
            
        except Exception as e:
            self.logger.error(f"Failed to connect to controller: {e}")
            return False
    
    async def _notify_proxy_started(self):
        """Уведомление контроллера о запуске прокси"""
        if not self.control_stub:
            return
        
        try:
            request = ProxyStartedNotification(
                proxy_id=self.proxy_id,
                process_id=os.getpid()
            )
            
            await self.control_stub.NotifyProxyStarted(request)
            self.logger.info(f"Notified controller about proxy start: {self.proxy_id}")
            
        except Exception as e:
            self.logger.error(f"Failed to notify proxy start: {e}")
    
    async def _send_heartbeat(self):
        """Отправка heartbeat в контроллер"""
        if not self.control_stub:
            return
        
        try:
            stats = ProxyStats(
                memory_usage=0  # TODO: добавить реальные метрики
            )
            
            request = HeartbeatNotification(
                proxy_id=self.proxy_id,
                stats=stats
            )
            
            await self.control_stub.Heartbeat(request)
            self.logger.debug(f"Heartbeat sent for proxy: {self.proxy_id}")
            
        except Exception as e:
            self.logger.error(f"Failed to send heartbeat: {e}")
    
    async def _heartbeat_loop(self):
        """Цикл отправки heartbeat"""
        while True:
            try:
                await self._send_heartbeat()
                await asyncio.sleep(self.heartbeat_interval)
            except asyncio.CancelledError:
                break
            except Exception as e:
                self.logger.error(f"Heartbeat loop error: {e}")
                await asyncio.sleep(self.heartbeat_interval)
    
    async def _initialize_async(self):
        """Асинхронная инициализация аддона"""
        try:
            # Подключаемся к контроллеру
            if await self._connect_to_controller():
                # Уведомляем о запуске
                await self._notify_proxy_started()
                
                # Запускаем heartbeat
                self.heartbeat_task = asyncio.create_task(self._heartbeat_loop())
                
                self.logger.info(f"Proxy {self.proxy_id} is ready to intercept traffic")
            else:
                self.logger.warning("Failed to connect to controller, working in offline mode")
        except Exception as e:
            self.logger.error(f"Failed to initialize async: {e}")
    
    def request(self, flow: http.HTTPFlow) -> None:
        """Обработка HTTP запроса"""
        try:
            # Создаем уведомление о перехваченном запросе
            notification = self._create_intercepted_request(flow)
            
            # Отправляем в контроллер (асинхронно)
            if self.data_stub:
                try:
                    loop = asyncio.get_running_loop()
                    loop.create_task(self._send_request_async(notification))
                except RuntimeError:
                    self.logger.warning("No event loop running, skipping request send")
            
            self.logger.debug(f"Request intercepted: {flow.request.method} {flow.request.pretty_url}")
            
        except Exception as e:
            self.logger.error(f"Error processing request: {e}")
    
    def response(self, flow: http.HTTPFlow) -> None:
        """Обработка HTTP ответа"""
        try:
            # Создаем уведомление о перехваченном ответе
            notification = self._create_intercepted_response(flow)
            
            # Отправляем в контроллер (асинхронно)
            if self.data_stub:
                try:
                    loop = asyncio.get_running_loop()
                    loop.create_task(self._send_response_async(notification))
                except RuntimeError:
                    self.logger.warning("No event loop running, skipping response send")
            
            self.logger.debug(f"Response intercepted: {flow.response.status_code} for {flow.request.pretty_url}")
            
        except Exception as e:
            self.logger.error(f"Error processing response: {e}")
    
    def _create_intercepted_request(self, flow: http.HTTPFlow) -> InterceptedRequestNotification:
        """Создание уведомления о перехваченном HTTP запросе"""
        timestamp = Timestamp()
        timestamp.GetCurrentTime()
        
        # Создаем URL
        http_url = HttpUrl(
            full_url=flow.request.pretty_url,
            scheme=flow.request.scheme,
            host=flow.request.host,
            port=flow.request.port,
            path=flow.request.path
        )
        
        # Создаем контент
        http_content = self._create_http_content_from(flow.request)
        
        # Создаем HTTP запрос
        http_request = HttpRequest(
            url=http_url,
            method=flow.request.method,
            http_version=flow.request.http_version,
            content=http_content
        )
        
        # Добавляем заголовки
        for key, value in flow.request.headers.items():
            http_request.headers[key] = value
        
        request_id = flow.id
        # Создаем уведомление
        notification = InterceptedRequestNotification(
            proxy_id=self.proxy_id,
            request_id=request_id,
            timestamp=timestamp,
            http_request=http_request
        )
        
        return notification
    
    def _create_intercepted_response(self, flow: http.HTTPFlow) -> InterceptedResponseNotification:
        """Создание уведомления о перехваченном HTTP ответе"""
        timestamp = Timestamp()
        timestamp.GetCurrentTime()
        
        # Создаем контент
        http_content = self._create_http_content_from(flow.response)
        
        # Создаем HTTP ответ
        http_response = HttpResponse(
            status_code=flow.response.status_code,
            reason=flow.response.reason,
            http_version=flow.response.http_version,
            content=http_content
        )
        
        # Добавляем заголовки
        for key, value in flow.response.headers.items():
            http_response.headers[key] = value
        
        # Создаем уведомление
        notification = InterceptedResponseNotification(
            proxy_id=self.proxy_id,
            request_id=flow.id,
            timestamp=timestamp,
            http_response=http_response
        )
        
        return notification

    def _create_http_content_from(self, source: http.Request | http.Response) -> HttpContent:
        """Создание объекта HttpContent из mitmproxy flow Request или Response"""
        http_content = HttpContent()
        if source.content:
            http_content.body = source.content
        if 'content-type' in source.headers:
            http_content.content_type = source.headers['content-type']
        if 'content-length' in source.headers:
            try:
                http_content.content_length = int(source.headers['content-length'])
            except ValueError:
                pass
        return http_content
    
    async def _send_request_async(self, notification: InterceptedRequestNotification):
        """Асинхронная отправка перехваченного запроса в контроллер"""
        try:
            result = await self.data_stub.OnInterceptRequest(notification)
            self.logger.debug(f"Request processing result: {result}")
            # TODO: Обработать результат (например, модификация запроса)
        except Exception as e:
            self.logger.error(f"Failed to send request to controller: {e}")
    
    async def _send_response_async(self, notification: InterceptedResponseNotification):
        """Асинхронная отправка перехваченного ответа в контроллер"""
        try:
            result = await self.data_stub.OnInterceptResponse(notification)
            self.logger.debug(f"Response processing result: {result}")
            # TODO: Обработать результат (например, модификация ответа)
        except Exception as e:
            self.logger.error(f"Failed to send response to controller: {e}")

    def done(self):
        """Вызывается при остановке mitmproxy"""
        self.logger.info("Proxiya addon is shutting down...")
        
        # Останавливаем heartbeat
        if self.heartbeat_task:
            self.heartbeat_task.cancel()
        
        # Запускаем асинхронное завершение
        try:
            loop = asyncio.get_running_loop()
            loop.create_task(self._shutdown_async())
        except RuntimeError:
            self.logger.warning("No event loop running during shutdown")
        
        self.logger.info("Proxiya addon shutdown initiated")
    
    async def _shutdown_async(self):
        """Асинхронное завершение работы аддона"""
        try:
            # Уведомляем контроллер об остановке
            if self.control_stub:
                try:
                    request = ProxyStoppedNotification(
                        proxy_id=self.proxy_id,
                        exit_code=0
                    )
                    
                    await self.control_stub.NotifyProxyStopped(request)
                    self.logger.info("Notified controller about proxy stop")
                    
                except Exception as e:
                    self.logger.error(f"Failed to notify proxy stop: {e}")
            
            # Закрываем соединения
            if self.data_channel:
                await self.data_channel.close()
            
            if self.control_channel:
                await self.control_channel.close()
            
            self.logger.info("Proxiya addon shutdown complete")
            
        except Exception as e:
            self.logger.error(f"Error during async shutdown: {e}")

# Создаем экземпляр аддона
addons = [ProxiyaAddon()]
