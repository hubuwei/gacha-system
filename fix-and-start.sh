#!/bin/bash
echo "Step 1: 修复 MySQL 用户密码..."
mysql -u root -pXc037417! <<'SQL'
DROP USER IF EXISTS 'mall_user'@'localhost';
CREATE USER 'mall_user'@'localhost' IDENTIFIED BY 'Xc037417!';
GRANT ALL PRIVILEGES ON gacha_system_prod.* TO 'mall_user'@'localhost';
FLUSH PRIVILEGES;
SELECT 'OK' AS result;
SQL

echo ""
echo "Step 2: 测试 mall_user 连接..."
mysql -u mall_user -pXc037417! -e "SHOW DATABASES;" 2>&1
echo "返回码: $?"

echo ""
echo "Step 3: 创建各服务配置..."

# 创建通用配置文件
cat > /opt/gacha-system/shared-config.yml <<'YAML'
spring:
  main:
    allow-bean-definition-overriding: true
  datasource:
    url: jdbc:mysql://localhost:3306/gacha_system_prod?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: mall_user
    password: Xc037417!
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
  redis:
    host: localhost
    port: 6379
    password: Xc037417!
    database: 0
    timeout: 5000ms
  rabbitmq:
    host: localhost
    port: 5672
    username: admin
    password: Xc037417!
    virtual-host: /
  elasticsearch:
    uris: http://localhost:9200
    connection-timeout: 5s
    read-timeout: 30s
logging:
  level:
    root: WARN
    com.cheng: INFO
  file:
    name: /opt/gacha-system/logs/service.log
YAML

echo "✓ 配置文件已创建"
echo ""
echo "Step 4: 创建启动脚本..."

# 为每个服务创建启动脚本
for svc_dir in auth game gacha mall; do
  svc_name="${svc_dir}-service"
  jar_path="/opt/gacha-system/${svc_dir}/${svc_name}.jar"
  
  if [ ! -f "$jar_path" ]; then
    echo "⚠️  $svc_name.jar 不存在，跳过"
    continue
  fi
  
  case "$svc_dir" in
    auth)   port=8081 ;;
    game)   port=8082 ;;
    gacha)  port=8083 ;;
    mall)   port=8084 ;;
  esac
  
  cat > "/opt/gacha-system/${svc_dir}/start.sh" <<EOF
#!/bin/bash
cd /opt/gacha-system/${svc_dir}
nohup java -Xms256m -Xmx512m -jar "${jar_path}" --spring.profiles.active=prod --spring.config.additional-location=file:/opt/gacha-system/shared-config.yml --server.port=${port} > /opt/gacha-system/logs/${svc_dir}-startup.log 2>&1 &
echo \$! > ${svc_name}.pid
EOF
  chmod +x "/opt/gacha-system/${svc_dir}/start.sh"
  echo "✓ ${svc_name} (${port}) 启动脚本已创建"
done

echo ""
echo "Step 5: 清理旧进程..."
pkill -f 'service.jar' 2>/dev/null || true
sleep 2
rm -f /opt/gacha-system/*/*.pid
echo "✓ 清理完成"

echo ""
echo "Step 6: 启动服务..."
for svc_dir in auth game gacha mall; do
  svc_name="${svc_dir}-service"
  case "$svc_dir" in
    auth)   port=8081 ;;
    game)   port=8082 ;;
    gacha)  port=8083 ;;
    mall)   port=8084 ;;
  esac
  
  if [ -f "/opt/gacha-system/${svc_dir}/start.sh" ]; then
    echo "🚀 启动 ${svc_name} (端口 ${port})..."
    bash "/opt/gacha-system/${svc_dir}/start.sh"
    sleep 3
    echo "   PID: $(cat /opt/gacha-system/${svc_dir}/${svc_name}.pid 2>/dev/null || echo 'N/A')"
  fi
done

echo ""
echo "======================================"
echo "  等待 30 秒..."
echo "======================================"
sleep 30

echo ""
echo "======================================"
echo "  服务状态"
echo "======================================"
echo ""
echo "=== 端口监听 ==="
netstat -tlnp 2>/dev/null | grep -E "808[1-4]" || ss -tlnp 2>/dev/null | grep -E "808[1-4]" || echo "暂无端口监听"
echo ""
echo "=== 进程 ==="
ps aux | grep "service.jar" | grep -v grep || echo "无运行中的服务进程"
echo ""
echo "=== 健康检查 ==="
for port in 8081 8082 8083 8084; do
  code=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:${port}/actuator/health" 2>/dev/null)
  if [ "$code" = "200" ] || [ "$code" = "401" ]; then
    echo "端口 ${port}: ✅ HTTP ${code}"
  else
    echo "端口 ${port}: ❌ HTTP ${code}"
  fi
done
echo ""
echo "=== 日志摘要 ==="
for svc_dir in auth game gacha mall; do
  logfile="/opt/gacha-system/logs/${svc_dir}-startup.log"
  if [ -f "$logfile" ]; then
    echo "--- ${svc_dir} 最后 3 行 ---"
    tail -3 "$logfile"
    echo ""
    if grep -q "Started.*Application" "$logfile" 2>/dev/null; then
      echo "✅ ${svc_dir} 启动成功!"
    elif grep -qi "Access denied" "$logfile" 2>/dev/null; then
      echo "❌ ${svc_dir} 数据库认证失败"
    elif grep -qi "error\|exception" "$logfile" 2>/dev/null; then
      echo "⚠️  ${svc_dir} 可能有错误"
    fi
    echo ""
  fi
done
