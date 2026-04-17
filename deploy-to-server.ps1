# ============================================
# 远程服务器自动化部署脚本
# 适用于 Windows PowerShell
# ============================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  游戏商城系统 - 远程自动化部署工具" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 服务器配置
$SERVER_IP = "111.228.12.167"
$SERVER_USER = "root"
$PROJECT_DIR = "/opt/gacha-system"

Write-Host "服务器信息:" -ForegroundColor Yellow
Write-Host "  IP地址: $SERVER_IP" -ForegroundColor White
Write-Host "  用户: $SERVER_USER" -ForegroundColor White
Write-Host "  项目目录: $PROJECT_DIR" -ForegroundColor White
Write-Host ""

# 第1步：测试SSH连接
Write-Host "[步骤 1/5] 测试SSH连接..." -ForegroundColor Cyan
try {
    ssh -o ConnectTimeout=5 "${SERVER_USER}@${SERVER_IP}" "echo 'SSH连接成功'" 2>&1 | Out-Null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ SSH连接正常" -ForegroundColor Green
    } else {
        Write-Host "✗ SSH连接失败，请检查：" -ForegroundColor Red
        Write-Host "  1. 服务器IP是否正确" -ForegroundColor Yellow
        Write-Host "  2. 服务器是否开机" -ForegroundColor Yellow
        Write-Host "  3. 防火墙是否允许SSH（端口22）" -ForegroundColor Yellow
        exit 1
    }
} catch {
    Write-Host "✗ SSH连接异常: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "即将执行以下操作：" -ForegroundColor Yellow
Write-Host "  1. 在服务器上创建项目目录" -ForegroundColor White
Write-Host "  2. 从GitHub拉取最新代码" -ForegroundColor White
Write-Host "  3. 配置环境变量" -ForegroundColor White
Write-Host "  4. 配置防火墙" -ForegroundColor White
Write-Host "  5. 构建Docker镜像（需要5-15分钟）" -ForegroundColor White
Write-Host "  6. 启动所有微服务" -ForegroundColor White
Write-Host ""

$confirm = Read-Host "是否继续？(y/n)"
if ($confirm -ne "y" -and $confirm -ne "Y") {
    Write-Host "已取消部署" -ForegroundColor Yellow
    exit 0
}

Write-Host ""
Write-Host "[步骤 2/5] 准备服务器环境..." -ForegroundColor Cyan

# 使用SSH执行远程命令
$commands = @"
set -e

# 创建项目目录
mkdir -p $PROJECT_DIR
cd $PROJECT_DIR

# 检查是否有旧版本
if [ -d ".git" ]; then
    echo "更新现有代码..."
    git pull
else
    echo "克隆代码仓库..."
    git clone https://github.com/hubuwei/gacha-system.git .
fi

echo "代码准备完成"
"@

Write-Host "正在拉取代码..." -ForegroundColor Yellow
ssh "${SERVER_USER}@${SERVER_IP}" $commands

if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ 代码拉取失败" -ForegroundColor Red
    exit 1
}

Write-Host "✓ 代码拉取成功" -ForegroundColor Green
Write-Host ""

# 第3步：配置环境变量
Write-Host "[步骤 3/5] 配置环境变量..." -ForegroundColor Cyan
Write-Host ""
Write-Host "重要：需要在服务器上配置 .env 文件" -ForegroundColor Yellow
Write-Host "请按以下步骤操作：" -ForegroundColor White
Write-Host ""
Write-Host "1. 连接到服务器：" -ForegroundColor Cyan
Write-Host "   ssh root@${SERVER_IP}" -ForegroundColor White
Write-Host ""
Write-Host "2. 进入项目目录：" -ForegroundColor Cyan
Write-Host "   cd $PROJECT_DIR" -ForegroundColor White
Write-Host ""
Write-Host "3. 复制环境配置文件：" -ForegroundColor Cyan
Write-Host "   cp .env.docker .env" -ForegroundColor White
Write-Host ""
Write-Host "4. 编辑配置文件：" -ForegroundColor Cyan
Write-Host "   nano .env" -ForegroundColor White
Write-Host ""
Write-Host "5. 修改以下配置：" -ForegroundColor Cyan
Write-Host "   - DB_PASSWORD (数据库密码)" -ForegroundColor White
Write-Host "   - REDIS_PASSWORD (Redis密码)" -ForegroundColor White
Write-Host "   - RABBITMQ_PASSWORD (RabbitMQ密码)" -ForegroundColor White
Write-Host "   - MAIL_PASSWORD (QQ邮箱授权码)" -ForegroundColor White
Write-Host ""

$envConfigured = Read-Host "是否已完成环境变量配置？(y/n)"
if ($envConfigured -ne "y" -and $envConfigured -ne "Y") {
    Write-Host ""
    Write-Host "提示：你可以稍后手动配置，然后继续部署" -ForegroundColor Yellow
    Write-Host "配置完成后，重新运行此脚本即可" -ForegroundColor Yellow
    exit 0
}

