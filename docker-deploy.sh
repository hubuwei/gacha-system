#!/bin/bash
# ============================================
# Docker 一键部署脚本
# 使用方法: ./docker-deploy.sh
# ============================================

set -e

echo "=========================================="
echo "游戏商城系统 - Docker 部署"
echo "=========================================="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 检查Docker是否安装
if ! command -v docker &> /dev/null; then
    echo -e "${RED}错误: Docker未安装${NC}"
    echo "请先安装Docker: https://docs.docker.com/get-docker/"
    exit 1
fi

# 检查Docker Compose是否安装
if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    echo -e "${RED}错误: Docker Compose未安装${NC}"
    echo "请先安装Docker Compose: https://docs.docker.com/compose/install/"
    exit 1
fi

echo -e "${GREEN}✓ Docker环境检查通过${NC}"
echo ""

# 确定使用的docker-compose命令
if docker compose version &> /dev/null; then
    DOCKER_COMPOSE="docker compose"
else
    DOCKER_COMPOSE="docker-compose"
fi

# 检查.env文件
if [ ! -f ".env" ]; then
    echo -e "${YELLOW}⚠ .env文件不存在，从模板创建...${NC}"
    cp .env.docker .env
    echo -e "${RED}请编辑 .env 文件，修改密码等配置${NC}"
    echo ""
    read -p "按回车键继续..." 
fi

# 解析命令行参数
ACTION=${1:-up}

case $ACTION in
    up)
        echo -e "${BLUE}步骤 1/4: 构建镜像...${NC}"
        $DOCKER_COMPOSE build --no-cache
        
        echo ""
        echo -e "${BLUE}步骤 2/4: 启动服务...${NC}"
        $DOCKER_COMPOSE up -d
        
        echo ""
        echo -e "${BLUE}步骤 3/4: 等待服务启动...${NC}"
        sleep 10
        
        echo ""
        echo -e "${BLUE}步骤 4/4: 检查服务状态...${NC}"
        $DOCKER_COMPOSE ps
        
        echo ""
        echo "=========================================="
        echo -e "${GREEN}部署完成！${NC}"
        echo "=========================================="
        echo ""
        echo "访问地址:"
        echo -e "  ${GREEN}前端: http://localhost${NC}"
        echo -e "  ${GREEN}API:  http://localhost:8081/api${NC}"
        echo -e "  ${GREEN}RabbitMQ管理: http://localhost:15672 (admin/${RABBITMQ_PASSWORD})${NC}"
        echo ""
        echo "常用命令:"
        echo "  查看日志: docker-compose logs -f [service-name]"
        echo "  停止服务: docker-compose down"
        echo "  重启服务: docker-compose restart [service-name]"
        echo "  查看状态: docker-compose ps"
        echo ""
        ;;
        
    down)
        echo -e "${YELLOW}停止并删除所有容器...${NC}"
        $DOCKER_COMPOSE down
        echo -e "${GREEN}✓ 服务已停止${NC}"
        ;;
        
    restart)
        echo -e "${YELLOW}重启所有服务...${NC}"
        $DOCKER_COMPOSE restart
        echo -e "${GREEN}✓ 服务已重启${NC}"
        ;;
        
    logs)
        SERVICE=${2:-}
        if [ -z "$SERVICE" ]; then
            echo -e "${YELLOW}查看所有服务日志 (Ctrl+C退出)...${NC}"
            $DOCKER_COMPOSE logs -f
        else
            echo -e "${YELLOW}查看 $SERVICE 服务日志...${NC}"
            $DOCKER_COMPOSE logs -f $SERVICE
        fi
        ;;
        
    status)
        echo -e "${BLUE}服务状态:${NC}"
        $DOCKER_COMPOSE ps
        ;;
        
    rebuild)
        echo -e "${YELLOW}重新构建并启动...${NC}"
        $DOCKER_COMPOSE down
        $DOCKER_COMPOSE build --no-cache
        $DOCKER_COMPOSE up -d
        echo -e "${GREEN}✓ 重建完成${NC}"
        ;;
        
    *)
        echo "用法: $0 {up|down|restart|logs|status|rebuild}"
        echo ""
        echo "命令说明:"
        echo "  up      - 构建并启动所有服务（默认）"
        echo "  down    - 停止并删除所有容器"
        echo "  restart - 重启所有服务"
        echo "  logs    - 查看日志 (可指定服务名)"
        echo "  status  - 查看服务状态"
        echo "  rebuild - 重新构建并启动"
        exit 1
        ;;
esac
