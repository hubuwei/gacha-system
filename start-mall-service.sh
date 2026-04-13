#!/bin/bash
# ============================================
# Mall Service 启动脚本
# ============================================

echo "======================================"
echo "  启动 Mall Service"
echo "======================================"

# 服务目录
SERVICE_DIR="/opt/gacha-system/mall"
JAR_FILE="/opt/gacha-system/mall/mall-service.jar"
LOG_DIR="/opt/gacha-system/logs"
PID_FILE="/opt/gacha-system/mall/mall-service.pid"

# 检查 JAR 文件是否存在
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ 错误: 找不到 JAR 文件: $JAR_FILE"
    echo ""
    echo "可用的 JAR 文件："
    find /opt/gacha-system -name "*.jar" -type f
    exit 1
fi

echo "✓ JAR 文件找到: $JAR_FILE"

# 检查是否有残留进程
if [ -f "$PID_FILE" ]; then
    OLD_PID=$(cat "$PID_FILE")
    if ps -p "$OLD_PID" > /dev/null 2>&1; then
        echo "⚠️  发现旧进程 PID: $OLD_PID，正在停止..."
        kill -9 "$OLD_PID"
        sleep 2
        rm -f "$PID_FILE"
        echo "✓ 旧进程已停止"
    else
        echo "✓ 清理残留的 PID 文件"
        rm -f "$PID_FILE"
    fi
fi

# 创建日志目录（如果不存在）
mkdir -p "$LOG_DIR"

# 启动服务
echo ""
echo "正在启动 Mall Service..."
echo "配置文件: application-prod.yml"
echo "日志文件: $LOG_DIR/mall-startup.log"
echo ""

cd "$SERVICE_DIR"

nohup java -jar \
  -Dspring.profiles.active=prod \
  -Xms512m \
  -Xmx1024m \
  "$JAR_FILE" > "$LOG_DIR/mall-startup.log" 2>&1 &

# 保存 PID
echo $! > "$PID_FILE"

echo "======================================"
echo "  Mall Service 启动命令已执行"
echo "======================================"
echo ""
echo "进程 PID: $(cat $PID_FILE)"
echo "日志文件: $LOG_DIR/mall-startup.log"
echo ""
echo "等待 30 秒让服务启动..."
sleep 5

echo ""
echo "查看启动日志（最近 20 行）："
echo "--------------------------------------"
tail -20 "$LOG_DIR/mall-startup.log"

echo ""
echo "======================================"
echo "  下一步操作"
echo "======================================"
echo ""
echo "1. 继续查看日志："
echo "   tail -f $LOG_DIR/mall-startup.log"
echo ""
echo "2. 检查服务状态："
echo "   ps aux | grep java"
echo "   netstat -tlnp | grep 8081"
echo ""
echo "3. 健康检查："
echo "   curl http://localhost:8081/api/actuator/health"
echo ""
