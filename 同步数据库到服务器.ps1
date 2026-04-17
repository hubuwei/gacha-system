# ============================================
# 数据库同步脚本 - 从本地开发环境到服务器生产环境
# ============================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  数据库同步工具" -ForegroundColor Cyan
Write-Host "  本地 gacha_system_dev → 服务器 gacha_system_prod" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 配置信息
$LOCAL_MYSQL_USER = "root"
$LOCAL_MYSQL_PASSWORD = "123456"
$LOCAL_DB_NAME = "gacha_system_dev"
$SERVER_IP = "111.228.12.167"
$SERVER_USER = "root"
$SERVER_DB_NAME = "gacha_system_prod"
$SERVER_MYSQL_PASSWORD = "Xc037417!"
$EXPORT_FILE = "gacha_system_dev_export.sql"
$IMPORT_FILE = "gacha_system_prod_import.sql"

# 第1步：导出本地数据库
Write-Host "[步骤 1/4] 导出本地数据库..." -ForegroundColor Cyan
$mysqldumpCmd = "mysqldump -u$LOCAL_MYSQL_USER -p$LOCAL_MYSQL_PASSWORD --databases $LOCAL_DB_NAME --result-file=$EXPORT_FILE"
Write-Host "执行命令: $mysqldumpCmd" -ForegroundColor Gray

try {
    Invoke-Expression $mysqldumpCmd
    if (Test-Path $EXPORT_FILE) {
        Write-Host "✓ 数据库导出成功" -ForegroundColor Green
        $fileSize = (Get-Item $EXPORT_FILE).Length / 1MB
        Write-Host "  文件大小: $([math]::Round($fileSize, 2)) MB" -ForegroundColor Gray
    } else {
        Write-Host "✗ 导出失败，文件未生成" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "✗ 导出失败: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""

# 第2步：修改数据库名
Write-Host "[步骤 2/4] 修改数据库名为 gacha_system_prod..." -ForegroundColor Cyan

try {
    $content = Get-Content $EXPORT_FILE -Raw -Encoding UTF8
    $content = $content -replace "gacha_system_dev", "gacha_system_prod"
    $content | Set-Content $IMPORT_FILE -Encoding UTF8 -NoNewline
    Write-Host "✓ 数据库名修改完成" -ForegroundColor Green
} catch {
    Write-Host "✗ 修改失败: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""

# 第3步：上传到服务器
Write-Host "[步骤 3/4] 上传SQL文件到服务器..." -ForegroundColor Cyan
$scpCmd = "scp $IMPORT_FILE ${SERVER_USER}@${SERVER_IP}:/opt/gacha-system/"
Write-Host "执行命令: $scpCmd" -ForegroundColor Gray

try {
    Invoke-Expression $scpCmd
    Write-Host "✓ 文件上传成功" -ForegroundColor Green
} catch {
    Write-Host "✗ 上传失败: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""

# 第4步：在服务器上导入数据库
Write-Host "[步骤 4/4] 在服务器上导入数据库..." -ForegroundColor Cyan
$importCmd = "ssh ${SERVER_USER}@${SERVER_IP} `"cd /opt/gacha-system && docker exec -i gacha-mysql sh -c 'mysql -uroot -p\""$SERVER_MYSQL_PASSWORD\""' < $IMPORT_FILE`""
Write-Host "执行命令: $importCmd" -ForegroundColor Gray

try {
    Invoke-Expression $importCmd
    Write-Host "✓ 数据库导入成功" -ForegroundColor Green
} catch {
    Write-Host "✗ 导入失败: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""

# 验证导入结果
Write-Host "验证导入结果..." -ForegroundColor Cyan
$verifyCmd = "ssh ${SERVER_USER}@${SERVER_IP} `"cd /opt/gacha-system && docker exec gacha-mysql sh -c 'mysql -uroot -p\""$SERVER_MYSQL_PASSWORD\""' -e 'USE $SERVER_DB_NAME; SHOW TABLES;'`""

try {
    $tables = Invoke-Expression $verifyCmd
    Write-Host "✓ 导入验证成功" -ForegroundColor Green
    Write-Host ""
    Write-Host "已导入的表：" -ForegroundColor Yellow
    $tables | ForEach-Object { Write-Host "  $_" -ForegroundColor White }
} catch {
    Write-Host "⚠ 验证命令执行失败，但数据可能已导入" -ForegroundColor Yellow
    Write-Host "  请手动检查: ssh root@111.228.12.167" -ForegroundColor Gray
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  数据库同步完成！🎉" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

# 清理临时文件
$cleanup = Read-Host "是否删除临时SQL文件？(y/n)"
if ($cleanup -eq "y" -or $cleanup -eq "Y") {
    Remove-Item $EXPORT_FILE -ErrorAction SilentlyContinue
    Remove-Item $IMPORT_FILE -ErrorAction SilentlyContinue
    Write-Host "✓ 临时文件已删除" -ForegroundColor Green
}

Write-Host ""
