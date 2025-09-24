# Proxiya mitmproxy Addon

Python-аддон для mitmproxy, который перехватывает HTTP трафик и отправляет его в контроллер Proxiya через gRPC API.

## Установка

### Автоматическая установка (рекомендуется)

Запустите скрипт автоматической настройки:

```bash
./setup_environment.sh
```

Этот скрипт:
- Создаст виртуальное окружение
- Установит все необходимые зависимости
- Сгенерирует gRPC стабы

### Ручная установка

1. Создайте виртуальное окружение:
```bash
python3 -m venv venv
source venv/bin/activate
```

2. Установите зависимости:
```bash
pip install -r requirements.txt
```

3. Сгенерируйте gRPC стабы из proto файлов:
```bash
python3 generate_grpc_stubs.py
```

## Использование

### Базовое использование

Запустите mitmproxy с аддоном:

```bash
mitmdump -s proxiya_addon.py --set proxiya_proxy_id=my_proxy --set proxiya_controller_host=localhost --set proxiya_controller_port=50051
```

### Переменные окружения

Аддон поддерживает следующие переменные окружения:

- `PROXIYA_PROXY_ID` - ID прокси (по умолчанию: "default")
- `PROXIYA_CONTROLLER_HOST` - хост контроллера (по умолчанию: "localhost")
- `PROXIYA_CONTROLLER_PORT` - порт контроллера (по умолчанию: 50051)
- `PROXIYA_USE_TLS` - использовать TLS (по умолчанию: false)
- `PROXIYA_HEARTBEAT_INTERVAL` - интервал heartbeat в секундах (по умолчанию: 30)

### Пример запуска с переменными окружения

```bash
export PROXIYA_PROXY_ID=test_proxy
export PROXIYA_CONTROLLER_HOST=192.168.1.100
export PROXIYA_CONTROLLER_PORT=50051

mitmdump -s proxiya_addon.py
```

### Параметры mitmproxy

Аддон добавляет следующие параметры в mitmproxy:

- `--set proxiya_proxy_id=ID` - ID прокси
- `--set proxiya_controller_host=HOST` - хост контроллера
- `--set proxiya_controller_port=PORT` - порт контроллера
- `--set proxiya_use_tls=true` - использовать TLS

## Функциональность

### Перехват трафика

Аддон автоматически перехватывает:
- HTTP запросы (метод, URL, заголовки, тело)
- HTTP ответы (статус код, заголовки, тело)
- Информацию о клиенте (IP, порт, User-Agent)

### Уведомления контроллера

Аддон отправляет в контроллер:
- Уведомление о запуске прокси
- Уведомление об остановке прокси
- Периодические heartbeat с статистикой
- Уведомления об ошибках

### Статистика

Аддон ведет статистику:
- Количество обработанных запросов
- Количество обработанных ответов
- Время последней активности
- Использование памяти (опционально)

## Архитектура

```
┌─────────────────┐    gRPC     ┌─────────────────┐
│   mitmproxy     │ ──────────► │   Controller    │
│   + Addon       │             │   (Kotlin)      │
└─────────────────┘             └─────────────────┘
```

### Каналы связи

1. **ProxyInstanceDataService** - передача перехваченных HTTP данных
   - `OnInterceptRequest(InterceptedRequestNotification) -> RequestProcessingResult` - перехват запроса с ожиданием результата обработки
   - `OnInterceptResponse(InterceptedResponseNotification) -> ResponseProcessingResult` - перехват ответа с ожиданием результата обработки

2. **ProxyInstanceControlService** - управление жизненным циклом
   - `NotifyProxyStarted(ProxyStartedNotification) -> Empty` - уведомление о запуске
   - `NotifyProxyStopped(ProxyStoppedNotification) -> Empty` - уведомление об остановке
   - `NotifyProxyError(ProxyErrorNotification) -> Empty` - уведомления об ошибках
   - `Heartbeat(HeartbeatNotification) -> Empty` - периодические heartbeat с статистикой

## Разработка

### Структура файлов

```
mitmproxy_addon/
├── proxiya_addon.py          # Основной аддон
├── generate_grpc_stubs.py    # Генератор gRPC стабов
├── requirements.txt          # Зависимости
├── README.md                # Документация
├── setup_environment.sh     # Скрипт настройки окружения
├── run_proxy.sh             # Скрипт запуска прокси
└── generated/               # Сгенерированные gRPC стабы
    ├── __init__.py
    ├── common_types_pb2.py
    ├── proxiya_pb2.py
    ├── proxiya_pb2_grpc.py
    ├── control/
    │   ├── proxy_control_pb2.py
    │   ├── proxy_control_pb2_grpc.py
    │   ├── proxy_instance_control_service_pb2.py
    │   └── proxy_instance_control_service_pb2_grpc.py
    └── data/
        ├── http_data_pb2.py
        ├── interception_data_pb2.py
        ├── proxy_data_service_pb2.py
        ├── proxy_data_service_pb2_grpc.py
        ├── proxy_instance_data_service_pb2.py
        └── proxy_instance_data_service_pb2_grpc.py
```

### Логирование

Аддон использует стандартный Python logging. Уровень логирования можно настроить через переменную окружения:

```bash
export PYTHONPATH=.
export LOG_LEVEL=DEBUG
mitmdump -s proxiya_addon.py
```

### Тестирование

Для тестирования аддона можно использовать mock-контроллер или запустить реальный контроллер Proxiya.

## Устранение неполадок

### Проблемы с gRPC

1. Убедитесь, что gRPC стабы сгенерированы:
```bash
python generate_grpc_stubs.py
```

2. Проверьте подключение к контроллеру:
```bash
telnet localhost 50051
```

### Проблемы с mitmproxy

1. Проверьте версию mitmproxy:
```bash
mitmdump --version
```

2. Убедитесь, что аддон загружается:
```bash
mitmdump -s proxiya_addon.py --set confdir=~/.mitmproxy
```

### Логи

Аддон выводит подробные логи. Для отладки используйте уровень DEBUG:

```bash
export LOG_LEVEL=DEBUG
mitmdump -s proxiya_addon.py
```