Write-Host ""

# 第4步：配置防火墙
Write-Host "[步骤 4/5] 配置防火墙..." -ForegroundColor Cyan

$firewallCommands = @"
# 配置UFW防火墙
ufw allow 22/tcp
ufw allow 80/tcp
ufw allow 8081:8084/tcp
ufw allow 3307/tcp
ufw allow 6379/tcp
ufw allow 9200/tcp
ufw allow 15672/tcp

# 如果UFW未启用，询问是否启用
if ! ufw status | grep -q "Status: active"; then
    echo "y" | ufw enable
fi

ufw reload
echo "防火墙配置完成"
"@

Write-Host "正在配置防火墙规则..." -ForegroundColor Yellow
ssh "${SERVER_USER}@${SERVER_IP}" $firewallCommands

if ($LASTEXITCODE -ne 0) {
    Write-Host "⚠ 防火墙配置可能失败，但不影响部署" -ForegroundColor Yellow
} else {
    Write-Host "✓ 防火墙配置成功" -ForegroundColor Green
}

Write-Host ""

# 第5步：执行自动化部署脚本
Write-Host "[步骤 5/5] 执行自动化部署..." -ForegroundColor Cyan
Write-Host ""
Write-Host "这将需要5-15分钟，请耐心等待..." -ForegroundColor Yellow
Write-Host "你可以喝杯咖啡休息一下 ☕" -ForegroundColor Yellow
Write-Host ""

$deployCommands = @"
cd $PROJECT_DIR

# 给脚本添加执行权限
chmod +x deploy-docker-auto.sh

# 执行部署脚本（非交互模式）
export DEBIAN_FRONTEND=noninteractive

# 停止旧服务
pkill -f "mall-service.jar" 2>/dev/null || true
pkill -f "auth-service.jar" 2>/dev/null || true
pkill -f "game-service.jar" 2>/dev/null || true
pkill -f "gacha-service.jar" 2>/dev/null || true
docker-compose down 2>/dev/null || true

# 构建镜像
echo "开始构建Docker镜像..."
docker-compose build

# 启动服务
echo "启动所有服务..."
docker-compose up -d

# 等待服务启动
echo "等待服务启动..."
sleep 30

# 验证部署
echo "验证部署结果..."
docker-compose ps

echo ""
echo "========================================"
echo "部署完成！"
echo "========================================"
echo ""
echo "访问地址："
echo "  前端: http://${SERVER_IP}"
echo "  Mall API: http://${SERVER_IP}:8081/api"
echo "  Auth API: http://${SERVER_IP}:8084/api/auth"
echo "  Game API: http://${SERVER_IP}:8082/api/game"
echo "  Gacha API: http://${SERVER_IP}:8083/api/gacha"
echo ""
"@

Write-Host "正在执行部署..." -ForegroundColor Yellow
Write-Host ""

# 执行部署（这会显示实时输出）
ssh -t "${SERVER_USER}@${SERVER_IP}" $deployCommands

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "  🎉 部署成功！" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "服务访问地址：" -ForegroundColor Cyan
    Write-Host "  前端页面:      http://${SERVER_IP}" -ForegroundColor White
    Write-Host "  Mall API:      http://${SERVER_IP}:8081/api" -ForegroundColor White
    Write-Host "  Auth API:      http://${SERVER_IP}:8084/api/auth" -ForegroundColor White
    Write-Host "  Game API:      http://${SERVER_IP}:8082/api/game" -ForegroundColor White
    Write-Host "  Gacha API:     http://${SERVER_IP}:8083/api/gacha" -ForegroundColor White
    Write-Host "  RabbitMQ管理:  http://${SERVER_IP}:15672" -ForegroundColor White
    Write-Host ""
    Write-Host "常用命令：" -ForegroundColor Cyan
    Write-Host "  查看容器状态:  ssh root@${SERVER_IP} 'docker-compose ps'" -ForegroundColor White
    Write-Host "  查看日志:      ssh root@${SERVER_IP} 'docker-compose logs -f [服务名]'" -ForegroundColor White
    Write-Host "  重启服务:      ssh root@${SERVER_IP} 'docker-compose restart [服务名]'" -ForegroundColor White
    Write-Host ""
} else {
    Write-Host ""
    Write-Host "⚠ 部署过程中可能出现了一些问题" -ForegroundColor Yellow
    Write-Host "请手动连接到服务器检查：" -ForegroundColor Yellow
    Write-Host "  ssh root@${SERVER_IP}" -ForegroundColor White
    Write-Host "  cd $PROJECT_DIR" -ForegroundColor White
    Write-Host "  docker-compose logs" -ForegroundColor White
}
