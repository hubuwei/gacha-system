#!/bin/bash

echo "========================================="
echo "重启所有微服务（正确端口映射）"
echo "========================================="
echo ""
echo "端口映射："
echo "  - Mall Service:  8081"
echo "  - Game Service:  8082"
echo "  - Gacha Service: 8083"
echo "  - Auth Service:  8084"
echo ""

# Step 1: 停止所有服务
echo "Step 1: 停止所有服务..."
pkill -9 -f mall-service.jar
pkill -9 -f game-service.jar
pkill -9 -f gacha-service.jar
pkill -9 -f auth-service.jar
sleep 3
echo "✓ 所有服务已停止"
echo ""

# Step 2: 清理 PID 文件
echo "Step 2: 清理 PID 文件..."
rm -f /opt/gacha-system/mall/*.pid
rm -f /opt/gacha-system/game/*.pid
rm -f /opt/gacha-system/gacha/*.pid
rm -f /opt/gacha-system/auth/*.pid
echo "✓ PID 文件已清理"
echo ""

# Step 3: 启动 Mall Service (8081)
echo "Step 3: 启动 Mall Service (端口 8081)..."
cd /opt/gacha-system/mall
nohup java -Xms256m -Xmx512m -jar mall-service.jar \
  --spring.profiles.active=prod \
  --spring.config.additional-location=file:/opt/gacha-system/shared-config.yml \
  --server.port=8081 \
  > /opt/gacha-system/logs/mall-startup.log 2>&1 &
echo $! > mall-service.pid
echo "✓ Mall Service 已启动 (PID: $(cat mall-service.pid))"
sleep 5

# Step 4: 启动 Game Service (8082)
echo "Step 4: 启动 Game Service (端口 8082)..."
cd /opt/gacha-system/game
nohup java -Xms256m -Xmx512m -jar game-service.jar \
  --spring.profiles.active=prod \
  --spring.config.additional-location=file:/opt/gacha-system/shared-config.yml \
  --server.port=8082 \
  > /opt/gacha-system/logs/game-startup.log 2>&1 &
echo $! > game-service.pid
echo "✓ Game Service 已启动 (PID: $(cat game-service.pid))"
sleep 5

# Step 5: 启动 Gacha Service (8083)
echo "Step 5: 启动 Gacha Service (端口 8083)..."
cd /opt/gacha-system/gacha
nohup java -Xms256m -Xmx512m -jar gacha-service.jar \
  --spring.profiles.active=prod \
  --spring.config.additional-location=file:/opt/gacha-system/shared-config.yml \
  --server.port=8083 \
  > /opt/gacha-system/logs/gacha-startup.log 2>&1 &
echo $! > gacha-service.pid
echo "✓ Gacha Service 已启动 (PID: $(cat gacha-service.pid))"
sleep 5

# Step 6: 启动 Auth Service (8084)
echo "Step 6: 启动 Auth Service (端口 8084)..."
cd /opt/gacha-system/auth
nohup java -Xms256m -Xmx512m -jar auth-service.jar \
  --spring.profiles.active=prod \
  --spring.config.additional-location=file:/opt/gacha-system/shared-config.yml \
  --server.port=8084 \
  > /opt/gacha-system/logs/auth-startup.log 2>&1 &
echo $! > auth-service.pid
echo "✓ Auth Service 已启动 (PID: $(cat auth-service.pid))"
sleep 10

# Step 7: 验证服务状态
echo ""
echo "========================================="
echo "验证服务状态..."
echo "========================================="
ps aux | grep -E '(mall|game|gacha|auth)-service.jar' | grep -v grep

echo ""
echo "检查端口监听..."
netstat -tlnp | grep -E ':(8081|8082|8083|8084)'

echo ""
echo "========================================="
echo "重启完成！"
echo "========================================="
echo ""
echo "请执行以下命令重启 Nginx："
echo "  nginx -t && systemctl reload nginx"
echo ""
