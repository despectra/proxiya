#!/bin/bash

# Скрипт для сборки и запуска Proxiya Controller

set -e

echo "🚀 Proxiya Controller Build & Run Script"
echo "========================================"

# Проверяем, есть ли уже собранный jar
if [ ! -f "controller/build/libs/proxiya-controller.jar" ]; then
    echo "📦 Building controller JAR..."
    ./gradlew :controller:fatJar
    echo "✅ Build completed!"
else
    echo "📦 JAR already exists, skipping build..."
fi

# Парсим аргументы
PORT=50051
HOST="0.0.0.0"

while [[ $# -gt 0 ]]; do
    case $1 in
        -p|--port)
            PORT="$2"
            shift 2
            ;;
        -h|--host)
            HOST="$2"
            shift 2
            ;;
        --help)
            echo "Usage: $0 [options]"
            echo "Options:"
            echo "  -p, --port PORT     Port for gRPC server (default: 50051)"
            echo "  -h, --host HOST     Host for gRPC server (default: 0.0.0.0)"
            echo "  --help              Show this help message"
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

echo "🎯 Starting Proxiya Controller..."
echo "   Host: $HOST"
echo "   Port: $PORT"
echo "   JAR: controller/build/libs/proxiya-controller.jar"
echo ""
echo "Press Ctrl+C to stop the controller"
echo "========================================"

# Запускаем контроллер
java -jar controller/build/libs/proxiya-controller.jar --port "$PORT" --host "$HOST"
