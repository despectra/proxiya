#!/bin/bash

# Скрипт для запуска mitmproxy с Proxiya аддоном

set -e

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Функция для вывода сообщений
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Проверяем наличие Python
if ! command -v python3 &> /dev/null; then
    log_error "Python3 не найден. Установите Python 3.7+"
    exit 1
fi

# Проверяем наличие pip
if ! command -v pip3 &> /dev/null; then
    log_error "pip3 не найден. Установите pip"
    exit 1
fi

# Проверяем наличие mitmproxy
if ! command -v mitmdump &> /dev/null; then
    log_error "mitmproxy не найден. Установите mitmproxy:"
    echo "pip3 install mitmproxy"
    exit 1
fi

# Получаем директорию скрипта
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ADDON_FILE="$SCRIPT_DIR/proxiya_addon.py"

# Проверяем наличие аддона
if [ ! -f "$ADDON_FILE" ]; then
    log_error "Файл аддона не найден: $ADDON_FILE"
    exit 1
fi

# Параметры по умолчанию
PROXY_ID=${PROXIYA_PROXY_ID:-"default"}
CONTROLLER_HOST=${PROXIYA_CONTROLLER_HOST:-"localhost"}
CONTROLLER_PORT=${PROXIYA_CONTROLLER_PORT:-"50051"}
PROXY_PORT=${PROXIYA_PROXY_PORT:-"8080"}
USE_TLS=${PROXIYA_USE_TLS:-"false"}

# Парсим аргументы командной строки
while [[ $# -gt 0 ]]; do
    case $1 in
        --proxy-id)
            PROXY_ID="$2"
            shift 2
            ;;
        --controller-host)
            CONTROLLER_HOST="$2"
            shift 2
            ;;
        --controller-port)
            CONTROLLER_PORT="$2"
            shift 2
            ;;
        --proxy-port)
            PROXY_PORT="$2"
            shift 2
            ;;
        --use-tls)
            USE_TLS="true"
            shift
            ;;
        --help)
            echo "Использование: $0 [опции]"
            echo ""
            echo "Опции:"
            echo "  --proxy-id ID           ID прокси (по умолчанию: $PROXY_ID)"
            echo "  --controller-host HOST  Хост контроллера (по умолчанию: $CONTROLLER_HOST)"
            echo "  --controller-port PORT  Порт контроллера (по умолчанию: $CONTROLLER_PORT)"
            echo "  --proxy-port PORT       Порт прокси (по умолчанию: $PROXY_PORT)"
            echo "  --use-tls               Использовать TLS для подключения к контроллеру"
            echo "  --help                  Показать эту справку"
            echo ""
            echo "Переменные окружения:"
            echo "  PROXIYA_PROXY_ID        ID прокси"
            echo "  PROXIYA_CONTROLLER_HOST Хост контроллера"
            echo "  PROXIYA_CONTROLLER_PORT Порт контроллера"
            echo "  PROXIYA_PROXY_PORT      Порт прокси"
            echo "  PROXIYA_USE_TLS         Использовать TLS (true/false)"
            exit 0
            ;;
        *)
            log_error "Неизвестный параметр: $1"
            echo "Используйте --help для справки"
            exit 1
            ;;
    esac
done

# Проверяем, что gRPC стабы сгенерированы
GENERATED_DIR="$SCRIPT_DIR/generated"
if [ ! -d "$GENERATED_DIR" ] || [ ! -f "$GENERATED_DIR/proxiya_pb2.py" ]; then
    log_warn "gRPC стабы не найдены. Генерируем..."
    cd "$SCRIPT_DIR"
    python3 generate_grpc_stubs.py
    if [ $? -ne 0 ]; then
        log_error "Не удалось сгенерировать gRPC стабы"
        exit 1
    fi
fi

# Устанавливаем переменные окружения
export PROXIYA_PROXY_ID="$PROXY_ID"
export PROXIYA_CONTROLLER_HOST="$CONTROLLER_HOST"
export PROXIYA_CONTROLLER_PORT="$CONTROLLER_PORT"
export PROXIYA_USE_TLS="$USE_TLS"

# Выводим конфигурацию
log_info "Запуск Proxiya mitmproxy аддона"
echo "  Proxy ID: $PROXY_ID"
echo "  Controller: $CONTROLLER_HOST:$CONTROLLER_PORT"
echo "  Proxy Port: $PROXY_PORT"
echo "  Use TLS: $USE_TLS"
echo ""

# Проверяем подключение к контроллеру
log_info "Проверяем подключение к контроллеру..."
if command -v nc &> /dev/null; then
    if nc -z "$CONTROLLER_HOST" "$CONTROLLER_PORT" 2>/dev/null; then
        log_info "Контроллер доступен"
    else
        log_warn "Контроллер недоступен на $CONTROLLER_HOST:$CONTROLLER_PORT"
        log_warn "Убедитесь, что контроллер запущен"
    fi
else
    log_warn "netcat не найден, пропускаем проверку подключения"
fi

# Запускаем mitmproxy
log_info "Запускаем mitmproxy..."
echo ""

cd "$SCRIPT_DIR"
exec mitmdump \
    -s "$ADDON_FILE" \
    -p "$PROXY_PORT" \
    --set proxiya_proxy_id="$PROXY_ID" \
    --set proxiya_controller_host="$CONTROLLER_HOST" \
    --set proxiya_controller_port="$CONTROLLER_PORT" \
    --set proxiya_use_tls="$USE_TLS" \
    "$@"
