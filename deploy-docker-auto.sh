#!/bin/bash
# ============================================
# 远程服务器Docker一键部署脚本（超详细版）
# 适用于技术小白，每步都有详细说明
# ============================================

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 打印带颜色的消息
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_step() {
    echo ""
    echo -e "${CYAN}========================================${NC}"
    echo -e "${CYAN}  步骤 $1: $2${NC}"
    echo -e "${CYAN}========================================${NC}"
    echo ""
}

# ============================================
# 主程序开始
# ============================================

echo ""
echo -e "${CYAN}╔════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║  游戏商城系统 - Docker一键部署工具    ║${NC}"
echo -e "${CYAN}║  专为技术小白设计的超详细部署脚本      ║${NC}"
echo -e "${CYAN}╚════════════════════════════════════════╝${NC}"
echo ""

# 检查是否为root用户
if [ "$EUID" -ne 0 ]; then 
    print_error "请使用root用户或sudo执行此脚本"
    print_info "使用方法: sudo bash $0"
    exit 1
fi

print_warning "此脚本将自动完成以下操作："
echo "  1. 安装Docker和Docker Compose"
echo "  2. 配置Docker镜像加速器"
echo "  3. 拉取项目代码"
echo "  4. 配置环境变量"
echo "  5. 开放防火墙端口"
echo "  6. 构建并启动所有服务"
echo ""
print_warning "请确保："
echo "  ✓ 服务器至少有8GB内存（推荐16GB）"
echo "  ✓ 服务器至少有20GB可用磁盘空间"
echo "  ✓ 你已准备好QQ邮箱授权码（用于邮件功能）"
echo ""

read -p "$(echo -e ${YELLOW}是否继续？(y/n): ${NC})" -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    print_info "已取消部署"
    exit 0
fi

# ============================================
# 步骤1: 检查并安装Docker
# ============================================
print_step "1/7" "检查并安装Docker环境"

if command -v docker &> /dev/null; then
    print_success "Docker已安装: $(docker --version)"
else
    print_warning "Docker未安装，正在安装..."
    curl -fsSL https://get.docker.com | bash
    
    if [ $? -eq 0 ]; then
        print_success "Docker安装成功"
    else
        print_error "Docker安装失败，请检查网络连接"
        exit 1
    fi
fi

# 启动Docker服务
systemctl start docker
systemctl enable docker
print_success "Docker服务已启动并设置开机自启"

# ============================================
# 步骤2: 安装Docker Compose
# ============================================
print_step "2/7" "安装Docker Compose"

if command -v docker-compose &> /dev/null || docker compose version &> /dev/null; then
    print_success "Docker Compose已安装"
else
    print_warning "Docker Compose未安装，正在安装..."
    
    # 尝试使用包管理器安装
    if command -v apt &> /dev/null; then
        apt update && apt install -y docker-compose-plugin
    elif command -v yum &> /dev/null; then
        yum install -y docker-compose-plugin
    else
        # 从GitHub下载
        curl -L "https://github.com/docker/compose/releases/download/v2.29.1/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
        chmod +x /usr/local/bin/docker-compose
    fi
    
    if [ $? -eq 0 ]; then
        print_success "Docker Compose安装成功"
    else
        print_error "Docker Compose安装失败"
        exit 1
    fi
fi

# ============================================
# 步骤3: 配置Docker镜像加速器
# ============================================
print_step "3/7" "配置Docker镜像加速器"

print_info "创建Docker配置文件..."
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
print_success "Docker镜像加速器配置完成"

# ============================================
# 步骤4: 准备项目目录和代码
# ============================================
print_step "4/7" "准备项目代码"

PROJECT_DIR="/opt/gacha-system"

# 检查是否已存在旧版本
if [ -d "$PROJECT_DIR" ]; then
    print_warning "检测到旧版本项目目录"
    read -p "$(echo -e ${YELLOW}是否备份旧版本？(y/n): ${NC})" -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        BACKUP_DIR="${PROJECT_DIR}.backup.$(date +%Y%m%d_%H%M%S)"
        mv $PROJECT_DIR $BACKUP_DIR
        print_success "旧版本已备份到: $BACKUP_DIR"
    fi
fi

# 创建项目目录
mkdir -p $PROJECT_DIR
cd $PROJECT_DIR

# 检查是否有.git目录
if [ -d ".git" ]; then
    print_info "更新现有代码..."
    git pull
else
    print_info "从GitHub克隆代码..."
    git clone https://github.com/hubuwei/gacha-system.git .
fi

if [ $? -eq 0 ]; then
    print_success "代码准备完成"
else
    print_error "代码拉取失败，请检查网络连接或Git配置"
    exit 1
