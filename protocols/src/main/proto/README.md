# Протоколы Proxiya

Эта папка содержит все protobuf протоколы для проекта Proxiya.

## Структура

- `data/` - протоколы для канала данных (HTTP запросы/ответы, перехват трафика)
- `control/` - протоколы для канала управления (создание, запуск, остановка прокси)
- `common_types.proto` - общие типы данных (Endpoint, OperationResult)
- `errors.proto` - централизованная обработка ошибок
- `proxy.proto` - основные типы прокси (ProxyInstance, ProxyParams, статусы)
- `proxiya.proto` - точка входа, импортирует все протоколы

## Правила именования

- Протоколы в единственном числе: `Request`, `Response`, `ProxyInstance`
- Поля в snake_case: `proxy_id`, `request_url`, `created_at`
- Сервисы с суффиксом `Service`: `ProxyDataService`, `ProxyControlService`
- Enum значения с префиксом типа: `PROXY_TYPE_HTTP`, `ERROR_TYPE_INTERNAL`

## gRPC API Архитектура

### Канал данных

#### ProxyDataService (UI ← Controller)
Стриминг перехваченного трафика из контроллера в UI-приложение.

**Методы:**
- `StreamInterceptedRequests(stream InterceptedRequestNotification) -> stream InterceptedRequestAction`
- `StreamInterceptedResponses(stream InterceptedResponseNotification) -> stream InterceptedResponseAction`

#### ProxyInstanceDataService (mitmproxy → Controller)
Передача перехваченных запросов и ответов от прокси-инстанса в контроллер.

**Методы:**
- `OnInterceptRequest(InterceptedRequestNotification) -> RequestProcessingResult`
- `OnInterceptResponse(InterceptedResponseNotification) -> ResponseProcessingResult`

### Канал управления

#### ProxyControlService (UI ↔ Controller)
Управление жизненным циклом прокси из UI-приложения.

**Основные методы:**
- `CreateProxy(CreateProxyRequest) -> CreateProxyResult` - создание нового прокси
- `UpdateProxy(UpdateProxyRequest) -> UpdateProxyResult` - редактирование параметров
- `StartProxy(StartProxyRequest) -> StartProxyResult` - запуск прокси
- `StopProxy(StopProxyRequest) -> StopProxyResult` - остановка прокси
- `DeleteProxy(DeleteProxyRequest) -> DeleteProxyResult` - удаление прокси

**Стриминг методы:**
- `StreamProxies(StreamProxiesRequest) -> stream ProxiesList` - список прокси в реальном времени
- `StreamControllerStatus(StreamControllerStatusRequest) -> stream ControllerStatus` - статус контроллера

#### ProxyInstanceControlService (mitmproxy → Controller)
Уведомления от прокси-инстансов о состоянии и событиях.

**Методы:**
- `NotifyProxyStarted(ProxyStartedNotification) -> Empty` - уведомление о запуске
- `NotifyProxyStopped(ProxyStoppedNotification) -> Empty` - уведомление об остановке
- `NotifyProxyError(ProxyErrorNotification) -> Empty` - уведомление об ошибке
- `Heartbeat(HeartbeatNotification) -> Empty` - периодические heartbeat

## Поток данных

### Управление прокси
1. **Создание**: UI → `ProxyControlService.CreateProxy()` → Controller
2. **Запуск**: Controller → mitmproxy process (запуск)
3. **Уведомление о запуске**: mitmproxy → `ProxyInstanceControlService.NotifyProxyStarted()` → Controller
4. **Обновление UI**: Controller → `ProxyControlService.StreamProxies()` → UI
5. **Heartbeat**: mitmproxy → `ProxyInstanceControlService.Heartbeat()` → Controller
6. **Остановка**: UI → `ProxyControlService.StopProxy()` → Controller
7. **Уведомление об остановке**: mitmproxy → `ProxyInstanceControlService.NotifyProxyStopped()` → Controller

### Обработка трафика
1. **Перехват запроса**: mitmproxy → `ProxyInstanceDataService.OnInterceptRequest()` → Controller
2. **Стриминг в UI**: Controller → `ProxyDataService.StreamInterceptedRequests()` → UI
3. **Обработка в UI**: UI → `ProxyDataService` (response) → Controller
4. **Результат обработки**: Controller → mitmproxy (processing result)
5. **Перехват ответа**: mitmproxy → `ProxyInstanceDataService.OnInterceptResponse()` → Controller
6. **Стриминг в UI**: Controller → `ProxyDataService.StreamInterceptedResponses()` → UI

## Типы данных

### Основные сущности

- **ProxyInstance** - экземпляр прокси-сервера с параметрами, статусом и метаданными
- **ProxyParams** - параметры конфигурации прокси (тип, порт, фильтры)
- **HttpRequest/HttpResponse** - структурированные HTTP данные с заголовками и контентом
- **InterceptedRequestNotification/InterceptedResponseNotification** - уведомления о перехваченном трафике

### Статусы и состояния

- **ProxyOperationState** - состояние работы прокси (IDLE, STARTING, ACTIVE, STOPPING, ERROR)
- **ProxyStreamingStatus** - статус стриминга событий
- **ControllerHealth** - общее состояние контроллера (OK, WARNING, CRITICAL)
- **ProcessingKind** - тип обработки трафика (NONE, MODIFY, DROP)

## Расширяемость

### Поддержка новых протоколов
Архитектура готова к поддержке протоколов кроме HTTP через `oneof` поля:

```protobuf
oneof request {
  proxiya.data.http.HttpRequest http_request = 4;
  // Будущие протоколы: gRPC, WebSocket, etc.
}
```

### Типы прокси
Поддерживаются различные типы прокси:
- HTTP proxy
- SOCKS5 proxy  
- Upstream proxy (с настраиваемым destination)
- Reverse proxy (с множественными destinations)
- Transparent proxy

## Асинхронность

Все gRPC методы спроектированы для асинхронной работы:
- Python аддон использует async/await для отправки данных
- Контроллер обрабатывает запросы асинхронно с Kotlin coroutines
- UI подписывается на обновления через streaming API
- Bidirectional streaming для интерактивной обработки трафика

## Безопасность

- gRPC соединения могут использовать TLS
- Каждый прокси имеет уникальный ID для изоляции данных
- Контроллер валидирует все входящие запросы
- Централизованная обработка ошибок через `GenericError`

## Временные метки

Все временные поля используют стандартный тип `google.protobuf.Timestamp`:
- `ProxyInstance.created_at`
- `ProxyInstance.updated_at` 
- `InterceptedRequestNotification.timestamp`
- `InterceptedResponseNotification.timestamp`