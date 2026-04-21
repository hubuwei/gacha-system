#!/bin/bash
# mall-service ES配置修复脚本
# 用于手动重启mall-service并启用ES功能

echo "========================================="
echo "开始修复 mall-service ES配置"
echo "========================================="

# 1. 停止旧的mall-service容器
echo "1. 停止旧的mall-service容器..."
docker stop gacha-mall-service || true
docker rm gacha-mall-service || true

# 2. 重新启动mall-service，确保环境变量正确
echo "2. 重新启动mall-service..."
docker run -d \
  --name gacha-mall-service \
  --restart always \
  --network gacha-network \
  -p 8081:8081 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e ELASTICSEARCH_ENABLED=true \
  -e ES_URIS=http://gacha-elasticsearch:9200 \
  -e ES_USERNAME=elastic \
  -e ES_PASSWORD=Xc037417! \
  -e DB_HOST=gacha-mysql \
  -e DB_PORT=3306 \
  -e DB_NAME=gacha_system_prod \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=Xc037417! \
  -e REDIS_HOST=gacha-redis \
  -e REDIS_PORT=6379 \
  -e REDIS_PASSWORD=Xc037417! \
  -e RABBITMQ_HOST=gacha-rabbitmq \
  -e RABBITMQ_PORT=5672 \
  -e RABBITMQ_USERNAME=admin \
  -e RABBITMQ_PASSWORD=Xc037417! \
  gacha-system-mall-service:v6

# 3. 等待服务启动
echo "3. 等待服务启动（30秒）..."
sleep 30

# 4. 检查服务状态
echo "4. 检查服务状态..."
docker logs gacha-mall-service --tail 20 | grep -i "started\|elasticsearch"

# 5. 同步数据到ES
echo "5. 同步游戏数据到Elasticsearch..."
curl -X POST http://localhost:8081/api/sync/games

echo ""
echo "========================================="
echo "修复完成！请检查日志确认ES已启用"
echo "========================================="
echo "查看完整日志: docker logs gacha-mall-service --tail 100"
echo "测试搜索: curl 'http://localhost:8081/api/search/autocomplete?prefix=黑神话'"
