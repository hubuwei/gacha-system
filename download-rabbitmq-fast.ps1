# Quick download RabbitMQ using Chinese mirror
Write-Host "Downloading RabbitMQ from Chinese mirror..." -ForegroundColor Cyan

$urls = @(
    "https://mirrors.cloud.tencent.com/rabbitmq/v3.12.10/rabbitmq-server-3.12.10.exe",
    "https://github.com/rabbitmq/rabbitmq-server/releases/download/v3.12.10/rabbitmq-server-3.12.10.exe"
)

$output = "E:\DeveloperKits\rabbitmq-server-3.12.10.exe"

foreach ($url in $urls) {
    try {
        Write-Host "Trying: $url" -ForegroundColor Yellow
        Invoke-WebRequest -Uri $url -OutFile $output -TimeoutSec 120
        $size = [math]::Round((Get-Item $output).Length / 1MB, 2)
        Write-Host "[OK] Downloaded: ${size}MB" -ForegroundColor Green
        exit 0
    } catch {
        Write-Host "[Failed] $_" -ForegroundColor Red
        Remove-Item $output -Force -ErrorAction SilentlyContinue
    }
}

Write-Host "[ERROR] All download methods failed" -ForegroundColor Red
exit 1
