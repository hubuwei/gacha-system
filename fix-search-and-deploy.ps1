# ============================================
# 修复线上搜索功能并重新部署前端
# ============================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  修复线上搜索功能 - 前端重新部署" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$ErrorActionPreference = "Stop"

# 配置信息
$SERVER_IP = "111.228.12.167"
$SERVER_USER = "root"
$PROJECT_ROOT = $PSScriptRoot
$FRONTEND_DIR = Join-Path $PROJECT_ROOT "game-mall"
$DEPLOY_DIR = "/usr/share/nginx/html"

Write-Host "配置信息:" -ForegroundColor Yellow
Write-Host "  服务器IP: $SERVER_IP" -ForegroundColor White
Write-Host "  前端目录: $FRONTEND_DIR" -ForegroundColor White
Write-Host "  部署路径: $DEPLOY_DIR" -ForegroundColor White
Write-Host ""

# 第1步：测试SSH连接
Write-Host "[步骤 1/6] 测试SSH连接..." -ForegroundColor Cyan
try {
    ssh -o ConnectTimeout=5 "${SERVER_USER}@${SERVER_IP}" "echo 'SSH连接成功'" 2>&1 | Out-Null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ SSH连接正常" -ForegroundColor Green
    } else {
        Write-Host "✗ SSH连接失败，请检查网络连接和SSH配置" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "✗ SSH连接异常: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""

# 第2步：检查本地前端代码
Write-Host "[步骤 2/6] 检查本地前端代码..." -ForegroundColor Cyan

if (-not (Test-Path $FRONTEND_DIR)) {
    Write-Host "✗ 前端目录不存在: $FRONTEND_DIR" -ForegroundColor Red
    exit 1
}

$appJsxPath = Join-Path $FRONTEND_DIR "src\App.jsx"
if (-not (Test-Path $appJsxPath)) {
    Write-Host "✗ App.jsx 文件不存在" -ForegroundColor Red
    exit 1
}

Write-Host "✓ 前端代码存在" -ForegroundColor Green

# 检查搜索功能相关代码
$appContent = Get-Content $appJsxPath -Raw -Encoding UTF8
if ($appContent -match "search-suggestions") {
    Write-Host "✓ 搜索建议弹窗代码已实现" -ForegroundColor Green
} else {
    Write-Host "✗ 搜索建议弹窗代码未找到" -ForegroundColor Red
    exit 1
}

if ($appContent -match "fetchSearchSuggestions") {
    Write-Host "✓ 搜索建议API调用函数已实现" -ForegroundColor Green
} else {
    Write-Host "✗ 搜索建议API调用函数未找到" -ForegroundColor Red
    exit 1
}

if ($appContent -match "executeSearch") {
    Write-Host "✓ ES搜索执行函数已实现" -ForegroundColor Green
} else {
    Write-Host "✗ ES搜索执行函数未找到" -ForegroundColor Red
    exit 1
}

Write-Host ""

# 第3步：安装依赖并构建前端
Write-Host "[步骤 3/6] 构建前端项目..." -ForegroundColor Cyan
Write-Host "这可能需要几分钟时间，请耐心等待..." -ForegroundColor Yellow
Write-Host ""

Set-Location $FRONTEND_DIR

# 检查node_modules是否存在
if (-not (Test-Path "node_modules")) {
    Write-Host "检测到未安装依赖，正在安装..." -ForegroundColor Yellow
    npm install
    if ($LASTEXITCODE -ne 0) {
        Write-Host "✗ 依赖安装失败" -ForegroundColor Red
        exit 1
    }
    Write-Host "✓ 依赖安装完成" -ForegroundColor Green
    Write-Host ""
}

# 清理旧的构建文件
if (Test-Path "dist") {
    Write-Host "清理旧的构建文件..." -ForegroundColor Yellow
    Remove-Item "dist" -Recurse -Force
}

# 执行构建
Write-Host "开始构建生产版本..." -ForegroundColor Yellow
npm run build

if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ 前端构建失败" -ForegroundColor Red
    exit 1
}

Write-Host "✓ 前端构建成功" -ForegroundColor Green

# 检查构建输出
if (-not (Test-Path "dist")) {
    Write-Host "✗ 构建输出目录不存在" -ForegroundColor Red
    exit 1
}

$distFiles = Get-ChildItem "dist" -Recurse
Write-Host "   构建了 $($distFiles.Count) 个文件" -ForegroundColor Gray

Write-Host ""

# 第4步：备份服务器上的旧文件
Write-Host "[步骤 4/6] 备份服务器上的旧前端文件..." -ForegroundColor Cyan

$backupCommands = @"
cd $DEPLOY_DIR
if [ -d "assets" ] || [ -f "index.html" ]; then
    BACKUP_DIR="/tmp/frontend-backup-\$(date +%Y%m%d_%H%M%S)"
    mkdir -p \$BACKUP_DIR
    cp -r * \$BACKUP_DIR/ 2>/dev/null || true
    echo "备份完成: \$BACKUP_DIR"
