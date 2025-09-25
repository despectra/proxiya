#!/usr/bin/env python3
"""
Скрипт для базового тестирования Proxiya mitmproxy аддона
"""

import asyncio
import os
import signal
import subprocess
import sys
import time
from pathlib import Path
from typing import Optional


class Colors:
    """ANSI цветовые коды для красивого вывода"""
    GREEN = '\033[0;32m'
    YELLOW = '\033[1;33m'
    RED = '\033[0;31m'
    BLUE = '\033[0;34m'
    CYAN = '\033[0;36m'
    NC = '\033[0m'  # No Color


def print_colored(message: str, color: str = Colors.NC):
    """Выводит сообщение с цветом"""
    print(f"{color}{message}{Colors.NC}")


def print_header(message: str):
    """Выводит заголовок"""
    print_colored(f"\n🧪 {message}", Colors.CYAN)
    print("=" * (len(message) + 3))


def print_success(message: str):
    """Выводит сообщение об успехе"""
    print_colored(f"✅ {message}", Colors.GREEN)


def print_error(message: str):
    """Выводит сообщение об ошибке"""
    print_colored(f"❌ {message}", Colors.RED)


def print_warning(message: str):
    """Выводит предупреждение"""
    print_colored(f"⚠️  {message}", Colors.YELLOW)


def print_info(message: str):
    """Выводит информационное сообщение"""
    print_colored(f"ℹ️  {message}", Colors.BLUE)


