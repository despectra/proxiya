# Proxiya

Современный UI-клиент для прокси-сниффера mitmproxy. Позволяет запускать процессы mitmproxy и работать с трафиком устройств и приложений. Аналог Proxyman, Charles, Fiddler.

## Архитектура проекта

- **composeApp** - UI часть приложения (Compose Multiplatform)
- **controller** - контроллер для управления прокси (JVM-only)
- **protocols** - общие protobuf протоколы и gRPC API
- **mitmproxy_addon** - Python аддон для mitmproxy

### Модули

* [/composeApp](./composeApp/src) - UI приложение на Compose Multiplatform
    - [commonMain](./composeApp/src/commonMain/kotlin) - общий код для всех платформ
    - [jvmMain](./composeApp/src/jvmMain/kotlin) - JVM-специфичный код

* [/controller](./controller/src) - контроллер прокси на Kotlin
* [/protocols](./protocols/src) - protobuf протоколы и gRPC API
* [/mitmproxy_addon](./mitmproxy_addon/) - Python аддон для mitmproxy

## Сборка и запуск

### UI приложение

```shell
# macOS/Linux
./gradlew :composeApp:run

# Windows
.\gradlew.bat :composeApp:run
```

### Контроллер

```shell
./gradlew :controller:fatJar
java -jar controller/build/libs/proxiya-controller.jar
```

Или используйте скрипт:
```shell
./run-controller.sh
```

### mitmproxy аддон

```shell
cd mitmproxy_addon
./setup_environment.sh
source venv/bin/activate
mitmdump -s proxiya_addon.py --set proxiya_proxy_id=test
```

## gRPC API

Система использует gRPC для связи между компонентами:

- **ProxyControlService** - управление прокси (UI ↔ Controller)
- **ProxyDataService** - стриминг трафика (Controller → UI)
- **ProxyInstanceControlService** - уведомления (mitmproxy → Controller)
- **ProxyInstanceDataService** - передача трафика (mitmproxy → Controller)

Подробнее в [protocols/README.md](./protocols/src/main/proto/README.md)

## Документация

- [Общее описание проекта](./docs/overview.md)
- [Текущая работа MVP](./docs/current-work-mvp.md)
- [Протоколы и API](./protocols/src/main/proto/README.md)
- [Контроллер](./controller/README.md)
- [mitmproxy аддон](./mitmproxy_addon/README.md)

