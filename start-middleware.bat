@echo off
echo ========================================
echo   Start Elasticsearch and RabbitMQ
echo ========================================
echo.

:: Check if Elasticsearch is running
echo [1/2] Checking Elasticsearch...
curl -s http://localhost:9200 >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Elasticsearch is already running
) else (
    echo [INFO] Starting Elasticsearch with Docker...
    docker ps | findstr elasticsearch >nul 2>&1
    if %errorlevel% equ 0 (
        echo [OK] Elasticsearch container is running
    ) else (
        echo [INFO] Pulling and starting Elasticsearch...
        docker run -d --name elasticsearch -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" -e "xpack.security.enabled=false" elasticsearch:7.17.9
        echo [WAIT] Waiting for Elasticsearch to start (30 seconds)...
        timeout /t 30 /nobreak >nul
    )
)

:: Check if RabbitMQ is running
echo.
echo [2/2] Checking RabbitMQ...
docker ps | findstr rabbitmq >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] RabbitMQ container is running
) else (
    echo [INFO] Starting RabbitMQ with Docker...
    docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3.12-management
    echo [WAIT] Waiting for RabbitMQ to start (15 seconds)...
    timeout /t 15 /nobreak >nul
)

echo.
echo ========================================
echo   All services started!
echo ========================================
echo.
echo Elasticsearch: http://localhost:9200
echo RabbitMQ Management: http://localhost:15672 (guest/guest)
echo.
pause
