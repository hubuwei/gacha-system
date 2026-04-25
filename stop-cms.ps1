# Stop CMS Services
Write-Host "Stopping CMS services..." -ForegroundColor Yellow
Write-Host ""

# Check and stop port 8085 (backend)
$backendPort = netstat -ano | findstr ":8085"
if ($backendPort) {
    Write-Host "Found process on port 8085:" -ForegroundColor Cyan
    $backendPort
    $processId = ($backendPort -split '\s+')[-1]
    Write-Host ""
    Write-Host "Stopping backend service (PID: $processId)..." -ForegroundColor Yellow
    taskkill /F /PID $processId 2>&1 | Out-Null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "[OK] Backend service stopped" -ForegroundColor Green
    } else {
        Write-Host "[WARN] Failed to stop backend service" -ForegroundColor Yellow
    }
} else {
    Write-Host "[INFO] No process found on port 8085" -ForegroundColor Gray
}

Write-Host ""

# Check and stop port 5174 (frontend)
$frontendPort = netstat -ano | findstr ":5174"
if ($frontendPort) {
    Write-Host "Found process on port 5174:" -ForegroundColor Cyan
    $frontendPort
    $processId = ($frontendPort -split '\s+')[-1]
    Write-Host ""
    Write-Host "Stopping frontend service (PID: $processId)..." -ForegroundColor Yellow
    taskkill /F /PID $processId 2>&1 | Out-Null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "[OK] Frontend service stopped" -ForegroundColor Green
    } else {
        Write-Host "[WARN] Failed to stop frontend service" -ForegroundColor Yellow
    }
} else {
    Write-Host "[INFO] No process found on port 5174" -ForegroundColor Gray
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "All CMS services stopped" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
