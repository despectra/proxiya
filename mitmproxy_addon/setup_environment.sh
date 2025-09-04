#!/bin/bash

# Скрипт для автоматической настройки окружения mitmproxy_addon
# Создает виртуальное окружение и устанавливает зависимости

set -e  # Остановиться при ошибке

echo "🚀 Настройка окружения для mitmproxy_addon..."
echo "=============================================="

# Проверяем наличие Python 3
if ! command -v python3 &> /dev/null; then
    echo "❌ Python 3 не найден. Пожалуйста, установите Python 3."
    exit 1
fi

echo "✅ Python 3 найден: $(python3 --version)"

# Создаем виртуальное окружение если его нет
if [ ! -d "venv" ]; then
    echo "📦 Создание виртуального окружения..."
    python3 -m venv venv
    echo "✅ Виртуальное окружение создано"
else
    echo "✅ Виртуальное окружение уже существует"
fi

# Активируем виртуальное окружение
echo "🔧 Активация виртуального окружения..."
source venv/bin/activate

# Обновляем pip
echo "⬆️  Обновление pip..."
pip install --upgrade pip

# Устанавливаем зависимости
echo "📚 Установка зависимостей..."
pip install mitmproxy==9.0.1 grpcio grpcio-tools protobuf

echo ""
echo "🎉 Окружение настроено успешно!"
echo ""
echo "Для активации окружения выполните:"
echo "  source venv/bin/activate"
echo ""
echo "Для генерации gRPC стабов выполните:"
echo "  python3 generate_grpc_stubs.py"
echo ""
echo "Для запуска mitmproxy с аддоном:"
echo "  source venv/bin/activate"
echo "  mitmdump -s proxiya_addon.py --set confdir=."
