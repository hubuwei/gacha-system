#!/bin/bash
# 构建并部署带插件的Elasticsearch镜像
# 用于持久化ES配置，防止重启后丢失插件

set -e

echo "========================================="
echo "开始构建 Elasticsearch 镜像（含IK+拼音插件）"
echo "========================================="

# 1. 构建镜像
echo "1. 构建Docker镜像..."
docker build -t gacha-elasticsearch:with-plugins -f Dockerfile.elasticsearch .

# 2. 标记镜像
echo "2. 标记镜像版本..."
docker tag gacha-elasticsearch:with-plugins gacha-elasticsearch:v1

# 3. 停止旧容器
echo "3. 停止旧的Elasticsearch容器..."
docker stop gacha-elasticsearch || true
docker rm gacha-elasticsearch || true

# 4. 启动新容器
echo "4. 启动新的Elasticsearch容器..."
docker run -d \
  --name gacha-elasticsearch \
  --restart always \
  -p 9200:9200 \
  -p 9300:9300 \
  -e discovery.type=single-node \
  -e ES_JAVA_OPTS="-Xms1g -Xmx1g" \
  -e xpack.security.enabled=false \
  -v $(pwd)/elasticsearch/data:/usr/share/elasticsearch/data \
  --network gacha-network \
  gacha-elasticsearch:v1

# 5. 等待ES启动
echo "5. 等待Elasticsearch启动（30秒）..."
sleep 30

# 6. 验证插件
echo "6. 验证插件安装..."
docker exec gacha-elasticsearch bin/elasticsearch-plugin list

# 7. 检查集群健康
echo "7. 检查ES集群健康状态..."
curl -s http://localhost:9200/_cluster/health | python3 -m json.tool 2>/dev/null || curl -s http://localhost:9200/_cluster/health

echo ""
echo "========================================="
echo "✅ Elasticsearch 镜像构建并部署完成！"
echo "========================================="
echo "已安装的插件："
docker exec gacha-elasticsearch bin/elasticsearch-plugin list
echo ""
echo "数据持久化位置: $(pwd)/elasticsearch/data"
echo "即使容器重建，数据和插件都不会丢失！"
