# Протоколы Proxiya

Эта папка содержит все protobuf протоколы для проекта Proxiya.

## Структура

- `data/` - протоколы для канала данных (HTTP запросы/ответы)
- `control/` - протоколы для канала управления (создание, запуск, остановка прокси)

## Правила именования

- Протоколы в единственном числе: `Request`, `Response`, `ProxyStatus`
- Поля в snake_case: `proxy_id`, `request_url`
- Сервисы с суффиксом `Service`: `ProxyDataService`, `ProxyControlService`

## gRPC API Архитектура

### Канал данных (ProxyDataService)
Используется для передачи перехваченных HTTP запросов и ответов от mitmproxy аддона в контроллер.

**Основные методы:**
- `SendRequest(HttpRequest) -> Empty` - отправка перехваченного запроса
- `SendResponse(HttpResponse) -> Empty` - отправка перехваченного ответа
- `StreamRequests(stream HttpRequest) -> Empty` - потоковая передача запросов
- `StreamResponses(stream HttpResponse) -> Empty` - потоковая передача ответов

### Канал управления - UI (ProxyManagementService)
Используется для управления жизненным циклом прокси из UI и внешних клиентов.

**Основные методы:**
- `CreateProxy(ProxyConfig) -> ProxyInfo` - создание нового прокси
- `StartProxy(StartProxyRequest) -> ProxyInfo` - запуск прокси
- `StopProxy(StopProxyRequest) -> ProxyInfo` - остановка прокси
- `GetProxy(GetProxyRequest) -> ProxyInfo` - получение информации о прокси
- `ListProxies(ListProxiesRequest) -> ListProxiesResponse` - список всех прокси
- `GetControllerStatus(GetControllerStatusRequest) -> ControllerStatus` - статус контроллера
- `DeleteProxy(DeleteProxyRequest) -> Empty` - удаление прокси

### Канал управления - mitmproxy (ProxyNotificationService)
Используется для уведомлений от mitmproxy аддонов о состоянии прокси.

**Основные методы:**
- `NotifyProxyStarted(NotifyProxyStartedRequest) -> Empty` - уведомление о запуске
- `NotifyProxyStopped(NotifyProxyStoppedRequest) -> Empty` - уведомление об остановке
- `NotifyProxyError(NotifyProxyErrorRequest) -> Empty` - уведомление об ошибке
- `Heartbeat(HeartbeatRequest) -> Empty` - периодическое уведомление о жизни прокси

## Поток данных

1. **Создание прокси**: UI -> Controller (ProxyManagementService.CreateProxy)
2. **Запуск прокси**: Controller -> mitmproxy (запуск процесса)
3. **Уведомление о запуске**: mitmproxy -> Controller (ProxyNotificationService.NotifyProxyStarted)
4. **Перехват трафика**: mitmproxy -> Controller (ProxyDataService.SendRequest/SendResponse)
5. **Heartbeat**: mitmproxy -> Controller (ProxyNotificationService.Heartbeat)
6. **Остановка прокси**: UI -> Controller (ProxyManagementService.StopProxy)
7. **Уведомление об остановке**: mitmproxy -> Controller (ProxyNotificationService.NotifyProxyStopped)

## Асинхронность

Все gRPC методы спроектированы для асинхронной работы:
- Python аддон использует async/await для отправки данных
- Контроллер обрабатывает запросы асинхронно
- UI может подписываться на обновления статуса через streaming

## Безопасность

- gRPC соединения могут использовать TLS (настраивается в GrpcConfig)
- Каждый прокси имеет уникальный ID для изоляции данных
- Контроллер валидирует все входящие запросы