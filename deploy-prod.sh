#!/bin/bash
# ============================================
# 生产环境快速部署脚本
# 使用方法: ./deploy-prod.sh
# ============================================

set -e  # 遇到错误立即退出

echo "=========================================="
echo "游戏商城系统 - 生产环境部署"
echo "=========================================="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 配置变量
DEPLOY_DIR="/opt/gacha-system"
BACKUP_DIR="/opt/gacha-system-backup-$(date +%Y%m%d_%H%M%S)"

# 检查是否以root运行
if [ "$EUID" -ne 0 ]; then 
    echo -e "${RED}请使用sudo运行此脚本${NC}"
    exit 1
fi

echo ""
echo -e "${YELLOW}步骤 1/7: 备份当前版本...${NC}"
if [ -d "$DEPLOY_DIR" ]; then
    cp -r "$DEPLOY_DIR" "$BACKUP_DIR"
    echo -e "${GREEN}✓ 备份完成: $BACKUP_DIR${NC}"
else
    echo -e "${YELLOW}⚠ 首次部署，跳过备份${NC}"
fi

echo ""
echo -e "${YELLOW}步骤 2/7: 更新代码...${NC}"
cd "$DEPLOY_DIR" || {
    echo -e "${RED}错误: 部署目录不存在${NC}"
    echo "请先运行: mkdir -p $DEPLOY_DIR && cd $DEPLOY_DIR && git clone <repository-url>"
    exit 1
}
git pull origin master
echo -e "${GREEN}✓ 代码更新完成${NC}"

echo ""
echo -e "${YELLOW}步骤 3/7: 检查配置文件...${NC}"
if [ ! -f ".env.prod" ]; then
    echo -e "${YELLOW}⚠ .env.prod 文件不存在，从模板创建...${NC}"
    cp .env.prod.example .env.prod
    echo -e "${RED}请编辑 .env.prod 文件，配置数据库密码等信息${NC}"
    echo "按任意键继续..."
    read -n 1
fi
echo -e "${GREEN}✓ 配置文件检查完成${NC}"

echo ""
echo -e "${YELLOW}步骤 4/7: 编译打包...${NC}"
mvn clean package -DskipTests
echo -e "${GREEN}✓ 编译完成${NC}"

echo ""
echo -e "${YELLOW}步骤 5/7: 检查GamePapers目录...${NC}"
if [ ! -d "GamePapers" ]; then
    echo -e "${RED}错误: GamePapers目录不存在${NC}"
    exit 1
fi
IMAGE_COUNT=$(ls GamePapers/*.jpg 2>/dev/null | wc -l)
echo -e "${GREEN}✓ GamePapers目录存在，图片数量: $IMAGE_COUNT${NC}"

echo ""
echo -e "${YELLOW}步骤 6/7: 重启服务...${NC}"

# 停止旧服务
echo "停止mall-service..."
systemctl stop mall-service 2>/dev/null || {
    echo "未找到systemd服务，尝试手动停止..."
    pkill -f "mall-service" || true
}

# 启动新服务
echo "启动mall-service..."
cd mall-service
nohup java -jar \
    --spring.profiles.active=prod \
    target/mall-service-1.0.0-SNAPSHOT.jar \
    > ../logs/mall-startup.log 2>&1 &

echo $! > ../mall-service.pid
echo -e "${GREEN}✓ 服务已启动 (PID: $!)${NC}"

echo ""
echo -e "${YELLOW}步骤 7/7: 检查服务状态...${NC}"
sleep 5

if ps -p $(cat ../mall-service.pid 2>/dev/null) > /dev/null 2>&1; then
    echo -e "${GREEN}✓ 服务运行正常${NC}"
    
    # 检查日志
    echo ""
    echo "最近日志:"
    tail -n 10 ../logs/mall-startup.log
else
    echo -e "${RED}✗ 服务启动失败，请检查日志${NC}"
    echo "查看完整日志: tail -f ../logs/mall-startup.log"
    exit 1
fi

echo ""
echo "=========================================="
echo -e "${GREEN}部署完成！${NC}"
echo "=========================================="
echo ""
echo "访问地址:"
echo "  前端: http://your-server-ip"
echo "  API:  http://your-server-ip/api"
echo ""
echo "常用命令:"
echo "  查看日志: tail -f $DEPLOY_DIR/logs/mall.log"
echo "  重启服务: systemctl restart mall-service"
echo "  停止服务: systemctl stop mall-service"
echo ""
echo -e "${YELLOW}提示: 如果使用了systemd，请运行:${NC}"
echo "  sudo systemctl daemon-reload"
echo "  sudo systemctl enable mall-service"
echo ""
