# ============================================
# 远程服务器部署 - 简化版
# ============================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  游戏商城系统 - 远程部署助手" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$SERVER_IP = "111.228.12.167"

Write-Host "请按照以下步骤手动执行部署：" -ForegroundColor Yellow
Write-Host ""

Write-Host "第1步：连接到服务器" -ForegroundColor Cyan
Write-Host "在PowerShell中执行：" -ForegroundColor White
Write-Host "  ssh root@$SERVER_IP" -ForegroundColor Green
Write-Host ""

Write-Host "第2步：创建项目目录并拉取代码" -ForegroundColor Cyan
Write-Host "在服务器上执行：" -ForegroundColor White
Write-Host "  mkdir -p /opt/gacha-system" -ForegroundColor Green
Write-Host "  cd /opt/gacha-system" -ForegroundColor Green
Write-Host "  git clone https://github.com/hubuwei/gacha-system.git ." -ForegroundColor Green
Write-Host ""

Write-Host "第3步：配置环境变量" -ForegroundColor Cyan
Write-Host "在服务器上执行：" -ForegroundColor White
Write-Host "  cp .env.docker .env" -ForegroundColor Green
Write-Host "  nano .env" -ForegroundColor Green
Write-Host ""
Write-Host "修改以下配置后保存（Ctrl+O, Enter, Ctrl+X）：" -ForegroundColor Yellow
Write-Host "  - DB_PASSWORD (数据库密码)" -ForegroundColor White
Write-Host "  - REDIS_PASSWORD (Redis密码)" -ForegroundColor White
Write-Host "  - RABBITMQ_PASSWORD (RabbitMQ密码)" -ForegroundColor White
Write-Host "  - MAIL_PASSWORD (QQ邮箱授权码)" -ForegroundColor White
Write-Host ""

Write-Host "第4步：配置防火墙" -ForegroundColor Cyan
Write-Host "在服务器上执行：" -ForegroundColor White
Write-Host "  ufw allow 22/tcp" -ForegroundColor Green
Write-Host "  ufw allow 80/tcp" -ForegroundColor Green
Write-Host "  ufw allow 8081:8084/tcp" -ForegroundColor Green
Write-Host "  ufw allow 3307/tcp" -ForegroundColor Green
Write-Host "  ufw allow 6379/tcp" -ForegroundColor Green
Write-Host "  ufw allow 9200/tcp" -ForegroundColor Green
Write-Host "  ufw allow 15672/tcp" -ForegroundColor Green
Write-Host "  echo 'y' | ufw enable" -ForegroundColor Green
Write-Host ""

Write-Host "第5步：构建并启动服务" -ForegroundColor Cyan
Write-Host "在服务器上执行：" -ForegroundColor White
Write-Host "  cd /opt/gacha-system" -ForegroundColor Green
Write-Host "  docker-compose build" -ForegroundColor Green
Write-Host "  （等待5-15分钟...）" -ForegroundColor Yellow
Write-Host "  docker-compose up -d" -ForegroundColor Green
Write-Host "  sleep 30" -ForegroundColor Green
Write-Host ""

Write-Host "第6步：验证部署" -ForegroundColor Cyan
Write-Host "在服务器上执行：" -ForegroundColor White
Write-Host "  docker-compose ps" -ForegroundColor Green
Write-Host "  docker-compose logs -f mall-service" -ForegroundColor Green
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "访问地址：" -ForegroundColor Cyan
Write-Host "  前端: http://$SERVER_IP" -ForegroundColor Green
Write-Host "  Mall API: http://$SERVER_IP`:8081/api" -ForegroundColor Green
Write-Host "  Auth API: http://$SERVER_IP`:8084/api/auth" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$ready = Read-Host "是否已准备好开始？按回车键将通过SSH连接服务器"

# 自动连接服务器
ssh "root@$SERVER_IP"
