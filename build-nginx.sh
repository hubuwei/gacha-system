#!/bin/bash
# Nginx 持久化配置构建和部署脚本

set -e

echo "=========================================="
echo "  构建和部署持久化 Nginx 配置"
echo "=========================================="

# 1. 准备前端构建文件
echo ""
echo "步骤 1/6: 准备前端构建文件..."
if [ ! -d "game-mall/dist" ]; then
    echo "❌ game-mall/dist 目录不存在，请先构建前端"
    echo "运行: cd game-mall && npm run build"
    exit 1
fi

# 临时复制 dist 到构建上下文（避免 .dockerignore 排除）
cp -r game-mall/dist ./nginx-dist

# 2. 构建自定义 Nginx 镜像
echo ""
echo "步骤 2/6: 构建自定义 Nginx 镜像..."
docker build -t gacha-nginx:with-config -f Dockerfile.nginx .

# 2. 标记版本
echo ""
echo "步骤 3/6: 标记镜像版本..."
docker tag gacha-nginx:with-config gacha-nginx:v1

# 3. 停止旧容器
echo ""
echo "步骤 4/6: 停止旧 Nginx 容器..."
docker stop gacha-frontend || true
docker rm gacha-frontend || true

# 4. 启动新容器
echo ""
echo "步骤 5/6: 启动新的 Nginx 容器..."
docker run -d \
  --name gacha-frontend \
  --restart always \
  -p 80:80 \
  -v /opt/gacha-system/GamePapers:/opt/gacha-system/GamePapers \
  --network gacha-network \
  gacha-nginx:v1

# 5. 清理临时文件
echo ""
echo "步骤 6/6: 清理临时文件..."
rm -rf ./nginx-dist

# 6. 等待并验证
echo ""
echo "验证 Nginx 启动..."
sleep 5

# 检查容器状态
if docker ps | grep -q gacha-frontend; then
    echo "✅ Nginx 容器启动成功！"
    
    # 测试配置
    echo ""
    echo "验证 Nginx 配置..."
    if docker exec gacha-frontend nginx -t 2>&1 | grep -q "successful"; then
        echo "✅ Nginx 配置验证通过"
    else
        echo "❌ Nginx 配置验证失败"
        docker exec gacha-frontend nginx -t
        exit 1
    fi
    
    # 测试 API 连接
    echo ""
    echo "测试 API 连接..."
    sleep 3
    if curl -s http://localhost/api/games/all-with-tags | grep -q '"code":200'; then
        echo "✅ API 连接测试通过"
    else
        echo "⚠️  API 连接测试失败，请检查后端服务是否正常运行"
    fi
    
    echo ""
    echo "=========================================="
    echo "  ✅ Nginx 持久化配置完成！"
    echo "=========================================="
    echo ""
    echo "配置特点："
    echo "  • 使用 upstream 块实现动态 DNS 解析"
    echo "  • 即使后端容器重启，Nginx 也能自动重新连接"
    echo "  • 配置文件已打包到镜像中，不会丢失"
    echo "  • GamePapers 目录通过 volume 挂载持久化"
    echo ""
else
    echo "❌ Nginx 容器启动失败！"
    docker logs gacha-frontend
    exit 1
fi
