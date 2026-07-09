# Proxiya

Proxiya — desktop-приложение для работы с HTTP(S)-трафиком через mitmproxy. Оно объединяет управление локальными прокси и просмотр перехваченных запросов в одном интерфейсе.

Приложение помогает исследовать сетевое взаимодействие мобильных приложений, desktop-клиентов и backend-сервисов: запускать нужную конфигурацию прокси, наблюдать за её состоянием и разбирать проходящий трафик в реальном времени.

## Возможности

- Создание и настройка нескольких прокси.
- Запуск, остановка и удаление прокси из приложения.
- Отображение состояния запущенных процессов.
- Перехват HTTP-запросов и ответов через mitmproxy.
- Потоковое отображение трафика в табличном и waterfall-представлении.
- Настройка портов, режимов прокси и фильтрации хостов.
- Светлая и тёмная темы интерфейса.

## Как это работает

```text
Desktop UI  <-- gRPC -->  Controller  <-- gRPC -->  mitmproxy addon
```

- `composeApp` предоставляет desktop-интерфейс на Compose Multiplatform.
- `controller` управляет конфигурациями и процессами прокси, а также передаёт трафик в UI.
- `mitmproxy_addon` получает HTTP-события от mitmproxy и отправляет их controller.
- `protocols` содержит общие protobuf/gRPC-контракты компонентов.

## Технологии

- Kotlin, Compose Multiplatform и Material 3
- Decompose и Koin
- Kotlin Coroutines и Flow
- gRPC и Protocol Buffers
- Python, asyncio и mitmproxy

## Разработка

Проверка проекта:

```shell
./gradlew check
```

Запуск desktop-приложения:

```shell
./gradlew :composeApp:run
```

Сборка controller:

```shell
./gradlew :controller:fatJar
```

Архитектурные соглашения, команды проверок и правила агентской работы описаны в [`AGENTS.md`](./AGENTS.md).
