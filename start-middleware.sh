#!/bin/bash
# ============================================
# 启动服务器中间件服务（MySQL, Redis, RabbitMQ）
# ============================================

set -e

echo "=========================================="
echo "  启动中间件服务"
echo "=========================================="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

DEPLOY_DIR="/opt/gacha-system"

cd $DEPLOY_DIR

# 1. 检查 Docker Compose
echo -e "${YELLOW}[1/5] 检查 Docker Compose...${NC}"
if ! command -v docker compose &> /dev/null; then
    echo -e "${RED}错误: Docker Compose 未安装${NC}"
    exit 1
fi
echo -e "${GREEN}Docker Compose 已安装${NC}"

# 2. 停止现有的应用容器（避免连接问题）
echo -e "${YELLOW}[2/5] 停止现有应用容器...${NC}"
docker stop gacha-game-service gacha-auth-service gacha-mall-service gacha-gacha-service 2>/dev/null || true
echo -e "${GREEN}应用容器已停止${NC}"

# 3. 启动中间件服务
echo -e "${YELLOW}[3/5] 启动 MySQL, Redis, RabbitMQ...${NC}"
docker compose up -d mysql redis rabbitmq

# 等待中间件启动
echo -e "${YELLOW}等待中间件启动（30秒）...${NC}"
sleep 30

# 4. 检查中间件状态
echo -e "${YELLOW}[4/5] 检查中间件状态...${NC}"
docker ps | grep -E 'gacha-mysql|gacha-redis|gacha-rabbitmq'

# 5. 重启应用容器
echo -e "${YELLOW}[5/5] 重启应用容器...${NC}"
docker start gacha-game-service gacha-auth-service gacha-mall-service gacha-gacha-service

echo ""
echo -e "${GREEN}=========================================="
echo "  中间件启动完成！"
echo "==========================================${NC}"
echo ""
echo -e "${YELLOW}检查所有容器状态:${NC}"
docker ps

echo ""
echo -e "${YELLOW}查看 game-service 日志:${NC}"
echo "  docker logs -f gacha-game-service"
echo ""
echo -e "${GREEN}请等待 1-2 分钟后刷新浏览器测试${NC}"
