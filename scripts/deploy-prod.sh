#!/bin/bash

echo "=== Gacha System 生产环境部署 ==="

echo "1. 拉取最新代码..."
cd /opt/gacha-system
git pull origin master

echo "2. 停止现有服务..."
docker compose stop auth-service game-service mall-service cms-service nginx

echo "3. 删除旧容器..."
docker compose rm -f auth-service game-service mall-service cms-service nginx

echo "4. 构建后端服务..."
docker compose build auth-service game-service mall-service cms-service

echo "5. 启动服务..."
docker compose up -d auth-service game-service mall-service cms-service nginx

echo "6. 等待服务启动..."
sleep 45

echo "7. 查看服务状态..."
docker compose ps

echo "8. 健康检查..."
curl -s http://localhost:8081/api/actuator/health || echo "mall-service 健康检查失败"
curl -s http://localhost:8084/api/auth/health || echo "auth-service 健康检查失败"

echo "9. 同步游戏数据到ES..."
curl -X POST http://localhost:8081/api/sync/games || echo "ES同步失败，请手动执行"

echo "=== 部署完成 ==="
echo "前端地址: http://111.228.12.167"
echo "管理后台: http://111.228.12.167/admin"