class AddonTester:
    """Класс для тестирования аддона"""
    
    def __init__(self):
        self.script_dir = Path(__file__).parent
        self.addon_file = self.script_dir / "proxiya_addon.py"
        self.venv_python = self.script_dir / "venv" / "bin" / "python"
        self.mitm_process: Optional[subprocess.Popen] = None
        self.proxy_port = 8889
        
    def check_environment(self) -> bool:
        """Проверяет окружение"""
        print_header("Проверка окружения")
        
        # Проверяем наличие аддона
        if not self.addon_file.exists():
            print_error(f"Файл аддона не найден: {self.addon_file}")
            return False
        print_success("Файл аддона найден")
        
        # Проверяем виртуальное окружение
        if not self.venv_python.exists():
            print_error(f"Виртуальное окружение не найдено: {self.venv_python}")
            print_info("Запустите: python3 -m venv venv && source venv/bin/activate && pip install -r requirements.txt")
            return False
        print_success("Виртуальное окружение найдено")
        
        # Проверяем mitmproxy
        try:
            # Сначала пробуем mitmdump как исполняемый файл
            result = subprocess.run(
                ["mitmdump", "--version"],
                capture_output=True, text=True, timeout=10,
                env=dict(os.environ, PATH=str(self.script_dir / "venv" / "bin") + ":" + os.environ.get("PATH", ""))
            )
            if result.returncode == 0:
                version_line = result.stdout.strip().split('\n')[0]
                print_success(f"Mitmproxy найден: {version_line}")
            else:
                print_error("Mitmproxy не работает")
                if result.stderr:
                    print_error(f"Ошибка: {result.stderr}")
                return False
        except (subprocess.TimeoutExpired, FileNotFoundError) as e:
            print_error(f"Ошибка проверки mitmproxy: {e}")
            return False
        
        return True
    
    def test_addon_import(self) -> bool:
        """Тестирует импорт аддона"""
        print_header("Тестирование импорта аддона")
        
        try:
            # Устанавливаем переменные окружения
            env = os.environ.copy()
            env['PROXIYA_PROXY_ID'] = 'test_addon'
            env['PROXIYA_CONTROLLER_HOST'] = 'localhost'
            env['PROXIYA_CONTROLLER_PORT'] = '50051'
            
            # Добавляем текущую директорию в PYTHONPATH
            python_path = str(self.script_dir)
            if 'PYTHONPATH' in env:
                env['PYTHONPATH'] = f"{python_path}:{env['PYTHONPATH']}"
            else:
                env['PYTHONPATH'] = python_path
            
            result = subprocess.run(
                [str(self.venv_python), "-c", """
import sys
sys.path.insert(0, '.')
try:
    from proxiya_addon import ProxiyaAddon
    addon = ProxiyaAddon()
    print('SUCCESS: Addon imported and created successfully')
    print(f'Proxy ID: {addon.proxy_id}')
    print(f'Controller: {addon.controller_host}:{addon.controller_port}')
except Exception as e:
    print(f'ERROR: {e}')
    import traceback
    traceback.print_exc()
    sys.exit(1)
"""],
                cwd=self.script_dir,
                env=env,
                capture_output=True,
                text=True,
                timeout=30
            )
            
            if result.returncode == 0:
                print_success("Аддон успешно импортируется")
                for line in result.stdout.strip().split('\n'):
                    if line.startswith('SUCCESS:'):
                        print_info(line[8:].strip())
                    elif line.startswith('Proxy ID:') or line.startswith('Controller:'):
                        print_info(line.strip())
                return True
            else:
                print_error("Ошибка импорта аддона:")
                print(result.stdout)
                print(result.stderr)
                return False
                
        except subprocess.TimeoutExpired:
            print_error("Таймаут при импорте аддона")
            return False
        except Exception as e:
            print_error(f"Неожиданная ошибка: {e}")
            return False
    
    def start_mitmproxy(self) -> bool:
        """Запускает mitmproxy с аддоном"""
        print_header("Запуск mitmproxy с аддоном")
        
        try:
            # Устанавливаем переменные окружения
            env = os.environ.copy()
            env['PROXIYA_PROXY_ID'] = 'test_addon'
            env['PROXIYA_CONTROLLER_HOST'] = 'localhost'
            env['PROXIYA_CONTROLLER_PORT'] = '50051'
            
            # Запускаем mitmproxy
            cmd = [
                "mitmdump",
                "-s", str(self.addon_file),
                "-p", str(self.proxy_port),
                "--quiet"
            ]
            
            print_info(f"Команда: {' '.join(cmd)}")
            
            # Добавляем venv/bin в PATH
            env['PATH'] = str(self.script_dir / "venv" / "bin") + ":" + env.get("PATH", "")
            
            self.mitm_process = subprocess.Popen(
                cmd,
                cwd=self.script_dir,
                env=env,
                stdout=subprocess.PIPE,
                stderr=subprocess.PIPE,
                text=True
            )
            
            # Ждем запуска
            time.sleep(3)
            
            if self.mitm_process.poll() is None:
                print_success(f"Mitmproxy запущен (PID: {self.mitm_process.pid})")
                return True
            else:
                stdout, stderr = self.mitm_process.communicate()
                print_error("Mitmproxy не запустился")
                if stdout:
                    print("STDOUT:", stdout)
                if stderr:
                    print("STDERR:", stderr)
                return False
                
        except Exception as e:
            print_error(f"Ошибка запуска mitmproxy: {e}")
            return False
    
    def test_proxy_functionality(self) -> bool:
        """Тестирует функциональность прокси"""
        print_header("Тестирование функциональности прокси")
        
        try:
            # Проверяем, что прокси отвечает
            print_info("Проверяем доступность прокси...")
            
            result = subprocess.run(
                ["curl", "-x", f"localhost:{self.proxy_port}", "--connect-timeout", "5", "-s", "http://httpbin.org/ip"],
                capture_output=True,
                text=True,
                timeout=10
            )
            
            if result.returncode == 0 and result.stdout.strip():
                print_success("Прокси работает")
                try:
                    import json
                    response_data = json.loads(result.stdout)
                    if 'origin' in response_data:
                        print_info(f"IP через прокси: {response_data['origin']}")
                    else:
                        print_info(f"Ответ прокси: {result.stdout[:100]}...")
                except json.JSONDecodeError:
                    print_info(f"Ответ прокси: {result.stdout[:100]}...")
                return True
            else:
                print_error("Прокси не отвечает")
                if result.stderr:
                    print_error(f"Ошибка curl: {result.stderr}")
                return False
                
        except subprocess.TimeoutExpired:
            print_error("Таймаут при тестировании прокси")
            return False
        except FileNotFoundError:
            print_warning("curl не найден, пропускаем тест прокси")
            return True
        except Exception as e:
            print_error(f"Ошибка тестирования прокси: {e}")
            return False
    
    def stop_mitmproxy(self):
        """Останавливает mitmproxy"""
        if self.mitm_process and self.mitm_process.poll() is None:
            print_info("Останавливаем mitmproxy...")
            try:
                # Отправляем SIGTERM
                self.mitm_process.terminate()
                self.mitm_process.wait(timeout=5)
                print_success("Mitmproxy остановлен")
            except subprocess.TimeoutExpired:
                print_warning("Принудительно завершаем mitmproxy...")
                self.mitm_process.kill()
                self.mitm_process.wait()
                print_success("Mitmproxy принудительно остановлен")
            except Exception as e:
                print_error(f"Ошибка остановки mitmproxy: {e}")
    
    def run_all_tests(self) -> bool:
        """Запускает все тесты"""
        print_colored("🧪 Тестирование Proxiya mitmproxy аддона", Colors.CYAN)
        print("=" * 50)
        
        success = True
        
        # Проверка окружения
        if not self.check_environment():
            success = False
        
        # Тест импорта
        if success and not self.test_addon_import():
            success = False
        
        # Запуск mitmproxy
        if success and not self.start_mitmproxy():
            success = False
        
        # Тест прокси
        if success and not self.test_proxy_functionality():
            success = False
        
        # Остановка mitmproxy
        self.stop_mitmproxy()
        
        # Результат
        if success:
            print_colored("\n🎉 Все тесты пройдены!", Colors.GREEN)
        else:
            print_colored("\n💥 Некоторые тесты не пройдены!", Colors.RED)
        
        return success


def main():
    """Основная функция"""
    tester = AddonTester()
    
    try:
        success = tester.run_all_tests()
        sys.exit(0 if success else 1)
    except KeyboardInterrupt:
        print_colored("\n⚠️  Тестирование прервано пользователем", Colors.YELLOW)
        tester.stop_mitmproxy()
        sys.exit(1)
    except Exception as e:
        print_error(f"Неожиданная ошибка: {e}")
        tester.stop_mitmproxy()
        sys.exit(1)


if __name__ == "__main__":
    main()
