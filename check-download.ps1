# Monitor download progress
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Monitoring Download Progress" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$installDir = "E:\DeveloperKits"

do {
    Clear-Host
    Write-Host "Checking files..." -ForegroundColor Yellow
    Write-Host ""
    
    $files = @(
        @{Name="Elasticsearch 7.13.1"; File="$installDir\elasticsearch-7.13.1.zip"; ExpectedMB=330},
        @{Name="Erlang OTP 26.2.5"; File="$installDir\otp_win64_26.2.5.exe"; ExpectedMB=120},
        @{Name="RabbitMQ 3.12.10"; File="$installDir\rabbitmq-server-3.12.10.exe"; ExpectedMB=20}
    )
    
    $allComplete = $true
    
    foreach ($item in $files) {
        if (Test-Path $item.File) {
            $size = [math]::Round((Get-Item $item.File).Length / 1MB, 2)
            $status = if ($size -gt ($item.ExpectedMB * 0.9)) { "[OK]" } else { "[DL]" }
            $color = if ($size -gt ($item.ExpectedMB * 0.9)) { "Green" } else { "Yellow" }
            
            Write-Host "$status $($item.Name): ${size}MB / ~$($item.ExpectedMB)MB" -ForegroundColor $color
            
            if ($size -lt ($item.ExpectedMB * 0.9)) {
                $allComplete = $false
            }
        } else {
            Write-Host "[..] $($item.Name): Not started" -ForegroundColor Gray
            $allComplete = $false
        }
    }
    
    Write-Host ""
    if ($allComplete) {
        Write-Host "========================================" -ForegroundColor Green
        Write-Host "  All downloads complete!" -ForegroundColor Green
        Write-Host "========================================" -ForegroundColor Green
        Write-Host ""
        Write-Host "Now run: .\install-middleware.bat" -ForegroundColor Cyan
        break
    } else {
        Write-Host "Waiting... (Press Ctrl+C to cancel)" -ForegroundColor Gray
        Start-Sleep -Seconds 5
    }
} while ($true)

pause