fi

# ============================================
# 步骤5: 配置环境变量
# ============================================
print_step "5/7" "配置环境变量"

if [ ! -f ".env" ]; then
    cp .env.docker .env
    print_success "已创建.env配置文件"
    
    print_warning "重要：请编辑 .env 文件修改密码配置"
    print_info "至少需要修改以下配置："
    echo "  - DB_PASSWORD (数据库密码)"
    echo "  - REDIS_PASSWORD (Redis密码)"
    echo "  - RABBITMQ_PASSWORD (RabbitMQ密码)"
    echo "  - MAIL_PASSWORD (QQ邮箱授权码)"
    echo ""
    
    read -p "$(echo -e ${YELLOW}是否现在编辑 .env 文件？(y/n): ${NC})" -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        # 检测可用的编辑器
        if command -v nano &> /dev/null; then
            nano .env
        elif command -v vim &> /dev/null; then
            vim .env
        else
            print_error "未找到可用的文本编辑器，请手动编辑 .env 文件"
            print_info "命令: nano .env 或 vim .env"
            exit 1
        fi
    fi
else
    print_success ".env文件已存在"
    read -p "$(echo -e ${YELLOW}是否需要重新编辑？(y/n): ${NC})" -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        if command -v nano &> /dev/null; then
            nano .env
        elif command -v vim &> /dev/null; then
            vim .env
        fi
    fi
fi

# ============================================
# 步骤6: 配置防火墙
# ============================================
print_step "6/7" "配置防火墙规则"

print_warning "即将开放以下端口："
echo "  - 22 (SSH)"
echo "  - 80 (HTTP前端)"
echo "  - 8081-8084 (后端服务)"
echo "  - 3307 (MySQL)"
echo "  - 6379 (Redis)"
echo "  - 9200 (Elasticsearch)"
echo "  - 15672 (RabbitMQ管理)"
echo ""

read -p "$(echo -e ${YELLOW}是否配置防火墙？(y/n): ${NC})" -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    # 检测防火墙类型
    if command -v ufw &> /dev/null; then
        print_info "检测到UFW防火墙（Ubuntu）"
        
        ufw allow 22/tcp
        ufw allow 80/tcp
        ufw allow 8081:8084/tcp
        ufw allow 3307/tcp
        ufw allow 6379/tcp
        ufw allow 9200/tcp
        ufw allow 15672/tcp
        
        # 如果UFW未启用，询问是否启用
        if ! ufw status | grep -q "Status: active"; then
            read -p "$(echo -e ${YELLOW}是否启用UFW防火墙？(y/n): ${NC})" -n 1 -r
            echo
            if [[ $REPLY =~ ^[Yy]$ ]]; then
                echo "y" | ufw enable
                print_success "UFW防火墙已启用"
            fi
        else
            ufw reload
            print_success "UFW规则已更新"
        fi
        
    elif command -v firewall-cmd &> /dev/null; then
        print_info "检测到firewalld防火墙（CentOS）"
        
        firewall-cmd --permanent --add-port=22/tcp
        firewall-cmd --permanent --add-port=80/tcp
        firewall-cmd --permanent --add-port=8081-8084/tcp
        firewall-cmd --permanent --add-port=3307/tcp
        firewall-cmd --permanent --add-port=6379/tcp
        firewall-cmd --permanent --add-port=9200/tcp
        firewall-cmd --permanent --add-port=15672/tcp
        firewall-cmd --reload
        
        print_success "firewalld规则已更新"
    else
        print_warning "未检测到防火墙，如果是云服务器，请在控制台配置安全组"
    fi
else
    print_info "跳过防火墙配置，请手动配置"
fi

# ============================================
# 步骤7: 停止旧服务并清理
# ============================================
print_step "7/7" "清理旧服务"

# 停止旧的JAR服务
print_info "检查并停止旧的JAR服务..."
pkill -f "mall-service.jar" 2>/dev/null || true
pkill -f "auth-service.jar" 2>/dev/null || true
pkill -f "game-service.jar" 2>/dev/null || true
pkill -f "gacha-service.jar" 2>/dev/null || true

# 停止旧的Docker容器
if [ -f "docker-compose.yml" ]; then
    print_info "停止旧的Docker容器..."
    docker-compose down 2>/dev/null || true
fi

print_success "旧服务已清理"

# ============================================
# 步骤8: 构建并启动服务
# ============================================
echo ""
echo -e "${CYAN}========================================${NC}"
echo -e "${CYAN}  开始构建和启动服务${NC}"
echo -e "${CYAN}========================================${NC}"
echo ""

print_warning "首次构建需要5-15分钟，请耐心等待..."
print_info "你可以喝杯咖啡休息一下 ☕"
echo ""

