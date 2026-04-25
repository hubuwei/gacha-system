# CMS Startup Script
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Game Mall CMS - Quick Start" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Check MySQL
Write-Host "[1/4] Checking MySQL connection..." -ForegroundColor Yellow
try {
    $result = mysql -u root -p123456 -e "SELECT 1;" 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "[OK] MySQL connection successful" -ForegroundColor Green
    } else {
        Write-Host "[ERROR] MySQL connection failed. Please check if MySQL is running." -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "[ERROR] mysql command not found. Please ensure MySQL is installed and added to PATH." -ForegroundColor Red
    exit 1
}

# Step 2: Initialize Database
Write-Host ""
Write-Host "[2/4] Initializing CMS database tables..." -ForegroundColor Yellow
$cmsSqlPath = "E:\CFDemo\gacha-system\database\create_cms_tables.sql"
if (Test-Path $cmsSqlPath) {
    try {
        Get-Content $cmsSqlPath -Encoding UTF8 | mysql -u root -p123456 gacha_system_dev 2>&1 | Out-Null
        if ($LASTEXITCODE -eq 0) {
            Write-Host "[OK] Database tables created successfully" -ForegroundColor Green
        } else {
            Write-Host "[WARN] Database tables may already exist, skipping creation" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "[WARN] Database initialization warning, continuing..." -ForegroundColor Yellow
    }
} else {
    Write-Host "[ERROR] SQL file not found: $cmsSqlPath" -ForegroundColor Red
}

# Step 3: Compile common module
Write-Host ""
Write-Host "[3/4] Compiling common module..." -ForegroundColor Yellow
Set-Location "E:\CFDemo\gacha-system\common"
mvn clean install -DskipTests -q
if ($LASTEXITCODE -eq 0) {
    Write-Host "[OK] Common module compiled successfully" -ForegroundColor Green
} else {
    Write-Host "[ERROR] Common module compilation failed" -ForegroundColor Red
    exit 1
}

# Step 4: Start backend service
Write-Host ""
Write-Host "[4/4] Starting CMS backend service..." -ForegroundColor Yellow
Set-Location "E:\CFDemo\gacha-system\cms-service"

# Check if port 8085 is in use
$portInUse = netstat -ano | findstr ":8085"
if ($portInUse) {
    Write-Host "[WARN] Port 8085 is already in use. Please stop the existing service first." -ForegroundColor Yellow
    Write-Host "  Use this command to check the process:" -ForegroundColor Gray
    Write-Host "  netstat -ano | findstr `":8085`"" -ForegroundColor Gray
} else {
    # Start Java service in background
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd E:\CFDemo\gacha-system\cms-service; mvn spring-boot:run" -WindowStyle Normal
    Write-Host "[OK] CMS backend service is starting..." -ForegroundColor Green
    Write-Host "  URL: http://localhost:8085" -ForegroundColor Cyan
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Next Steps:" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Wait for backend service to start (about 30-60 seconds)" -ForegroundColor White
Write-Host "2. Open a new terminal and navigate to frontend directory:" -ForegroundColor White
Write-Host "   cd E:\CFDemo\gacha-system\cms-admin" -ForegroundColor Gray
Write-Host "3. Start frontend development server:" -ForegroundColor White
Write-Host "   npm run dev" -ForegroundColor Gray
Write-Host "4. Open browser and visit:" -ForegroundColor White
Write-Host "   http://localhost:5174" -ForegroundColor Cyan
Write-Host ""
Write-Host "Default admin account:" -ForegroundColor Yellow
Write-Host "  Username: admin" -ForegroundColor Gray
Write-Host "  Password: admin123" -ForegroundColor Gray
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
