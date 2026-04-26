#!/bin/bash

# 生产环境一键部署脚本
echo "=== Gacha System 生产环境部署 ==="

# 备份配置
echo "1. 备份配置文件..."
cp .env.prod .env.prod.bak

# 拉取最新代码
echo "2. 拉取最新代码..."
git pull origin master

# 停止现有服务
echo "3. 停止现有服务..."
docker-compose -f docker-compose.prod.yml down

# 清理旧文件
echo "4. 清理旧文件..."
rm -rf game-mall/dist cms-admin/dist

# 构建前端
echo "5. 构建前端..."
cd game-mall
npm install && npm run build
cd ../cms-admin
npm install && npm run build
cd ..

# 构建后端
echo "6. 构建后端..."
mvn clean package -Pprod -DskipTests

# 启动服务
echo "7. 启动服务..."
docker-compose -f docker-compose.prod.yml up -d

# 等待服务启动
echo "8. 等待服务启动..."
sleep 60

# 验证服务
echo "9. 验证服务状态..."
docker-compose -f docker-compose.prod.yml ps

# 测试 API
echo "10. 测试 API 接口..."
curl -s https://your-domain.com/api/actuator/health
curl -s https://your-domain.com/api/games/all-with-tags | head -50

# 同步游戏数据
echo "11. 同步游戏数据..."
curl -X POST https://your-domain.com/api/sync/games

echo "=== 部署完成 ==="
echo "生产环境地址: https://your-domain.com"