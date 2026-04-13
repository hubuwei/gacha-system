@echo off
echo ========================================
echo   Install Elasticsearch 7.13.1 and RabbitMQ
echo ========================================
echo.

set INSTALL_DIR=E:\DeveloperKits

:: Check if files are downloaded
if not exist "%INSTALL_DIR%\elasticsearch-7.13.1.zip" (
    echo [ERROR] elasticsearch-7.13.1.zip not found!
    echo Please wait for download to complete...
    timeout /t 5
)

if not exist "%INSTALL_DIR%\otp_win64_26.2.5.exe" (
    echo [ERROR] otp_win64_26.2.5.exe not found!
    echo Please wait for download to complete...
    timeout /t 5
)

if not exist "%INSTALL_DIR%\rabbitmq-server-3.12.10.exe" (
    echo [ERROR] rabbitmq-server-3.12.10.exe not found!
    echo Please wait for download to complete...
    timeout /t 5
)

:: Step 1: Install Erlang
echo [1/3] Installing Erlang...
start /wait "" "%INSTALL_DIR%\otp_win64_26.2.5.exe" /S
echo [OK] Erlang installed
echo.

:: Step 2: Install RabbitMQ
echo [2/3] Installing RabbitMQ...
start /wait "" "%INSTALL_DIR%\rabbitmq-server-3.12.10.exe" /S
echo [OK] RabbitMQ installed
echo.

:: Step 3: Extract Elasticsearch
echo [3/3] Extracting Elasticsearch...
if exist "%INSTALL_DIR%\elasticsearch" rmdir /s /q "%INSTALL_DIR%\elasticsearch"
powershell -Command "Expand-Archive -Path '%INSTALL_DIR%\elasticsearch-7.13.1.zip' -DestinationPath '%INSTALL_DIR%' -Force"
ren "%INSTALL_DIR%\elasticsearch-7.13.1" elasticsearch
echo [OK] Elasticsearch extracted
echo.

:: Enable RabbitMQ Management Plugin
echo Enabling RabbitMQ Management Plugin...
cd "C:\Program Files\RabbitMQ Server\rabbitmq_server-3.12.10\sbin"
call rabbitmq-plugins.bat enable rabbitmq_management
echo.

echo ========================================
echo   Installation Complete!
echo ========================================
echo.
echo Next steps:
echo 1. Start services using: start-middleware.bat
echo 2. Stop services using: stop-middleware.bat
echo.
pause
