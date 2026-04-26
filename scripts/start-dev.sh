#!/bin/bash

# 开发环境一键启动脚本
echo "=== Gacha System 开发环境启动 ==="

# 启动数据库
echo "1. 启动 MySQL 服务..."
sudo systemctl start mysql

# 构建 common 模块
echo "2. 构建 common 模块..."
cd common
mvn clean install -DskipTests
cd ..

# 启动各服务
echo "3. 启动后端服务..."

# 启动 mall-service
cd mall-service
echo "   - 启动 mall-service..."
mvn spring-boot:run -Dspring-boot.run.profiles=dev > ../logs/mall-service.log 2>&1 &
cd ..

# 启动 cms-service
cd cms-service
echo "   - 启动 cms-service..."
mvn spring-boot:run -Dspring-boot.run.profiles=dev > ../logs/cms-service.log 2>&1 &
cd ..

# 启动前端
echo "4. 启动前端服务..."

# 启动 game-mall
cd game-mall
echo "   - 启动 game-mall..."
npm run dev > ../logs/game-mall.log 2>&1 &
cd ..

# 启动 cms-admin
cd cms-admin
echo "   - 启动 cms-admin..."
npm run dev > ../logs/cms-admin.log 2>&1 &
cd ..

echo "=== 启动完成 ==="
echo "访问地址:"
echo "  游戏商城: http://localhost:5173"
echo "  CMS后台: http://localhost:5174"
echo "  Mall API: http://localhost:8081/api"
echo "  CMS API: http://localhost:8085"
echo "  Actuator: http://localhost:8081/api/actuator"