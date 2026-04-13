#!/bin/bash
# ============================================
# Gacha System 全服务启动脚本
# ============================================

echo "======================================"
echo "  Gacha System 服务启动"
echo "======================================"
echo ""

# Step 1: 创建数据库用户
echo "📋 Step 1: 创建 MySQL 用户..."
mysql -u root -pXc037417! <<EOF
CREATE USER IF NOT EXISTS 'mall_user'@'localhost' IDENTIFIED BY 'Xc037417!';
GRANT ALL PRIVILEGES ON gacha_system_prod.* TO 'mall_user'@'localhost';
FLUSH PRIVILEGES;
EOF

if [ $? -eq 0 ]; then
    echo "✓ MySQL 用户创建成功"
else
    echo "⚠️  MySQL 用户可能已存在，继续..."
fi
echo ""

# Step 2: 清理旧进程
echo "📋 Step 2: 清理旧进程..."
pkill -f 'service.jar' 2>/dev/null
sleep 2
echo "✓ 旧进程已清理"
echo ""

# Step 3: 清理 PID 文件
echo "📋 Step 3: 清理 PID 文件..."
rm -f /opt/gacha-system/*/*.pid
rm -f /opt/gacha-system/*.pid
echo "✓ PID 文件已清理"
echo ""

# Step 4: 创建启动脚本
echo "📋 Step 4: 创建各服务启动配置..."

# Auth Service (8081)
cat > /opt/gacha-system/auth/start.sh << 'INNEREOF'
#!/bin/bash
cd /opt/gacha-system/auth
nohup java \
  -Xms256m \
  -Xmx512m \
  -jar auth-service.jar \
  --spring.profiles.active=prod \
  --spring.datasource.url="jdbc:mysql://localhost:3306/gacha_system_prod?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true" \
  --spring.datasource.username=mall_user \
  --spring.datasource.password="Xc037417!" \
  --spring.redis.host=localhost \
  --spring.redis.port=6379 \
  --spring.redis.password="Xc037417!" \
  --server.port=8081 \
  > /opt/gacha-system/logs/auth-startup.log 2>&1 &
echo $! > auth-service.pid
INNEREOF
chmod +x /opt/gacha-system/auth/start.sh

# Game Service (8082)
cat > /opt/gacha-system/game/start.sh << 'INNEREOF'
#!/bin/bash
cd /opt/gacha-system/game
nohup java \
  -Xms256m \
  -Xmx512m \
  -jar game-service.jar \
  --spring.profiles.active=prod \
  --spring.datasource.url="jdbc:mysql://localhost:3306/gacha_system_prod?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true" \
  --spring.datasource.username=mall_user \
  --spring.datasource.password="Xc037417!" \
  --spring.redis.host=localhost \
  --spring.redis.port=6379 \
  --spring.redis.password="Xc037417!" \
  --server.port=8082 \
  > /opt/gacha-system/logs/game-startup.log 2>&1 &
echo $! > game-service.pid
INNEREOF
chmod +x /opt/gacha-system/game/start.sh

# Gacha Service (8083)
cat > /opt/gacha-system/gacha/start.sh << 'INNEREOF'
#!/bin/bash
cd /opt/gacha-system/gacha
nohup java \
  -Xms256m \
  -Xmx512m \
  -jar gacha-service.jar \
  --spring.profiles.active=prod \
  --spring.datasource.url="jdbc:mysql://localhost:3306/gacha_system_prod?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true" \
  --spring.datasource.username=mall_user \
  --spring.datasource.password="Xc037417!" \
  --spring.redis.host=localhost \
  --spring.redis.port=6379 \
  --spring.redis.password="Xc037417!" \
  --spring.rabbitmq.host=localhost \
  --spring.rabbitmq.port=5672 \
  --spring.rabbitmq.username=admin \
  --spring.rabbitmq.password="Xc037417!" \
  --server.port=8083 \
  > /opt/gacha-system/logs/gacha-startup.log 2>&1 &
echo $! > gacha-service.pid
INNEREOF
chmod +x /opt/gacha-system/gacha/start.sh

# Mall Service (8084)
cat > /opt/gacha-system/mall/start.sh << 'INNEREOF'
#!/bin/bash
cd /opt/gacha-system/mall
nohup java \
  -Xms512m \
  -Xmx1024m \
  -jar mall-service.jar \
  --spring.profiles.active=prod \
  --spring.datasource.url="jdbc:mysql://localhost:3306/gacha_system_prod?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true" \
  --spring.datasource.username=mall_user \
  --spring.datasource.password="Xc037417!" \
  --spring.redis.host=localhost \
  --spring.redis.port=6379 \
  --spring.redis.password="Xc037417!" \
  --spring.rabbitmq.host=localhost \
  --spring.rabbitmq.port=5672 \
  --spring.rabbitmq.username=admin \
  --spring.rabbitmq.password="Xc037417!" \
  --spring.elasticsearch.uris=http://localhost:9200 \
  --spring.main.allow-bean-definition-overriding=true \
  --server.port=8084 \
  > /opt/gacha-system/logs/mall-startup.log 2>&1 &
echo $! > mall-service.pid
INNEREOF
chmod +x /opt/gacha-system/mall/start.sh

echo "✓ 启动脚本创建完成"
echo ""

# Step 5: 启动所有服务
echo "======================================"
echo "  开始启动服务"
echo "======================================"
echo ""

echo "🚀 启动 Auth Service (端口 8081)..."
bash /opt/gacha-system/auth/start.sh
echo "✓ Auth Service 启动中... (PID: $(cat /opt/gacha-system/auth/auth-service.pid))"
echo ""

sleep 3

echo "🚀 启动 Game Service (端口 8082)..."
bash /opt/gacha-system/game/start.sh
echo "✓ Game Service 启动中... (PID: $(cat /opt/gacha-system/game/game-service.pid))"
echo ""

sleep 3

echo "🚀 启动 Gacha Service (端口 8083)..."
bash /opt/gacha-system/gacha/start.sh
echo "✓ Gacha Service 启动中... (PID: $(cat /opt/gacha-system/gacha/gacha-service.pid))"
echo ""

sleep 3

echo "🚀 启动 Mall Service (端口 8084)..."
bash /opt/gacha-system/mall/start.sh
echo "✓ Mall Service 启动中... (PID: $(cat /opt/gacha-system/mall/mall-service.pid))"
echo ""

echo "======================================"
echo "  所有服务已启动！"
echo "======================================"
echo ""
echo "等待 30 秒让服务完全启动..."
sleep 10

echo ""
echo "======================================"
echo "  服务状态检查"
echo "======================================"
echo ""

# 检查进程
echo "📊 Java 进程状态："
ps aux | grep 'service.jar' | grep -v grep
echo ""

# 检查端口
echo "📊 端口监听状态："
netstat -tlnp | grep -E ':(808[1-4])' || ss -tlnp | grep -E ':(808[1-4])'
echo ""

# 检查日志（最近 10 行）
echo "📊 Auth Service 最新日志："
tail -10 /opt/gacha-system/logs/auth-startup.log 2>/dev/null || echo "日志文件不存在"
echo ""

echo "📊 Mall Service 最新日志："
tail -10 /opt/gacha-system/logs/mall-startup.log 2>/dev/null || echo "日志文件不存在"
echo ""

echo "======================================"
echo "  完成！"
echo "======================================"
echo ""
echo "📌 服务信息："
echo "  Auth Service:   http://111.228.12.167:8081"
echo "  Game Service:   http://111.228.12.167:8082"
echo "  Gacha Service:  http://111.228.12.167:8083"
echo "  Mall Service:   http://111.228.12.167:8084"
echo ""
echo "📌 查看实时日志："
echo "  tail -f /opt/gacha-system/logs/auth-startup.log"
echo "  tail -f /opt/gacha-system/logs/game-startup.log"
echo "  tail -f /opt/gacha-system/logs/gacha-startup.log"
echo "  tail -f /opt/gacha-system/logs/mall-startup.log"
echo ""
echo "📌 健康检查："
echo "  curl http://localhost:8081/actuator/health"
echo "  curl http://localhost:8082/actuator/health"
echo "  curl http://localhost:8083/actuator/health"
echo "  curl http://localhost:8084/actuator/health"
echo ""