else
    echo "没有旧文件需要备份"
fi
"@

ssh "${SERVER_USER}@${SERVER_IP}" $backupCommands
Write-Host "✓ 备份完成（如果有旧文件）" -ForegroundColor Green

Write-Host ""

# 第5步：上传新的前端文件到服务器
Write-Host "[步骤 5/6] 上传前端文件到服务器..." -ForegroundColor Cyan
Write-Host "这可能需要几分钟，取决于网络速度..." -ForegroundColor Yellow
Write-Host ""

# 先清空远程目录
$cleanCommands = @"
rm -rf $DEPLOY_DIR/*
mkdir -p $DEPLOY_DIR
"@

ssh "${SERVER_USER}@${SERVER_IP}" $cleanCommands

# 使用scp递归上传整个dist目录
$distPath = Join-Path $FRONTEND_DIR "dist\*"
Write-Host "正在上传文件..." -ForegroundColor Yellow

# 注意：scp需要使用正斜杠路径
$remotePath = "${SERVER_USER}@${SERVER_IP}:${DEPLOY_DIR}/"
scp -r "$FRONTEND_DIR/dist/*" $remotePath

if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ 文件上传失败" -ForegroundColor Red
    Write-Host "尝试使用另一种方法..." -ForegroundColor Yellow
    
    # 备选方案：压缩后上传
    Write-Host "压缩构建文件..." -ForegroundColor Yellow
    $zipFile = Join-Path $PROJECT_ROOT "frontend-dist.zip"
    Compress-Archive -Path "$FRONTEND_DIR\dist\*" -DestinationPath $zipFile -Force
    
    Write-Host "上传压缩包..." -ForegroundColor Yellow
    scp $zipFile "${SERVER_USER}@${SERVER_IP}:/tmp/"
    
    Write-Host "在服务器上解压..." -ForegroundColor Yellow
    $extractCommands = @"
rm -rf $DEPLOY_DIR/*
unzip -o /tmp/frontend-dist.zip -d $DEPLOY_DIR/
rm /tmp/frontend-dist.zip
chown -R www-data:www-data $DEPLOY_DIR 2>/dev/null || true
chmod -R 755 $DEPLOY_DIR
"@
    ssh "${SERVER_USER}@${SERVER_IP}" $extractCommands
    
    Remove-Item $zipFile -Force
} else {
    Write-Host "设置文件权限..." -ForegroundColor Yellow
    $permissionCommands = @"
chown -R www-data:www-data $DEPLOY_DIR 2>/dev/null || true
chmod -R 755 $DEPLOY_DIR
"@
    ssh "${SERVER_USER}@${SERVER_IP}" $permissionCommands
}

Write-Host "✓ 文件上传完成" -ForegroundColor Green

Write-Host ""

# 第6步：验证部署
Write-Host "[步骤 6/6] 验证部署结果..." -ForegroundColor Cyan

# 检查Nginx是否运行
$checkCommands = @"
echo "=== Nginx状态 ==="
systemctl status nginx | head -5

echo ""
echo "=== 部署文件列表 ==="
ls -lh $DEPLOY_DIR/ | head -10

echo ""
echo "=== 文件大小统计 ==="
du -sh $DEPLOY_DIR/
"@

ssh "${SERVER_USER}@${SERVER_IP}" $checkCommands

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  🎉 前端部署完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "访问地址：" -ForegroundColor Cyan
Write-Host "  前端页面: http://${SERVER_IP}" -ForegroundColor White
Write-Host ""
Write-Host "测试搜索功能：" -ForegroundColor Cyan
Write-Host "  1. 打开浏览器访问: http://${SERVER_IP}" -ForegroundColor White
Write-Host "  2. 在顶部搜索框输入游戏名称（如'侠盗'）" -ForegroundColor White
Write-Host "  3. 应该能看到搜索建议下拉弹窗" -ForegroundColor White
Write-Host "  4. 点击建议项或等待500ms自动搜索" -ForegroundColor White
Write-Host ""
Write-Host "如果搜索功能仍有问题，请检查：" -ForegroundColor Yellow
Write-Host "  1. 后端mall-service是否正常运行" -ForegroundColor White
Write-Host "     ssh root@${SERVER_IP} 'docker logs mall-service --tail 50'" -ForegroundColor Gray
Write-Host ""
Write-Host "  2. Elasticsearch是否正常运行" -ForegroundColor White
Write-Host "     ssh root@${SERVER_IP} 'curl http://localhost:9200'" -ForegroundColor Gray
Write-Host ""
Write-Host "  3. 搜索数据是否已同步到ES" -ForegroundColor White
Write-Host "     ssh root@${SERVER_IP} 'curl -X POST http://localhost:8081/api/sync/games'" -ForegroundColor Gray
Write-Host ""

# 返回项目根目录
Set-Location $PROJECT_ROOT

Write-Host "提示：按任意键退出..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
