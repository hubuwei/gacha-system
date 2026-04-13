#!/bin/bash
# ============================================
# 服务器一键Docker部署脚本（完整版）
# 适用于: Ubuntu/CentOS
# ============================================

set -e

echo "=========================================="
echo "游戏商城系统 - Docker容器化部署"
echo "=========================================="
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 检查是否root
if [ "$EUID" -ne 0 ]; then 
    echo -e "${RED}请使用root用户或sudo执行${NC}"
    exit 1
fi

echo -e "${BLUE}步骤 1/8: 检查Docker环境...${NC}"
if ! command -v docker &> /dev/null; then
    echo -e "${YELLOW}Docker未安装，开始安装...${NC}"
    curl -fsSL https://get.docker.com | bash
    systemctl start docker
    systemctl enable docker
else
    echo -e "${GREEN}✓ Docker已安装: $(docker --version)${NC}"
fi

if ! command -v docker-compose &> /dev/null; then
    echo -e "${YELLOW}Docker Compose未安装，开始安装...${NC}"
    curl -L "https://github.com/docker/compose/releases/download/v2.29.1/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose
else
    echo -e "${GREEN}✓ Docker Compose已安装: $(docker-compose --version)${NC}"
fi

echo ""
echo -e "${BLUE}步骤 2/8: 配置Docker镜像加速器...${NC}"
cat > /etc/docker/daemon.json <<EOF
{
  "registry-mirrors": [
    "https://docker.m.daocloud.io",
    "https://huecker.io",
    "https://dockerhub.timeweb.cloud",
    "https://registry.docker-cn.com"
  ],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  }
}
EOF
systemctl restart docker
echo -e "${GREEN}✓ Docker镜像加速器配置完成${NC}"

echo ""
echo -e "${BLUE}步骤 3/8: 准备项目目录...${NC}"
PROJECT_DIR="/opt/gacha-system"

if [ -d "$PROJECT_DIR" ]; then
    echo -e "${YELLOW}检测到旧版本，正在备份...${NC}"
    mv $PROJECT_DIR ${PROJECT_DIR}.backup.$(date +%Y%m%d_%H%M%S)
fi

mkdir -p $PROJECT_DIR
cd $PROJECT_DIR

echo -e "${YELLOW}从GitHub拉取最新代码...${NC}"
git clone https://github.com/hubuwei/gacha-system.git .
echo -e "${GREEN}✓ 代码拉取完成${NC}"

echo ""
echo -e "${BLUE}步骤 4/8: 配置环境变量...${NC}"
if [ ! -f ".env" ]; then
    cp .env.docker .env
    echo -e "${GREEN}✓ 已创建.env配置文件${NC}"
    echo -e "${YELLOW}请编辑 .env 文件修改密码配置:${NC}"
    echo -e "   nano .env"
    echo ""
    read -p "按回车继续（使用默认配置）..." 
else
    echo -e "${GREEN}✓ .env文件已存在${NC}"
fi

echo ""
echo -e "${BLUE}步骤 5/8: 停止旧服务并清理...${NC}"
# 停止旧的JAR服务
pkill -f "mall-service.jar" || true
pkill -f "auth-service.jar" || true
pkill -f "game-service.jar" || true
pkill -f "gacha-service.jar" || true

# 停止旧的Docker容器
docker-compose down 2>/dev/null || true
echo -e "${GREEN}✓ 旧服务已清理${NC}"

echo ""
echo -e "${BLUE}步骤 6/8: 构建Docker镜像...${NC}"
echo -e "${YELLOW}首次构建需要5-10分钟，请耐心等待...${NC}"
docker-compose build --no-cache
echo -e "${GREEN}✓ 镜像构建完成${NC}"

echo ""
echo -e "${BLUE}步骤 7/8: 启动所有服务...${NC}"
docker-compose up -d

echo ""
echo -e "${YELLOW}等待服务启动...${NC}"
sleep 15

echo ""
echo -e "${BLUE}步骤 8/8: 检查服务状态...${NC}"
docker-compose ps

echo ""
echo "=========================================="
echo -e "${GREEN}部署完成！${NC}"
echo "=========================================="
echo ""
echo "服务访问地址："
echo -e "  前端页面:    ${GREEN}http://你的服务器IP${NC}"
echo -e "  Mall API:    ${GREEN}http://你的服务器IP:8081/api${NC}"
echo -e "  Auth API:    ${GREEN}http://你的服务器IP:8084/api/auth${NC}"
echo -e "  Game API:    ${GREEN}http://你的服务器IP:8082/api/game${NC}"
echo -e "  Gacha API:   ${GREEN}http://你的服务器IP:8083/api/gacha${NC}"
echo -e "  RabbitMQ管理: ${GREEN}http://你的服务器IP:15672${NC}"
echo -e "  MySQL:       ${GREEN}你的服务器IP:3307${NC}"
echo -e "  Redis:       ${GREEN}你的服务器IP:6379${NC}"
echo ""
echo "常用命令："
echo -e "  查看日志:    ${BLUE}docker-compose logs -f [服务名]${NC}"
echo -e "  重启服务:    ${BLUE}docker-compose restart [服务名]${NC}"
echo -e "  停止服务:    ${BLUE}docker-compose down${NC}"
echo -e "  更新部署:    ${BLUE}./deploy-docker.sh update${NC}"
echo ""
echo -e "${YELLOW}注意事项:${NC}"
echo "  1. 如果Elasticsearch启动失败，可以暂时注释掉继续使用其他功能"
echo "  2. 确保防火墙开放了相应端口"
echo "  3. 数据库初始数据会自动导入"
echo ""
