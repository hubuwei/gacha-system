@echo off
echo ========================================
echo   Stop Elasticsearch and RabbitMQ
echo ========================================
echo.

:: Stop RabbitMQ container
echo [1/2] Stopping RabbitMQ...
docker stop rabbitmq >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] RabbitMQ stopped
) else (
    echo [INFO] RabbitMQ is not running
)

:: Stop Elasticsearch container
echo.
echo [2/2] Stopping Elasticsearch...
docker stop elasticsearch >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Elasticsearch stopped
) else (
    echo [INFO] Elasticsearch is not running
)

echo.
echo ========================================
echo   All services stopped
echo ========================================
echo.
pause
