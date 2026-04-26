#!/bin/bash

# 测试环境一键部署脚本
echo "=== Gacha System 测试环境部署 ==="

# 停止现有服务
echo "1. 停止现有服务..."
docker-compose -f docker-compose.test.yml down

# 清理旧文件
echo "2. 清理旧文件..."
rm -rf game-mall/dist cms-admin/dist

# 构建前端
echo "3. 构建前端..."
cd game-mall
npm install && npm run build
cd ../cms-admin
npm install && npm run build
cd ..

# 构建后端
echo "4. 构建后端..."
mvn clean package -Ptest -DskipTests

# 启动服务
echo "5. 启动服务..."
docker-compose -f docker-compose.test.yml up -d

# 等待服务启动
echo "6. 等待服务启动..."
sleep 30

# 验证服务
echo "7. 验证服务状态..."
docker-compose -f docker-compose.test.yml ps

# 测试 API
echo "8. 测试 API 接口..."
curl -s http://localhost:8081/api/actuator/health
curl -s http://localhost:8081/api/games/all-with-tags | head -50

echo "=== 部署完成 ==="
echo "测试环境地址: http://test.your-domain.com"