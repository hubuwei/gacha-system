# ============================================
# Database Sync Tool - Local to Server
# ============================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Database Sync Tool" -ForegroundColor Cyan
Write-Host "  Local gacha_system_dev -> Server gacha_system_prod" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$LOCAL_MYSQL_USER = "root"
$LOCAL_MYSQL_PASSWORD = "123456"
$LOCAL_DB_NAME = "gacha_system_dev"
$SERVER_IP = "111.228.12.167"
$SERVER_USER = "root"
$SERVER_DB_NAME = "gacha_system_prod"
$SERVER_MYSQL_PASSWORD = "Xc037417!"
$EXPORT_FILE = "gacha_system_dev_export.sql"
$IMPORT_FILE = "gacha_system_prod_import.sql"

# Step 1: Export local database
Write-Host "[Step 1/4] Exporting local database..." -ForegroundColor Cyan
$mysqldumpCmd = "mysqldump -u$LOCAL_MYSQL_USER -p$LOCAL_MYSQL_PASSWORD --databases $LOCAL_DB_NAME --result-file=$EXPORT_FILE"
Write-Host "Command: $mysqldumpCmd" -ForegroundColor Gray

try {
    Invoke-Expression $mysqldumpCmd
    if (Test-Path $EXPORT_FILE) {
        Write-Host "OK Database exported successfully" -ForegroundColor Green
        $fileSize = (Get-Item $EXPORT_FILE).Length / 1MB
        Write-Host "  File size: $([math]::Round($fileSize, 2)) MB" -ForegroundColor Gray
    } else {
        Write-Host "ERROR Export failed, file not created" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "ERROR Export failed: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Step 2: Modify database name
Write-Host "[Step 2/4] Modifying database name to gacha_system_prod..." -ForegroundColor Cyan

try {
    $content = Get-Content $EXPORT_FILE -Raw -Encoding UTF8
    $content = $content -replace "gacha_system_dev", "gacha_system_prod"
    $content | Set-Content $IMPORT_FILE -Encoding UTF8 -NoNewline
    Write-Host "OK Database name modified" -ForegroundColor Green
} catch {
    Write-Host "ERROR Modification failed: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Step 3: Upload to server
Write-Host "[Step 3/4] Uploading SQL file to server..." -ForegroundColor Cyan
$scpCmd = "scp $IMPORT_FILE ${SERVER_USER}@${SERVER_IP}:/opt/gacha-system/"
Write-Host "Command: $scpCmd" -ForegroundColor Gray

try {
    Invoke-Expression $scpCmd
    Write-Host "OK File uploaded successfully" -ForegroundColor Green
} catch {
    Write-Host "ERROR Upload failed: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Step 4: Import on server
Write-Host "[Step 4/4] Importing database on server..." -ForegroundColor Cyan
$importCmd = "ssh ${SERVER_USER}@${SERVER_IP} `"cd /opt/gacha-system; docker exec -i gacha-mysql sh -c 'mysql -uroot -p""$SERVER_MYSQL_PASSWORD""' < $IMPORT_FILE`""
Write-Host "Command: $importCmd" -ForegroundColor Gray

try {
    Invoke-Expression $importCmd
    Write-Host "OK Database imported successfully" -ForegroundColor Green
} catch {
    Write-Host "ERROR Import failed: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Verify import
Write-Host "Verifying import..." -ForegroundColor Cyan
$verifyCmd = "ssh ${SERVER_USER}@${SERVER_IP} `"cd /opt/gacha-system; docker exec gacha-mysql sh -c 'mysql -uroot -p""$SERVER_MYSQL_PASSWORD""' -e 'USE $SERVER_DB_NAME; SHOW TABLES;'`""

try {
    $tables = Invoke-Expression $verifyCmd
    Write-Host "OK Import verification successful" -ForegroundColor Green
    Write-Host ""
    Write-Host "Tables imported:" -ForegroundColor Yellow
    $tables | ForEach-Object { Write-Host "  $_" -ForegroundColor White }
} catch {
    Write-Host "WARNING Verification command failed, but data may be imported" -ForegroundColor Yellow
    Write-Host "  Manual check: ssh root@111.228.12.167" -ForegroundColor Gray
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  Database sync completed!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

# Cleanup
$cleanup = Read-Host "Delete temporary SQL files? (y/n)"
if ($cleanup -eq "y" -or $cleanup -eq "Y") {
    Remove-Item $EXPORT_FILE -ErrorAction SilentlyContinue
    Remove-Item $IMPORT_FILE -ErrorAction SilentlyContinue
    Write-Host "OK Temporary files deleted" -ForegroundColor Green
}

Write-Host ""
