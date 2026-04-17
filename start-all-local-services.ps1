# ============================================
# 本地微服务启动脚本
# 启动端口: 8081 (mall), 8082 (game), 8083 (gacha), 8084 (auth)
# ============================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  本地微服务启动工具" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查Java环境
Write-Host "[检查] Java环境..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1
    Write-Host "✓ Java已安装" -ForegroundColor Green
} catch {
    Write-Host "✗ 未检测到Java，请先安装JDK 11+" -ForegroundColor Red
    exit 1
}

# 检查Maven环境
Write-Host "[检查] Maven环境..." -ForegroundColor Yellow
try {
    $mavenVersion = mvn -version 2>&1
    Write-Host "✓ Maven已安装" -ForegroundColor Green
} catch {
    Write-Host "✗ 未检测到Maven，请先安装Maven" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "提示: 请确保以下中间件已在本地运行:" -ForegroundColor Yellow
Write-Host "  - MySQL (端口 3306)" -ForegroundColor White
Write-Host "  - Redis (端口 6379)" -ForegroundColor White
Write-Host "  - RabbitMQ (端口 5672, 管理界面 15672)" -ForegroundColor White
Write-Host "  - Elasticsearch (端口 9200)" -ForegroundColor White
Write-Host ""

$continue = Read-Host "中间件都已就绪？(Y/N)"
if ($continue -ne "Y" -and $continue -ne "y") {
    Write-Host "请先启动中间件后再运行此脚本" -ForegroundColor Yellow
    exit 0
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  开始编译项目..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 进入项目根目录并编译
Set-Location E:\CFDemo\gacha-system

Write-Host "[编译] 正在使用Maven编译整个项目..." -ForegroundColor Yellow
mvn clean package -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ 编译失败，请检查错误信息" -ForegroundColor Red
    exit 1
}

Write-Host "✓ 编译成功！" -ForegroundColor Green
Write-Host ""

# 创建日志目录
$logDir = "E:\CFDemo\gacha-system\logs"
if (-not (Test-Path $logDir)) {
    New-Item -ItemType Directory -Path $logDir | Out-Null
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  启动微服务..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 启动 Mall Service (8081)
Write-Host "[启动] Mall Service (端口 8081)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd E:\CFDemo\gacha-system\mall-service; Write-Host 'Starting Mall Service on port 8081...' -ForegroundColor Cyan; java -jar target/mall-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev" -WindowStyle Normal
Start-Sleep -Seconds 3

# 启动 Game Service (8082)
Write-Host "[启动] Game Service (端口 8082)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd E:\CFDemo\gacha-system\game-service; Write-Host 'Starting Game Service on port 8082...' -ForegroundColor Cyan; java -jar target/game-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev" -WindowStyle Normal
Start-Sleep -Seconds 3

# 启动 Gacha Service (8083)
Write-Host "[启动] Gacha Service (端口 8083)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd E:\CFDemo\gacha-system\gacha-service; Write-Host 'Starting Gacha Service on port 8083...' -ForegroundColor Cyan; java -jar target/gacha-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev" -WindowStyle Normal
Start-Sleep -Seconds 3

# 启动 Auth Service (8084)
Write-Host "[启动] Auth Service (端口 8084)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd E:\CFDemo\gacha-system\auth-service; Write-Host 'Starting Auth Service on port 8084...' -ForegroundColor Cyan; java -jar target/auth-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev" -WindowStyle Normal

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  所有微服务已启动！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "服务端口:" -ForegroundColor Cyan
Write-Host "  - Mall Service:   http://localhost:8081/api" -ForegroundColor White
Write-Host "  - Game Service:   http://localhost:8082/api" -ForegroundColor White
Write-Host "  - Gacha Service:  http://localhost:8083/api" -ForegroundColor White
Write-Host "  - Auth Service:   http://localhost:8084/api" -ForegroundColor White
Write-Host ""
Write-Host "提示:" -ForegroundColor Yellow
Write-Host "  1. 每个服务会在独立的PowerShell窗口中运行" -ForegroundColor White
Write-Host "  2. 等待30-60秒让所有服务完全启动" -ForegroundColor White
Write-Host "  3. 查看各窗口日志确认启动成功" -ForegroundColor White
Write-Host "  4. 访问 http://localhost:80 测试前端页面" -ForegroundColor White
Write-Host ""
Write-Host "停止服务: 关闭对应的PowerShell窗口即可" -ForegroundColor Yellow
