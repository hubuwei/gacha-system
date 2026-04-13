# Install Elasticsearch and RabbitMQ Script
$installDir = "E:\DeveloperKits"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Download & Install Middleware" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Create directory if not exists
if (-not (Test-Path $installDir)) {
    New-Item -ItemType Directory -Path $installDir -Force
}

# Step 1: Download Elasticsearch 7.13.1
$esZip = "$installDir\elasticsearch-7.13.1.zip"
if (-not (Test-Path $esZip)) {
    Write-Host "[1/3] Downloading Elasticsearch 7.13.1..." -ForegroundColor Yellow
    Invoke-WebRequest -Uri "https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-7.13.1-windows-x86_64.zip" -OutFile $esZip
    Write-Host "[OK] Elasticsearch downloaded" -ForegroundColor Green
} else {
    Write-Host "[SKIP] Elasticsearch already downloaded" -ForegroundColor Gray
}

# Step 2: Download Erlang
$erlangExe = "$installDir\otp_win64_26.2.5.exe"
if (-not (Test-Path $erlangExe)) {
    Write-Host "[2/3] Downloading Erlang..." -ForegroundColor Yellow
    Invoke-WebRequest -Uri "https://github.com/erlang/otp/releases/download/OTP-26.2.5/otp_win64_26.2.5.exe" -OutFile $erlangExe
    Write-Host "[OK] Erlang downloaded" -ForegroundColor Green
} else {
    Write-Host "[SKIP] Erlang already downloaded" -ForegroundColor Gray
}

# Step 3: Download RabbitMQ
$rabbitExe = "$installDir\rabbitmq-server-3.12.10.exe"
if (-not (Test-Path $rabbitExe)) {
    Write-Host "[3/3] Downloading RabbitMQ..." -ForegroundColor Yellow
    Invoke-WebRequest -Uri "https://github.com/rabbitmq/rabbitmq-server/releases/download/v3.12.10/rabbitmq-server-3.12.10.exe" -OutFile $rabbitExe
    Write-Host "[OK] RabbitMQ downloaded" -ForegroundColor Green
} else {
    Write-Host "[SKIP] RabbitMQ already downloaded" -ForegroundColor Gray
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  All files downloaded!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Now run: .\install-middleware.bat" -ForegroundColor Yellow
Write-Host ""
pause
