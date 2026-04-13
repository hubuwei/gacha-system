#!/bin/bash
# ============================================
# Gacha System 服务器部署脚本
# 适用于: 4C16G100G 服务器
# ============================================

set -e

echo "=========================================="
echo "  Gacha System 部署脚本"
echo "=========================================="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 配置变量
DEPLOY_DIR="/opt/gacha-system"
BACKUP_DIR="/opt/gacha-system-backup"
JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"

# 检查是否以 root 运行
if [ "$EUID" -ne 0 ]; then 
    echo -e "${RED}请使用 sudo 运行此脚本${NC}"
    exit 1
fi

# 1. 创建部署目录
echo -e "${YELLOW}[1/8] 创建部署目录...${NC}"
mkdir -p $DEPLOY_DIR/{auth,mall,gacha,game,logs,config,uploads}
mkdir -p $DEPLOY_DIR/uploads/avatars
mkdir -p $DEPLOY_DIR/GamePapers

# 2. 安装必要软件
echo -e "${YELLOW}[2/8] 检查必要软件...${NC}"

# 检查 Java
if ! command -v java &> /dev/null; then
    echo -e "${YELLOW}安装 Java 11...${NC}"
    apt-get update && apt-get install -y openjdk-11-jdk
else
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
    echo -e "${GREEN}Java 已安装: $JAVA_VERSION${NC}"
fi

# 检查 Nginx
if ! command -v nginx &> /dev/null; then
    echo -e "${YELLOW}安装 Nginx...${NC}"
    apt-get install -y nginx
fi

