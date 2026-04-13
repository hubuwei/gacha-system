#!/bin/bash
# ============================================
# Gacha System 端口问题自动诊断脚本
# ============================================

echo "======================================"
echo "  Gacha System 端口诊断脚本"
echo "======================================"
echo ""

echo "📋 1. 检查 Java 进程"
echo "--------------------------------------"
JAVA_PROCS=$(ps aux | grep java | grep -v grep)
if [ -n "$JAVA_PROCS" ]; then
    echo "⚠️  发现运行中的 Java 进程："
    echo "$JAVA_PROCS"
else
    echo "✓ 没有运行中的 Java 进程"
fi
echo ""

echo "📋 2. 检查端口占用情况"
echo "--------------------------------------"
for port in 8081 8082 8083 8084; do
    result=$(netstat -tlnp 2>/dev/null | grep ":$port " || ss -tlnp 2>/dev/null | grep ":$port ")
    if [ -n "$result" ]; then
        echo "⚠️  端口 $port 被占用："
        echo "   $result"
    else
        echo "✓ 端口 $port 空闲可用"
    fi
done
echo ""

echo "📋 3. 检查 PID 文件"
echo "--------------------------------------"
cd /opt/gacha-system 2>/dev/null
PID_FILES=$(find . -name "*.pid" 2>/dev/null)
if [ -n "$PID_FILES" ]; then
    echo "发现 PID 文件："
    echo "$PID_FILES"
    echo ""
    for pidfile in $PID_FILES; do
        pid=$(cat "$pidfile" 2>/dev/null)
        if [ -n "$pid" ]; then
            if ps -p "$pid" > /dev/null 2>&1; then
                echo "  $pidfile: PID $pid - 进程运行中"
            else
                echo "  $pidfile: PID $pid - 进程不存在（残留文件）"
            fi
        fi
    done
else
    echo "✓ 没有发现 PID 文件"
fi
echo ""

echo "📋 4. 检查依赖服务状态"
echo "--------------------------------------"
services=("mysql" "redis" "rabbitmq-server" "elasticsearch")
for service in "${services[@]}"; do
    if systemctl is-active --quiet "$service" 2>/dev/null; then
        echo "✓ $service 运行中"
    else
        echo "✗ $service 未运行或不存在"
    fi
done
echo ""

echo "📋 5. 检查目录结构"
echo "--------------------------------------"
echo "主目录："
ls -lh /opt/gacha-system/ 2>/dev/null | grep "^d"
echo ""
echo "检查服务目录："
for dir in auth game gacha mall; do
    if [ -d "/opt/gacha-system/$dir" ]; then
        echo "✓ /opt/gacha-system/$dir 存在"
        # 检查是否有 JAR 文件或配置
        jar_count=$(find /opt/gacha-system/$dir -name "*.jar" 2>/dev/null | wc -l)
        if [ "$jar_count" -gt 0 ]; then
            echo "  发现 $jar_count 个 JAR 文件"
            find /opt/gacha-system/$dir -name "*.jar" -exec basename {} \;
        fi
    else
        echo "✗ /opt/gacha-system/$dir 不存在"
    fi
done
echo ""

echo "📋 6. 检查日志文件"
echo "--------------------------------------"
if [ -d "/opt/gacha-system/logs" ]; then
    echo "日志文件列表："
    ls -lhrt /opt/gacha-system/logs/ 2>/dev/null | tail -10
    echo ""
    
    # 显示最近的错误
    if [ -f "/opt/gacha-system/logs/mall-startup.log" ]; then
        echo "mall-startup.log 最后 20 行："
        tail -20 /opt/gacha-system/logs/mall-startup.log
        echo ""
    fi
    
    # 搜索错误关键字
    echo "搜索错误信息："
    grep -i "error\|exception\|failed\|bind" /opt/gacha-system/logs/*.log 2>/dev/null | tail -10
else
    echo "✗ 日志目录不存在"
fi
echo ""

echo "📋 7. 检查系统资源"
echo "--------------------------------------"
echo "磁盘使用："
df -h /opt/gacha-system
echo ""
echo "内存使用："
free -h
echo ""
echo "CPU 信息："
nproc
echo ""

echo "📋 8. 检查防火墙"
echo "--------------------------------------"
if command -v ufw &> /dev/null; then
    echo "UFW 状态："
    sudo ufw status 2>/dev/null || echo "无法获取 UFW 状态"
elif command -v firewall-cmd &> /dev/null; then
    echo "FirewallD 状态："
    sudo firewall-cmd --list-all 2>/dev/null || echo "无法获取 FirewallD 状态"
else
    echo "未检测到防火墙管理工具"
fi
echo ""

echo "======================================"
echo "  诊断完成！"
echo "======================================"
echo ""
echo "📌 建议操作："
echo ""
echo "1. 如果有残留的 Java 进程，执行："
echo "   pkill -9 java"
echo ""
echo "2. 清理 PID 文件："
echo "   cd /opt/gacha-system && rm -f *.pid"
echo ""
echo "3. 启动依赖服务（如果有未运行的）："
echo "   sudo systemctl start mysql redis rabbitmq-server elasticsearch"
echo ""
echo "4. 启动 Mall Service："
echo "   cd /opt/gacha-system/mall"
echo "   nohup java -jar -Dspring.profiles.active=prod -Xms512m -Xmx1024m /opt/gacha-system/mall-service.jar > ../logs/mall-startup.log 2>&1 &"
echo ""
echo "5. 验证服务："
echo "   curl http://localhost:8081/api/actuator/health"
echo ""
