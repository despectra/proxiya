#!/usr/bin/env python3
"""
Скрипт для генерации Python gRPC стабов из proto файлов
"""

import os
import subprocess
import sys
from pathlib import Path


def generate_grpc_stubs():
    """Генерирует Python gRPC стабы из proto файлов"""
    
    # Пути к файлам
    script_dir = Path(__file__).parent
    project_root = script_dir.parent
    proto_dir = project_root / "protocols" / "src" / "main" / "proto"
    output_dir = script_dir / "generated"
    venv_python = script_dir / "venv" / "bin" / "python"
    
    # Создаем директорию для сгенерированных файлов
    output_dir.mkdir(exist_ok=True)
    
    # Основной proto файл
    main_proto = proto_dir / "proxiya.proto"
    
    if not main_proto.exists():
        print(f"Error: Proto file not found: {main_proto}")
        return False
    
    # Проверяем наличие виртуального окружения
    if not venv_python.exists():
        print(f"Error: Virtual environment not found at {venv_python}")
        print("Please run: python3 -m venv venv && source venv/bin/activate && pip install -r requirements.txt")
        return False
    
    # Находим все proto файлы
    proto_files = list(proto_dir.glob("**/*.proto"))
    if not proto_files:
        print(f"Error: No proto files found in {proto_dir}")
        return False
    
    # Команда для генерации с использованием venv
    cmd = [
        str(venv_python), "-m", "grpc_tools.protoc",
        f"--proto_path={proto_dir}",
        f"--python_out={output_dir}",
        f"--grpc_python_out={output_dir}",
    ] + [str(f) for f in proto_files]
    
    print(f"Generating gRPC stubs from {main_proto}")
    print(f"Command: {' '.join(cmd)}")
    
    try:
        result = subprocess.run(cmd, check=True, capture_output=True, text=True)
        print("gRPC stubs generated successfully!")
        print(f"Output directory: {output_dir}")
        
        # Показываем сгенерированные файлы
        generated_files = list(output_dir.glob("*.py"))
        if generated_files:
            print("\nGenerated files:")
            for file in generated_files:
                print(f"  - {file.name}")
        
        return True
        
    except subprocess.CalledProcessError as e:
        print(f"Error generating gRPC stubs: {e}")
        print(f"stdout: {e.stdout}")
        print(f"stderr: {e.stderr}")
        return False
    except FileNotFoundError:
        print("Error: grpc_tools not found. Please install it with:")
        print("pip install grpcio-tools")
        return False


def create_init_file():
    """Создает __init__.py файл для пакета"""
    script_dir = Path(__file__).parent
    output_dir = script_dir / "generated"
    
    init_file = output_dir / "__init__.py"
    init_file.write_text('"""Generated gRPC stubs for Proxiya"""\n')


if __name__ == "__main__":
    print("Proxiya gRPC Stub Generator")
    print("=" * 40)
    
    if generate_grpc_stubs():
        create_init_file()
        print("\n✅ All done! gRPC stubs are ready to use.")
    else:
        print("\n❌ Failed to generate gRPC stubs.")
        sys.exit(1)
