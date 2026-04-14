#!/bin/bash
# 配置Docker镜像加速器
cat > /etc/docker/daemon.json <<'EOF'
{
  "registry-mirrors": [
    "https://docker.m.daocloud.io",
    "https://huecker.io",
    "https://dockerhub.timeweb.cloud"
  ]
}
EOF

# 重启Docker
systemctl restart docker
echo "✓ Docker镜像加速器配置完成"

# 拉取中间件镜像
echo "开始拉取中间件镜像..."
docker pull mysql:8.0
docker pull redis:7-alpine
docker pull rabbitmq:3-management-alpine

# 尝试拉取Elasticsearch
echo "尝试拉取Elasticsearch..."
if docker pull elasticsearch:7.17.29; then
    echo "✓ Elasticsearch拉取成功"
else
    echo "⚠ Elasticsearch拉取失败，尝试备用方案..."
    # 临时移除加速器直接拉取
    mv /etc/docker/daemon.json /etc/docker/daemon.json.bak
    systemctl restart docker
    docker pull elasticsearch:7.17.29
    mv /etc/docker/daemon.json.bak /etc/docker/daemon.json
    systemctl restart docker
fi

echo "✓ 所有中间件镜像拉取完成"
docker images | grep -E "mysql|redis|rabbitmq|elasticsearch"
