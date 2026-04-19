#!/bin/bash
# ============================================
# 修复 game-service 数据库连接问题
# 添加 allowPublicKeyRetrieval=true 参数
# ============================================

set -e

echo "=========================================="
echo "  修复 game-service 数据库连接"
echo "=========================================="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}停止 game-service...${NC}"
docker stop gacha-game-service

echo -e "${YELLOW}移除旧的 game-service 容器...${NC}"
docker rm gacha-game-service

echo -e "${YELLOW}使用正确的环境变量重新启动 game-service...${NC}"
cd /opt/gacha-system

# 从 docker-compose.yml 重新创建，但覆盖环境变量
docker run -d \
  --name gacha-game-service \
  --network gacha-system_gacha-network \
  -p 8082:8082 \
  -e DB_HOST=mysql \
  -e DB_PORT=3306 \
  -e DB_USERNAME=root \
  -e DB_PASSWORD='Xc037417!' \
  -e DB_NAME=gacha_system_prod \
  -e DB_USE_SSL=false \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://mysql:3306/gacha_system_prod?useSSL=false&serverTimezone=UTC&characterEncoding=utf-8&allowPublicKeyRetrieval=true" \
  --restart unless-stopped \
  gacha-system-game-service

echo -e "${GREEN}game-service 已重新启动${NC}"
echo -e "${YELLOW}等待 30 秒让服务启动...${NC}"
sleep 30

echo -e "${YELLOW}检查日志...${NC}"
docker logs --tail 30 gacha-game-service | grep -E '(Started|ERROR|Public Key)' || true

echo ""
echo -e "${GREEN}修复完成！请检查上述日志确认是否成功启动${NC}"
