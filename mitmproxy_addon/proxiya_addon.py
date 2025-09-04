#!/usr/bin/env python3
"""
Proxiya mitmproxy addon

Этот аддон перехватывает HTTP трафик и отправляет его в контроллер Proxiya
через gRPC API.
"""

import asyncio
import json
import logging
import os
import sys
from typing import Optional, Dict, Any
from datetime import datetime

import grpc
from mitmproxy import http, ctx

# Импорты для gRPC (будут сгенерированы из proto файлов)
try:
    from proxiya_pb2 import (
        HttpRequest, HttpResponse, ClientInfo,
        NotifyProxyStartedRequest, NotifyProxyStoppedRequest,
        HeartbeatRequest, ProxyStats, Empty
    )
    from proxiya_pb2_grpc import (
        ProxyDataServiceStub, ProxyNotificationServiceStub
    )
except ImportError:
    # Fallback для разработки - создадим заглушки
    print("Warning: gRPC stubs not found. Using mock implementations.")
    
    class HttpRequest:
        def __init__(self, **kwargs):
            for k, v in kwargs.items():
                setattr(self, k, v)
    
    class HttpResponse:
        def __init__(self, **kwargs):
            for k, v in kwargs.items():
                setattr(self, k, v)
    
    class ClientInfo:
        def __init__(self, **kwargs):
            for k, v in kwargs.items():
                setattr(self, k, v)
    
    class NotifyProxyStartedRequest:
        def __init__(self, **kwargs):
            for k, v in kwargs.items():
                setattr(self, k, v)
    
    class NotifyProxyStoppedRequest:
        def __init__(self, **kwargs):
            for k, v in kwargs.items():
                setattr(self, k, v)
    
    class HeartbeatRequest:
        def __init__(self, **kwargs):
            for k, v in kwargs.items():
                setattr(self, k, v)
    
    class ProxyStats:
        def __init__(self, **kwargs):
            for k, v in kwargs.items():
                setattr(self, k, v)
    
    class Empty:
        pass
    
    class ProxyDataServiceStub:
        def __init__(self, channel):
            pass
        
        def SendRequest(self, request):
            return Empty()
        
        def SendResponse(self, request):
            return Empty()
    
    class ProxyNotificationServiceStub:
        def __init__(self, channel):
            pass
        
        def NotifyProxyStarted(self, request):
            return Empty()
        
        def NotifyProxyStopped(self, request):
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
        self.heartbeat_interval: int = 30  # секунды
        
        # gRPC соединения
        self.data_channel: Optional[grpc.aio.Channel] = None
        self.notification_channel: Optional[grpc.aio.Channel] = None
        self.data_stub: Optional[ProxyDataServiceStub] = None
        self.notification_stub: Optional[ProxyNotificationServiceStub] = None
        
        # Статистика
        self.stats = {
            'requests_processed': 0,
            'responses_processed': 0,
            'last_activity': 0
        }
        
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
        
        # Загружаем конфигурацию из переменных окружения
        self._load_config()
        
        # Регистрируем команды
        loader.add_option(
            name="proxiya_proxy_id",
            typespec=str,
            default=self.proxy_id or "default",
            help="ID прокси для идентификации в контроллере"
        )
        loader.add_option(
            name="proxiya_controller_host",
            typespec=str,
            default=self.controller_host,
            help="Хост контроллера Proxiya"
        )
        loader.add_option(
            name="proxiya_controller_port",
            typespec=int,
            default=self.controller_port,
            help="Порт контроллера Proxiya"
        )
        loader.add_option(
            name="proxiya_use_tls",
            typespec=bool,
            default=self.use_tls,
            help="Использовать TLS для подключения к контроллеру"
        )
    
    def _load_config(self):
        """Загрузка конфигурации из переменных окружения"""
        self.proxy_id = os.getenv('PROXIYA_PROXY_ID', 'default')
        self.controller_host = os.getenv('PROXIYA_CONTROLLER_HOST', 'localhost')
        self.controller_port = int(os.getenv('PROXIYA_CONTROLLER_PORT', '50051'))
        self.use_tls = os.getenv('PROXIYA_USE_TLS', 'false').lower() == 'true'
        self.heartbeat_interval = int(os.getenv('PROXIYA_HEARTBEAT_INTERVAL', '30'))
        
        self.logger.info(f"Configuration loaded: proxy_id={self.proxy_id}, "
                        f"controller={self.controller_host}:{self.controller_port}, "
                        f"tls={self.use_tls}")
    
    async def _connect_to_controller(self):
        """Подключение к контроллеру через gRPC"""
        try:
            # Создаем каналы
            target = f"{self.controller_host}:{self.controller_port}"
            
            if self.use_tls:
                # TODO: Добавить поддержку TLS
                credentials = grpc.ssl_channel_credentials()
                self.data_channel = grpc.aio.secure_channel(target, credentials)
                self.notification_channel = grpc.aio.secure_channel(target, credentials)
            else:
                self.data_channel = grpc.aio.insecure_channel(target)
                self.notification_channel = grpc.aio.insecure_channel(target)
            
            # Создаем стабы
            self.data_stub = ProxyDataServiceStub(self.data_channel)
            self.notification_stub = ProxyNotificationServiceStub(self.notification_channel)
            
            self.logger.info(f"Connected to controller at {target}")
            return True
            
        except Exception as e:
            self.logger.error(f"Failed to connect to controller: {e}")
            return False
    
    async def _notify_proxy_started(self):
        """Уведомление контроллера о запуске прокси"""
        if not self.notification_stub:
            return
        
        try:
            request = NotifyProxyStartedRequest(
                proxy_id=self.proxy_id,
                pid=os.getpid(),
                startup_info=f"mitmproxy addon started at {datetime.now().isoformat()}"
            )
            
            await self.notification_stub.NotifyProxyStarted(request)
            self.logger.info(f"Notified controller about proxy start: {self.proxy_id}")
            
        except Exception as e:
            self.logger.error(f"Failed to notify proxy start: {e}")
    
    async def _send_heartbeat(self):
        """Отправка heartbeat в контроллер"""
        if not self.notification_stub:
            return
        
        try:
            stats = ProxyStats(
                requests_processed=self.stats['requests_processed'],
                responses_processed=self.stats['responses_processed'],
                last_activity=self.stats['last_activity']
            )
            
            request = HeartbeatRequest(
                proxy_id=self.proxy_id,
                stats=stats
            )
            
            await self.notification_stub.Heartbeat(request)
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
    
    def running(self):
        """Вызывается когда mitmproxy запущен"""
        self.logger.info("Proxiya addon is running...")
        
        # Запускаем асинхронные задачи
        loop = asyncio.new_event_loop()
        asyncio.set_event_loop(loop)
        
        # Подключаемся к контроллеру
        loop.run_until_complete(self._connect_to_controller())
        
        # Уведомляем о запуске
        loop.run_until_complete(self._notify_proxy_started())
        
        # Запускаем heartbeat
        self.heartbeat_task = loop.create_task(self._heartbeat_loop())
        
        self.logger.info(f"Proxy {self.proxy_id} is ready to intercept traffic")
    
    def done(self):
        """Вызывается при остановке mitmproxy"""
        self.logger.info("Proxiya addon is shutting down...")
        
        # Останавливаем heartbeat
        if self.heartbeat_task:
            self.heartbeat_task.cancel()
        
        # Уведомляем контроллер об остановке
        if self.notification_stub:
            try:
                request = NotifyProxyStoppedRequest(
                    proxy_id=self.proxy_id,
                    exit_code=0,
                    reason="mitmproxy addon shutdown"
                )
                
                loop = asyncio.get_event_loop()
                loop.run_until_complete(
                    self.notification_stub.NotifyProxyStopped(request)
                )
                
            except Exception as e:
                self.logger.error(f"Failed to notify proxy stop: {e}")
        
        # Закрываем соединения
        if self.data_channel:
            loop = asyncio.get_event_loop()
            loop.run_until_complete(self.data_channel.close())
        
        if self.notification_channel:
            loop = asyncio.get_event_loop()
            loop.run_until_complete(self.notification_channel.close())
        
        self.logger.info("Proxiya addon shutdown complete")
    
    def request(self, flow: http.HTTPFlow) -> None:
        """Обработка HTTP запроса"""
        try:
            # Обновляем статистику
            self.stats['requests_processed'] += 1
            self.stats['last_activity'] = int(datetime.now().timestamp() * 1000)
            
            # Создаем протку запроса
            request_data = self._create_http_request(flow)
            
            # Отправляем в контроллер (асинхронно)
            if self.data_stub:
                loop = asyncio.get_event_loop()
                loop.create_task(self._send_request_async(request_data))
            
            self.logger.debug(f"Request intercepted: {flow.request.method} {flow.request.pretty_url}")
            
        except Exception as e:
            self.logger.error(f"Error processing request: {e}")
    
    def response(self, flow: http.HTTPFlow) -> None:
        """Обработка HTTP ответа"""
        try:
            # Обновляем статистику
            self.stats['responses_processed'] += 1
            self.stats['last_activity'] = int(datetime.now().timestamp() * 1000)
            
            # Создаем протку ответа
            response_data = self._create_http_response(flow)
            
            # Отправляем в контроллер (асинхронно)
            if self.data_stub:
                loop = asyncio.get_event_loop()
                loop.create_task(self._send_response_async(response_data))
            
            self.logger.debug(f"Response intercepted: {flow.response.status_code} for {flow.request.pretty_url}")
            
        except Exception as e:
            self.logger.error(f"Error processing response: {e}")
    
    def _create_http_request(self, flow: http.HTTPFlow) -> HttpRequest:
        """Создание протки HTTP запроса"""
        # Получаем информацию о клиенте
        client_info = None
        if flow.client_conn:
            client_info = ClientInfo(
                client_ip=flow.client_conn.address[0] if flow.client_conn.address else "unknown",
                client_port=flow.client_conn.address[1] if flow.client_conn.address else 0,
                user_agent=flow.request.headers.get('User-Agent')
            )
        
        # Создаем запрос
        request = HttpRequest(
            proxy_id=self.proxy_id,
            url=flow.request.pretty_url,
            method=flow.request.method,
            headers=dict(flow.request.headers),
            body=flow.request.content if flow.request.content else b'',
            timestamp=int(datetime.now().timestamp() * 1000),
            client_info=client_info
        )
        
        return request
    
    def _create_http_response(self, flow: http.HTTPFlow) -> HttpResponse:
        """Создание протки HTTP ответа"""
        response = HttpResponse(
            proxy_id=self.proxy_id,
            status_code=flow.response.status_code,
            headers=dict(flow.response.headers),
            body=flow.response.content if flow.response.content else b'',
            timestamp=int(datetime.now().timestamp() * 1000),
            request_id=None  # TODO: Добавить связывание с запросом
        )
        
        return response
    
    async def _send_request_async(self, request: HttpRequest):
        """Асинхронная отправка запроса в контроллер"""
        try:
            await self.data_stub.SendRequest(request)
        except Exception as e:
            self.logger.error(f"Failed to send request to controller: {e}")
    
    async def _send_response_async(self, response: HttpResponse):
        """Асинхронная отправка ответа в контроллер"""
        try:
            await self.data_stub.SendResponse(response)
        except Exception as e:
            self.logger.error(f"Failed to send response to controller: {e}")


# Создаем экземпляр аддона
addons = [ProxiyaAddon()]