read -p "$(echo -e ${YELLOW}按回车键开始构建...${NC})" -r

# 构建镜像
print_info "正在构建Docker镜像..."
docker-compose build

if [ $? -eq 0 ]; then
    print_success "镜像构建成功"
else
    print_error "镜像构建失败"
    print_info "常见原因："
    echo "  1. 网络连接问题"
    echo "  2. Elasticsearch镜像下载失败"
    echo ""
    print_info "如果ES下载失败，可以尝试："
    echo "  1. 临时移除镜像加速器直接下载ES"
    echo "  2. 或者暂时注释掉ES服务（不影响核心功能）"
    echo ""
    read -p "$(echo -e ${YELLOW}是否查看错误详情？(y/n): ${NC})" -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        docker-compose logs
    fi
    exit 1
fi

# 启动服务
print_info "正在启动所有服务..."
docker-compose up -d

print_info "等待服务启动（30秒）..."
sleep 30

# ============================================
# 验证部署结果
# ============================================
echo ""
echo -e "${CYAN}========================================${NC}"
echo -e "${CYAN}  验证部署结果${NC}"
echo -e "${CYAN}========================================${NC}"
echo ""

print_info "检查容器状态..."
docker-compose ps

echo ""
print_info "测试API接口..."

# 测试Mall Service
if curl -s http://localhost:8081/api/actuator/health > /dev/null 2>&1; then
    print_success "✓ Mall Service 运行正常"
else
    print_error "✗ Mall Service 启动失败，请查看日志: docker-compose logs mall-service"
fi

# 测试Auth Service
if curl -s http://localhost:8084/actuator/health > /dev/null 2>&1; then
    print_success "✓ Auth Service 运行正常"
else
    print_error "✗ Auth Service 启动失败，请查看日志: docker-compose logs auth-service"
fi

# 测试Game Service
if curl -s http://localhost:8082/actuator/health > /dev/null 2>&1; then
    print_success "✓ Game Service 运行正常"
else
    print_error "✗ Game Service 启动失败，请查看日志: docker-compose logs game-service"
fi

# 测试Gacha Service
if curl -s http://localhost:8083/actuator/health > /dev/null 2>&1; then
    print_success "✓ Gacha Service 运行正常"
else
    print_error "✗ Gacha Service 启动失败，请查看日志: docker-compose logs gacha-service"
fi

# ============================================
# 显示部署结果
# ============================================
echo ""
echo -e "${GREEN}╔════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║          部署完成！🎉                  ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════╝${NC}"
echo ""

# 获取服务器IP
SERVER_IP=$(curl -s ifconfig.me 2>/dev/null || echo "你的服务器IP")

echo -e "${CYAN}服务访问地址：${NC}"
echo -e "  前端页面:      ${GREEN}http://${SERVER_IP}${NC}"
echo -e "  Mall API:      ${GREEN}http://${SERVER_IP}:8081/api${NC}"
echo -e "  Auth API:      ${GREEN}http://${SERVER_IP}:8084/api/auth${NC}"
echo -e "  Game API:      ${GREEN}http://${SERVER_IP}:8082/api/game${NC}"
echo -e "  Gacha API:     ${GREEN}http://${SERVER_IP}:8083/api/gacha${NC}"
echo -e "  RabbitMQ管理:  ${GREEN}http://${SERVER_IP}:15672${NC}"
echo -e "  MySQL:         ${GREEN}${SERVER_IP}:3307${NC}"
echo -e "  Redis:         ${GREEN}${SERVER_IP}:6379${NC}"
echo -e "  Elasticsearch: ${GREEN}http://${SERVER_IP}:9200${NC}"
echo ""

echo -e "${CYAN}常用维护命令：${NC}"
echo -e "  查看所有容器:    ${BLUE}docker-compose ps${NC}"
echo -e "  查看日志:        ${BLUE}docker-compose logs -f [服务名]${NC}"
echo -e "  重启服务:        ${BLUE}docker-compose restart [服务名]${NC}"
echo -e "  停止服务:        ${BLUE}docker-compose down${NC}"
echo -e "  更新部署:        ${BLUE}cd /opt/gacha-system && git pull && docker-compose up -d${NC}"
echo ""

echo -e "${YELLOW}注意事项：${NC}"
echo "  1. 如果某些服务启动失败，请查看日志排查问题"
echo "  2. 确保云服务器安全组已开放相应端口"
echo "  3. 建议定期备份数据库"
echo "  4. 查看详细文档: 远程服务器Docker部署-超详细指南.md"
echo ""

print_success "部署完成！祝你使用愉快！🚀"
echo ""
