#!/bin/bash

# Скрипт для сборки и запуска Proxiya Controller

set -e

echo "🔨 Proxiya Controller Build & Run Script"
echo "=========================================="

# Пересобираем контроллер
echo "🔨 Building controller JAR..."
./gradlew :controller:fatJar --rerun-tasks

echo "✅ Build completed!"
echo ""

# Передаем все аргументы в оригинальный скрипт
echo "🚀 Starting controller..."
echo "=========================="

# Вызываем оригинальный скрипт с переданными аргументами
exec ./run-controller.sh "$@"
