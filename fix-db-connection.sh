#!/bin/bash
# ============================================
# 修复服务器数据库连接问题
# 为 systemd 服务添加环境变量配置
# ============================================

set -e

echo "=========================================="
echo "  修复数据库连接配置"
echo "=========================================="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查是否以 root 运行
if [ "$EUID" -ne 0 ]; then 
    echo -e "${RED}请使用 sudo 运行此脚本${NC}"
    exit 1
fi

DEPLOY_DIR="/opt/gacha-system"

# 1. 创建 .env 文件
echo -e "${YELLOW}[1/4] 创建 .env 配置文件...${NC}"

cat > $DEPLOY_DIR/.env <<EOF
# 数据库配置
DB_HOST=localhost
DB_PORT=3306
DB_USERNAME=root
DB_PASSWORD=Xc037417!
DB_NAME=gacha_system_prod
DB_USE_SSL=false
DB_TIMEZONE=Asia/Shanghai
DB_ENCODING=utf-8

# JPA 配置
JPA_DDL_AUTO=update
JPA_SHOW_SQL=false
JPA_DIALECT=org.hibernate.dialect.MySQL5InnoDBDialect

# Redis 配置
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=Xc037417!
REDIS_DATABASE=0
REDIS_TIMEOUT=5000ms

# Elasticsearch 配置
ES_HOST=localhost
ES_PORT=9200
ES_SCHEME=http

# RabbitMQ 配置
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=admin
RABBITMQ_PASSWORD=Xc037417!

# 应用配置
APP_PORT=8082
LOG_LEVEL_ROOT=INFO
LOG_LEVEL_GAME=INFO
LOG_LEVEL_COMMON=INFO
LOG_LEVEL_SPRING_WEB=WARN
LOG_LEVEL_CORS=WARN
LOG_LEVEL_HIBERNATE_SQL=WARN
EOF

echo -e "${GREEN}.env 文件创建完成${NC}"

# 2. 更新 gacha-game.service
echo -e "${YELLOW}[2/4] 更新 gacha-game.service...${NC}"

cat > /etc/systemd/system/gacha-game.service <<EOF
[Unit]
Description=Gacha Game Service
After=network.target mysql.service redis.service

[Service]
Type=simple
User=root
WorkingDirectory=$DEPLOY_DIR/game
EnvironmentFile=$DEPLOY_DIR/.env
ExecStart=/usr/bin/java -Xms512m -Xmx1024m -XX:+UseG1GC -jar $DEPLOY_DIR/game/*.jar --spring.profiles.active=prod
Restart=on-failure
RestartSec=10
StandardOutput=append:$DEPLOY_DIR/logs/game.log
StandardError=append:$DEPLOY_DIR/logs/game-error.log

[Install]
WantedBy=multi-user.target
EOF

echo -e "${GREEN}gacha-game.service 更新完成${NC}"

# 3. 重新加载并重启服务
echo -e "${YELLOW}[3/4] 重新加载 systemd 配置...${NC}"
systemctl daemon-reload

echo -e "${YELLOW}[4/4] 重启 gacha-game 服务...${NC}"
systemctl restart gacha-game.service

# 等待服务启动
sleep 5

# 4. 检查服务状态
echo ""
echo -e "${GREEN}=========================================="
echo "  服务状态检查"
echo "==========================================${NC}"
echo ""
systemctl status gacha-game.service --no-pager -l | head -15

echo ""
echo -e "${YELLOW}查看最新日志:${NC}"
echo "  tail -f $DEPLOY_DIR/logs/game.log"
echo ""
echo -e "${YELLOW}查看错误日志:${NC}"
echo "  tail -f $DEPLOY_DIR/logs/game-error.log"
echo ""
echo -e "${GREEN}修复完成！请检查上述服务状态和日志确认是否正常启动${NC}"
