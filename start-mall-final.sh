#!/bin/bash
# ============================================
# Mall Service 启动脚本（最终修复版）
# ============================================

echo "======================================"
echo "  启动 Mall Service"
echo "======================================"

SERVICE_DIR="/opt/gacha-system/mall"
JAR_FILE="/opt/gacha-system/mall/mall-service.jar"
LOG_DIR="/opt/gacha-system/logs"
PID_FILE="/opt/gacha-system/mall/mall-service.pid"

# 检查 JAR 文件
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ 错误: 找不到 JAR 文件"
    exit 1
fi
echo "✓ JAR 文件找到"

# 清理旧进程
if [ -f "$PID_FILE" ]; then
    OLD_PID=$(cat "$PID_FILE")
    if ps -p "$OLD_PID" > /dev/null 2>&1; then
        echo "停止旧进程: $OLD_PID"
        kill -9 "$OLD_PID"
        sleep 2
    fi
    rm -f "$PID_FILE"
fi

mkdir -p "$LOG_DIR"

echo ""
echo "启动服务..."
echo ""

cd "$SERVICE_DIR"

# 使用命令行参数传递配置（最可靠的方式）
nohup java \
  -Xms512m \
  -Xmx1024m \
  -jar \
  "$JAR_FILE" \
  --spring.profiles.active=prod \
  "--spring.datasource.url=jdbc:mysql://localhost:3306/gacha_system_prod?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true" \
  --spring.datasource.username=root \
  "--spring.datasource.password=Xc037417!" \
  --spring.redis.host=localhost \
  --spring.redis.port=6379 \
  "--spring.redis.password=Xc037417!" \
  --spring.rabbitmq.host=localhost \
  --spring.rabbitmq.port=5672 \
  --spring.rabbitmq.username=admin \
  "--spring.rabbitmq.password=Xc037417!" \
  --spring.elasticsearch.uris=http://localhost:9200 \
  --spring.main.allow-bean-definition-overriding=true \
  > "$LOG_DIR/mall-startup.log" 2>&1 &

echo $! > "$PID_FILE"

echo "======================================"
echo "  启动完成！"
echo "======================================"
echo ""
echo "PID: $(cat $PID_FILE)"
echo "日志: $LOG_DIR/mall-startup.log"
echo ""
echo "等待 10 秒后查看状态..."
sleep 10

echo ""
echo "最近日志："
tail -30 "$LOG_DIR/mall-startup.log"
