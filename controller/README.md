# Proxiya Controller

Контроллер для управления HTTP прокси в системе Proxiya.

## Сборка

Для сборки исполняемого JAR файла:

```bash
./gradlew :controller:fatJar
```

Или используйте алиас:

```bash
./gradlew :controller:buildJar
```

## Запуск

### Прямой запуск JAR

```bash
java -jar controller/build/libs/proxiya-controller.jar [options]
```

### Использование скрипта

```bash
./run-controller.sh [options]
```

## Параметры командной строки

- `-p, --port PORT` - Порт для gRPC сервера (по умолчанию: 50051)
- `-h, --host HOST` - Хост для gRPC сервера (по умолчанию: 0.0.0.0)
- `--help` - Показать справку

## Примеры использования

```bash
# Запуск с портом по умолчанию
java -jar controller/build/libs/proxiya-controller.jar

# Запуск на другом порту
java -jar controller/build/libs/proxiya-controller.jar --port 8080

# Запуск на localhost
java -jar controller/build/libs/proxiya-controller.jar --host localhost --port 8080

# Использование скрипта
./run-controller.sh --port 8080 --host localhost
```

## Переменные окружения

- `PROXIYA_PORT` - Порт для gRPC сервера
- `PROXIYA_HOST` - Хост для gRPC сервера

## Архитектура

Контроллер предоставляет следующие gRPC сервисы:

### Для UI-приложения:
- **ProxyControlService** - Управление жизненным циклом прокси (создание, запуск, остановка, удаление)
- **ProxyDataService** - Стриминг перехваченного трафика в UI

### Для mitmproxy инстансов:
- **ProxyInstanceControlService** - Уведомления о событиях прокси (запуск, остановка, ошибки, heartbeat)
- **ProxyInstanceDataService** - Передача перехваченных HTTP данных от прокси в контроллер

## Основные компоненты

- **ProxiyaController** - главный класс контроллера
- **ProxyRepository** - управление состоянием прокси
- **gRPC сервисы** - реализация протоколов связи
- **Koin DI** - dependency injection контейнер

## Логирование

Контроллер использует SLF4J + Logback для логирования. Логи выводятся в консоль с уровнем INFO по умолчанию.