# 3. 备份旧版本
echo -e "${YELLOW}[3/8] 备份旧版本...${NC}"
if [ -d "$DEPLOY_DIR/auth" ] && [ "$(ls -A $DEPLOY_DIR/auth/*.jar 2>/dev/null)" ]; then
    mkdir -p $BACKUP_DIR/$(date +%Y%m%d_%H%M%S)
    cp -r $DEPLOY_DIR/* $BACKUP_DIR/$(date +%Y%m%d_%H%M%S)/ 2>/dev/null || true
    echo -e "${GREEN}备份完成${NC}"
fi

# 4. 停止旧服务
echo -e "${YELLOW}[4/8] 停止旧服务...${NC}"
systemctl stop gacha-auth.service 2>/dev/null || true
systemctl stop gacha-mall.service 2>/dev/null || true
systemctl stop gacha-gacha.service 2>/dev/null || true
systemctl stop gacha-game.service 2>/dev/null || true

# 等待服务完全停止
sleep 3

# 5. 上传新文件（需要手动执行）
echo -e "${YELLOW}[5/8] 请上传新的 JAR 包到以下目录:${NC}"
echo -e "  ${GREEN}$DEPLOY_DIR/auth/${NC}"
echo -e "  ${GREEN}$DEPLOY_DIR/mall/${NC}"
echo -e "  ${GREEN}$DEPLOY_DIR/gacha/${NC}"
echo -e "  ${GREEN}$DEPLOY_DIR/game/${NC}"
echo ""
read -p "上传完成后按回车继续..." 

# 6. 创建 systemd 服务文件
echo -e "${YELLOW}[6/8] 创建系统服务...${NC}"

# Auth Service
cat > /etc/systemd/system/gacha-auth.service <<EOF
[Unit]
Description=Gacha Auth Service
After=network.target mysql.service redis.service

[Service]
Type=simple
User=root
WorkingDirectory=$DEPLOY_DIR/auth
ExecStart=/usr/bin/java $JAVA_OPTS -jar $DEPLOY_DIR/auth/*.jar --spring.profiles.active=prod
Restart=on-failure
RestartSec=10
StandardOutput=append:$DEPLOY_DIR/logs/auth.log
StandardError=append:$DEPLOY_DIR/logs/auth-error.log

[Install]
WantedBy=multi-user.target
EOF

# Mall Service
cat > /etc/systemd/system/gacha-mall.service <<EOF
[Unit]
Description=Gacha Mall Service
After=network.target mysql.service redis.service rabbitmq.service elasticsearch.service

[Service]
Type=simple
User=root
WorkingDirectory=$DEPLOY_DIR/mall
ExecStart=/usr/bin/java $JAVA_OPTS -Xms1024m -Xmx1536m -jar $DEPLOY_DIR/mall/*.jar --spring.profiles.active=prod
Restart=on-failure
RestartSec=10
StandardOutput=append:$DEPLOY_DIR/logs/mall.log
StandardError=append:$DEPLOY_DIR/logs/mall-error.log

[Install]
WantedBy=multi-user.target
EOF

# Gacha Service
cat > /etc/systemd/system/gacha-gacha.service <<EOF
[Unit]
Description=Gacha Gacha Service
After=network.target mysql.service redis.service

[Service]
Type=simple
User=root
WorkingDirectory=$DEPLOY_DIR/gacha
ExecStart=/usr/bin/java $JAVA_OPTS -jar $DEPLOY_DIR/gacha/*.jar --spring.profiles.active=prod
Restart=on-failure
RestartSec=10
StandardOutput=append:$DEPLOY_DIR/logs/gacha.log
StandardError=append:$DEPLOY_DIR/logs/gacha-error.log

[Install]
WantedBy=multi-user.target
EOF

# Game Service
cat > /etc/systemd/system/gacha-game.service <<EOF
[Unit]
Description=Gacha Game Service
After=network.target mysql.service redis.service

[Service]
Type=simple
User=root
WorkingDirectory=$DEPLOY_DIR/game
ExecStart=/usr/bin/java $JAVA_OPTS -jar $DEPLOY_DIR/game/*.jar --spring.profiles.active=prod
Restart=on-failure
RestartSec=10
StandardOutput=append:$DEPLOY_DIR/logs/game.log
StandardError=append:$DEPLOY_DIR/logs/game-error.log

[Install]
WantedBy=multi-user.target
EOF

# 7. 配置 Nginx
echo -e "${YELLOW}[7/8] 配置 Nginx...${NC}"
cat > /etc/nginx/sites-available/gacha-system <<'EOF'
server {
    listen 80;
    server_name _;  # 替换为你的域名
    
    # 前端静态资源
    location / {
        root /opt/gacha-system/frontend;
        try_files $uri $uri/ /index.html;
        expires 1d;
        add_header Cache-Control "public, immutable";
    }
    
    # GamePapers 图片资源
    location /GamePapers/ {
        alias /opt/gacha-system/GamePapers/;
        expires 30d;
        add_header Cache-Control "public, immutable";
    }
    
    # API 反向代理
    location /api/auth/ {
        proxy_pass http://localhost:8080/api/auth/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 60s;
        proxy_read_timeout 60s;
    }
    
    location /api/ {
        proxy_pass http://localhost:8081/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 60s;
        proxy_read_timeout 60s;
    }
    
    # 上传文件
    client_max_body_size 10M;
}
EOF

# 启用站点
ln -sf /etc/nginx/sites-available/gacha-system /etc/nginx/sites-enabled/
nginx -t && systemctl restart nginx

# 8. 启动服务
echo -e "${YELLOW}[8/8] 启动服务...${NC}"

systemctl daemon-reload
systemctl enable gacha-auth.service
systemctl enable gacha-mall.service
systemctl enable gacha-gacha.service
systemctl enable gacha-game.service

systemctl start gacha-auth.service
sleep 5
systemctl start gacha-mall.service
sleep 5
systemctl start gacha-gacha.service
sleep 5
systemctl start gacha-game.service

# 检查服务状态
echo ""
echo -e "${GREEN}=========================================="
echo "  部署完成！"
echo "==========================================${NC}"
echo ""
echo "服务状态:"
systemctl status gacha-auth.service --no-pager -l | head -5
echo ""
systemctl status gacha-mall.service --no-pager -l | head -5
echo ""
systemctl status gacha-gacha.service --no-pager -l | head -5
echo ""
systemctl status gacha-game.service --no-pager -l | head -5
echo ""
echo -e "${YELLOW}查看日志:${NC}"
echo "  tail -f $DEPLOY_DIR/logs/auth.log"
echo "  tail -f $DEPLOY_DIR/logs/mall.log"
echo ""
echo -e "${YELLOW}访问地址:${NC}"
echo "  http://111.228.12.167"
echo ""
