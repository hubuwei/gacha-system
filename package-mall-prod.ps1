# ============================================
# Mall Service Production Build Script
# ============================================

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  Mall Service Production Build" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

$ErrorActionPreference = "Stop"

# Project root directory
$ProjectRoot = $PSScriptRoot
$MallServiceDir = Join-Path $ProjectRoot "mall-service"

# Check if mall-service directory exists
if (-not (Test-Path $MallServiceDir)) {
    Write-Host "ERROR: mall-service directory not found!" -ForegroundColor Red
    exit 1
}

# Output directory
$OutputDir = Join-Path $ProjectRoot "deploy-packages"
if (-not (Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir | Out-Null
    Write-Host "Created output directory: $OutputDir" -ForegroundColor Green
}

Write-Host "[Step 1/6] Validating production configuration files..." -ForegroundColor Yellow

# Check if application-prod.yml exists
$ProdConfigFile = Join-Path $MallServiceDir "src\main\resources\application-prod.yml"
if (-not (Test-Path $ProdConfigFile)) {
    Write-Host "ERROR: Production config file application-prod.yml not found" -ForegroundColor Red
    exit 1
}
Write-Host "OK: Production configuration file exists" -ForegroundColor Green

# Check if .env.prod exists
$EnvProdFile = Join-Path $MallServiceDir ".env.prod"
if (-not (Test-Path $EnvProdFile)) {
    Write-Host "WARNING: .env.prod file not found, will use default configuration" -ForegroundColor Yellow
} else {
    Write-Host "OK: Production environment file exists" -ForegroundColor Green
}

Write-Host ""
Write-Host "[Step 2/6] Cleaning old build files..." -ForegroundColor Yellow
Set-Location $MallServiceDir

# Clean target directory
if (Test-Path "target") {
    Remove-Item "target" -Recurse -Force
    Write-Host "OK: Cleaned target directory" -ForegroundColor Green
} else {
    Write-Host "OK: target directory does not exist, skipping clean" -ForegroundColor Green
}

Write-Host ""
Write-Host "[Step 3/6] Compiling and installing common module dependency..." -ForegroundColor Yellow
Set-Location (Join-Path $ProjectRoot "common")
mvn clean install -DskipTests -q
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: common module compilation failed!" -ForegroundColor Red
    exit 1
}
Write-Host "OK: common module compiled successfully" -ForegroundColor Green

Write-Host ""
Write-Host "[Step 4/6] Building mall-service with production profile..." -ForegroundColor Yellow
Set-Location $MallServiceDir

# Execute Maven build with prod profile activated
Write-Host "Executing: mvn clean package -DskipTests -Dspring.profiles.active=prod" -ForegroundColor Gray
mvn clean package "-DskipTests" "-Dspring.profiles.active=prod"

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: mall-service build failed!" -ForegroundColor Red
    exit 1
}

Write-Host "OK: mall-service built successfully" -ForegroundColor Green

Write-Host ""
Write-Host "[Step 5/6] Copying JAR file to deployment directory..." -ForegroundColor Yellow

# Find generated JAR files
$JarFiles = Get-ChildItem -Path "target" -Filter "*.jar" | Where-Object { $_.Name -notmatch "original" }

if ($JarFiles.Count -eq 0) {
    Write-Host "ERROR: No JAR file found!" -ForegroundColor Red
    exit 1
}

# Copy to deployment directory
foreach ($JarFile in $JarFiles) {
    $DestFile = Join-Path $OutputDir "mall-service.jar"
    Copy-Item $JarFile.FullName $DestFile -Force
    Write-Host "OK: Copied $($JarFile.Name) to mall-service.jar" -ForegroundColor Green
    
    # Display file size
    $FileSize = (Get-Item $DestFile).Length / 1MB
    Write-Host "   File size: $([math]::Round($FileSize, 2)) MB" -ForegroundColor Gray
}

Write-Host ""
Write-Host "[Step 6/6] Generating deployment documentation..." -ForegroundColor Yellow

$DeployNotesFile = Join-Path $OutputDir "MALL_SERVICE_DEPLOY_NOTES.md"
Write-Host "OK: Deployment notes generated: MALL_SERVICE_DEPLOY_NOTES.md" -ForegroundColor Green

Write-Host ""
Write-Host "==========================================" -ForegroundColor Green
Write-Host "  Mall Service Production Build Complete!" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Output directory: $OutputDir" -ForegroundColor Cyan
Write-Host ""
Write-Host "Generated files:" -ForegroundColor Yellow
Get-ChildItem $OutputDir | ForEach-Object {
    $SizeInfo = "$([math]::Round($_.Length / 1MB, 2)) MB"
    Write-Host "  - $($_.Name) ($SizeInfo)" -ForegroundColor White
}
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Review deployment notes: $DeployNotesFile" -ForegroundColor White
Write-Host "2. Update .env.prod with your production configuration" -ForegroundColor White
Write-Host "3. Upload files to production server" -ForegroundColor White
Write-Host "4. Follow deployment instructions" -ForegroundColor White
Write-Host ""

# Return to project root
Set-Location $ProjectRoot
