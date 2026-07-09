# Repository guidance

## Источник истины

- Актуальное устройство проекта определяется кодом, Gradle-конфигурацией и `.proto`-схемами.
- Документация описывает только подтверждённое текущее поведение и не задаёт roadmap.
- Проект незавершён: существующие `TODO` и неполные ветки не расширяют scope задачи автоматически.
- При изменении пользовательских команд, конфигурации или API обновляй соответствующую часть `README.md`. Не создавай отдельный Markdown-документ без устойчивой необходимости.

## Карта проекта

- `composeApp` — JVM/Desktop-клиент на Compose Multiplatform, Decompose и Koin.
- `controller` — Kotlin/JVM gRPC-сервер, конфигурация и состояние прокси, управление процессами mitmproxy и маршрутизация перехваченного трафика.
- `protocols` — канонические protobuf/gRPC-контракты и генерация JVM/Kotlin-кода.
- `mitmproxy_addon` — Python-аддон на `asyncio` и `grpc.aio`, передающий трафик и lifecycle-события controller.

Несмотря на размещение UI-кода в `commonMain`, сейчас настроен только JVM target. Не вводи платформенные абстракции без отдельной задачи.

## Архитектурные ограничения

- Изменения межкомпонентного API начинай с `protocols/src/main/proto`.
- Не редактируй вручную `protocols/build/generated` и `mitmproxy_addon/generated`.
- Сохраняй разделение control channel и data channel.
- Compose-функции отображают состояние и делегируют действия. Состояние и orchestration размещай в Decompose-компонентах, репозиториях и сервисах.
- Привязывай coroutine scope и освобождение ресурсов к `ComponentContext`, `Closeable` и Koin `onClose`.
- gRPC-реализации controller должны оставаться тонкими адаптерами. Состояние и orchestration принадлежат `ProxyRepository`, `ProxyStore`, `ProxyInterceptionGateway` и `MitmproxyProcessManager`.
- Не блокируй coroutine/Flow streaming paths; блокирующий process/file I/O выноси с UI и gRPC coroutine context.
- Используй существующие `MaterialTheme`, `ProxiyaCustomColors` и наборы `compose-icons`. У интерактивных иконок должен быть осмысленный `contentDescription`.
- Следуй ближайшему существующему коду и именованию пакетов; не создавай новый архитектурный слой ради одной операции.

## Контракты и generated code

При изменении `.proto`:

1. Измени каноническую схему в `protocols/src/main/proto`.
2. Выполни `./gradlew :protocols:generateProto`.
3. Обнови Kotlin producer/consumer с обеих сторон контракта.
4. Перегенерируй Python stubs через `mitmproxy_addon/generate_grpc_stubs.py`, если изменение затрагивает addon.
5. Не маскируй несовместимость ручной правкой generated-файлов.

## Рабочий процесс

- Перед изменениями проверяй `git status --short` и сохраняй пользовательские или посторонние изменения.
- Для поиска сначала используй `rg` и `rg --files`.
- Делай минимальный связный diff в рамках задачи.
- Не добавляй зависимости, не меняй публичный proto-контракт и не расширяй scope без необходимости.
- Не редактируй и не коммить build outputs, generated sources, virtual environments, runtime logs и локальные конфиги.
- Не выполняй stage, commit, push, destructive Git-команды или установку зависимостей без явной необходимости и разрешения.
- Для межмодульных задач можно параллелить независимый read-only анализ и проверки. Один агент владеет интеграцией; несколько агентов не меняют один файл одновременно.

## Проверки

Выбирай минимальный достаточный набор:

- Все Kotlin/proto-модули: `./gradlew check`
- UI: `./gradlew :composeApp:jvmTest`
- Controller: `./gradlew :controller:jvmTest`
- Protocols: `./gradlew :protocols:test`
- Генерация после изменения `.proto`: `./gradlew :protocols:generateProto`
- Controller JAR: `./gradlew :controller:fatJar`
- Python syntax: `PYTHONPYCACHEPREFIX=/tmp/proxiya-pycache python3 -m py_compile mitmproxy_addon/proxiya_addon.py mitmproxy_addon/generate_grpc_stubs.py mitmproxy_addon/test_addon_basic.py`

`mitmproxy_addon/test_addon_basic.py` является ручным integration-сценарием: он требует venv, запускает `mitmdump`, открывает порт и обращается к внешней сети. Не запускай его как обычный unit test.

Сейчас `:controller:jvmTest` обнаруживает 0 тестов из-за отсутствующей настройки JUnit Platform, а `:protocols:test` не имеет test sources. Считай их compile/smoke-проверками, пока test setup не будет исправлен.

В результате работы перечисляй реально выполненные проверки. Успешная сборка при текущем небольшом наборе тестов не доказывает корректность runtime-поведения.

## Runtime-безопасность

- Не запускай UI, controller или mitmproxy addon без необходимости: это долгоживущие процессы, открывающие порты и создающие дочерние процессы.
- Controller пишет состояние в пользовательский каталог приложения, addon создаёт runtime-логи и может обращаться к сети.
- После разрешённого runtime-теста заверши все созданные процессы.
- Не логируй секреты, токены и полные тела перехваченного трафика.
