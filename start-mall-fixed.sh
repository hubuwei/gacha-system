#!/bin/bash
# ============================================
# Mall Service 启动脚本（修复版）
# ============================================

echo "======================================"
echo "  启动 Mall Service"
echo "======================================"

# 服务目录
SERVICE_DIR="/opt/gacha-system/mall"
JAR_FILE="/opt/gacha-system/mall/mall-service.jar"
CONFIG_FILE="/opt/gacha-system/mall/application-prod.yml"
LOG_DIR="/opt/gacha-system/logs"
PID_FILE="/opt/gacha-system/mall/mall-service.pid"

# 检查 JAR 文件是否存在
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ 错误: 找不到 JAR 文件: $JAR_FILE"
    exit 1
fi

echo "✓ JAR 文件找到: $JAR_FILE"

# 检查配置文件
if [ -f "$CONFIG_FILE" ]; then
    echo "✓ 配置文件找到: $CONFIG_FILE"
    CONFIG_PARAM="--spring.config.location=file:$CONFIG_FILE"
else
    echo "⚠️  警告: 未找到配置文件，使用 JAR 包内默认配置"
    CONFIG_PARAM=""
fi

# 检查是否有残留进程
if [ -f "$PID_FILE" ]; then
    OLD_PID=$(cat "$PID_FILE")
    if ps -p "$OLD_PID" > /dev/null 2>&1; then
        echo "⚠️  发现旧进程 PID: $OLD_PID，正在停止..."
        kill -9 "$OLD_PID"
        sleep 2
        echo "✓ 旧进程已停止"
    fi
    rm -f "$PID_FILE"
fi

# 创建日志目录
mkdir -p "$LOG_DIR"

# 启动服务
echo ""
echo "正在启动 Mall Service..."
echo "配置: prod profile"
echo "日志: $LOG_DIR/mall-startup.log"
echo ""

cd "$SERVICE_DIR"

nohup java \
  -Xms512m \
  -Xmx1024m \
  -jar \
  "$JAR_FILE" \
  --spring.profiles.active=prod \
  $CONFIG_PARAM \
  > "$LOG_DIR/mall-startup.log" 2>&1 &

# 保存 PID
echo $! > "$PID_FILE"

echo "======================================"
echo "  Mall Service 启动命令已执行"
echo "======================================"
echo ""
echo "进程 PID: $(cat $PID_FILE)"
echo "日志文件: $LOG_DIR/mall-startup.log"
echo ""
echo "等待 5 秒查看启动状态..."
sleep 5

echo ""
echo "最近启动日志："
echo "--------------------------------------"
tail -30 "$LOG_DIR/mall-startup.log"

echo ""
echo "======================================"
echo "  下一步操作"
echo "======================================"
echo ""
echo "1. 继续查看实时日志："
echo "   tail -f $LOG_DIR/mall-startup.log"
echo ""
echo "2. 等待 30 秒后检查服务："
echo "   ps aux | grep mall-service"
echo "   netstat -tlnp | grep 8081"
echo "   curl http://localhost:8081/api/actuator/health"
echo ""
