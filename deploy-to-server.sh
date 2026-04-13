#!/bin/bash
# ============================================
# 服务器一键部署脚本（在服务器上执行）
# 服务器: 111.228.12.167
# ============================================

set -e

echo "=========================================="
echo "游戏商城系统 - 服务器Docker部署"
echo "=========================================="
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 检查是否root
if [ "$EUID" -ne 0 ]; then 
    echo -e "${RED}请使用root用户或sudo执行${NC}"
    exit 1
fi

echo -e "${BLUE}步骤 1/8: 安装 Docker Compose...${NC}"
if ! command -v docker-compose &> /dev/null; then
    echo "下载 Docker Compose..."
    wget -O /usr/local/bin/docker-compose https://github.com/docker/compose/releases/download/v2.29.1/docker-compose-linux-x86_64 || {
        echo -e "${YELLOW}GitHub下载失败，尝试备用源...${NC}"
        curl -L "https://get.daocloud.io/docker/compose/releases/download/v2.29.1/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    }
    chmod +x /usr/local/bin/docker-compose
    echo -e "${GREEN}✓ Docker Compose 安装完成${NC}"
else
    echo -e "${GREEN}✓ Docker Compose 已安装${NC}"
fi
docker-compose version
echo ""

echo -e "${BLUE}步骤 2/8: 停止旧服务...${NC}"
cd /opt/gacha-system
if [ -f "restart-all-services.sh" ]; then
    echo "停止传统JAR服务..."
    pkill -f "mall-service.jar" || true
    pkill -f "auth-service" || true
    pkill -f "game-service" || true
    pkill -f "gacha-service" || true
    echo -e "${GREEN}✓ 旧服务已停止${NC}"
else
    echo -e "${YELLOW}⚠ 未检测到旧服务${NC}"
fi
echo ""

echo -e "${BLUE}步骤 3/8: 备份旧数据...${NC}"
if [ -d "/opt/gacha-system-old" ]; then
    echo -e "${YELLOW}⚠ 备份目录已存在，跳过备份${NC}"
else
    mkdir -p /opt/gacha-system-old
    cp -r GamePapers /opt/gacha-system-old/ 2>/dev/null || true
    cp -r uploads /opt/gacha-system-old/ 2>/dev/null || true
    cp .env /opt/gacha-system-old/ 2>/dev/null || true
    echo -e "${GREEN}✓ 数据已备份到 /opt/gacha-system-old${NC}"
fi
echo ""

echo -e "${BLUE}步骤 4/8: 更新项目代码...${NC}"
if [ -d ".git" ]; then
    echo "使用Git更新..."
    git pull origin master
else
    echo -e "${YELLOW}⚠ 非Git仓库，请手动上传新代码${NC}"
    echo -e "${YELLOW}提示: 从本地执行 scp -r E:\\CFDemo\\gacha-system\\* root@111.228.12.167:/opt/gacha-system/${NC}"
fi
echo ""

echo -e "${BLUE}步骤 5/8: 配置环境变量...${NC}"
if [ ! -f ".env" ]; then
    echo "创建.env文件..."
    cat > .env << 'EOF'
DB_PASSWORD=Xc037417!
DB_NAME=gacha_system_prod
REDIS_PASSWORD=Xc037417!
RABBITMQ_USERNAME=admin
RABBITMQ_PASSWORD=Xc037417!
MAIL_USERNAME=1712133303@qq.com
MAIL_PASSWORD=wkokvlcppuuaehhd
EOF
    echo -e "${GREEN}✓ .env文件已创建${NC}"
    echo -e "${YELLOW}⚠️  请根据需要修改 .env 文件中的密码${NC}"
else
    echo -e "${GREEN}✓ .env文件已存在${NC}"
fi
echo ""

echo -e "${BLUE}步骤 6/8: 构建Docker镜像...${NC}"
echo "这可能需要5-10分钟，请耐心等待..."
docker-compose build --no-cache
echo -e "${GREEN}✓ 镜像构建完成${NC}"
echo ""

echo -e "${BLUE}步骤 7/8: 启动Docker容器...${NC}"
docker-compose up -d
echo -e "${GREEN}✓ 容器已启动${NC}"
echo ""

echo -e "${BLUE}步骤 8/8: 等待服务就绪...${NC}"
sleep 15
docker-compose ps
echo ""

echo "=========================================="
echo -e "${GREEN}部署完成！${NC}"
echo "=========================================="
echo ""
echo "访问地址:"
echo -e "  ${GREEN}前端: http://111.228.12.167${NC}"
echo -e "  ${GREEN}API:  http://111.228.12.167:8081/api${NC}"
echo -e "  ${GREEN}RabbitMQ管理: http://111.228.12.167:15672 (admin/Xc037417!)${NC}"
echo ""
echo "常用命令:"
echo "  查看状态: docker-compose ps"
echo "  查看日志: docker-compose logs -f [service-name]"
echo "  重启服务: docker-compose restart [service-name]"
echo "  停止服务: docker-compose down"
echo ""
echo -e "${YELLOW}提示: 如果访问不了，请检查防火墙和安全组是否开放端口 80, 8081-8084${NC}"
echo ""
