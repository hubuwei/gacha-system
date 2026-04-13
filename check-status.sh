#!/bin/bash
echo "=== 端口状态 ==="
netstat -tlnp | grep -E "808[1-4]" || echo "暂无端口监听"
echo ""
echo "=== 进程状态 ==="
ps aux | grep "service.jar" | grep -v grep
echo ""
echo "=== 健康检查 ==="
for port in 8081 8082 8083 8084; do
  result=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:$port/actuator/health 2>/dev/null)
  if [ -z "$result" ] || [ "$result" = "000" ]; then
    echo "端口 $port: 未响应"
  else
    echo "端口 $port: HTTP $result"
  fi
done
echo ""
echo "=== 最新日志 ==="
for svc in auth game gacha mall; do
  echo "--- ${svc}-startup.log 最后5行 ---"
  tail -5 /opt/gacha-system/logs/${svc}-startup.log 2>/dev/null || echo "日志不存在"
  echo ""
done
