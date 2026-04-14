# 服务器Docker部署完整指南（含Elasticsearch）

## 📋 服务器信息
- **IP**: 111.228.12.167
- **配置**: 4核16GB Ubuntu 24.04
- **Docker版本**: 29.1.3

---

## 🚀 快速部署步骤

### 第1步：连接服务器
```bash
ssh root@111.228.12.167
```

### 第2步：安装Docker Compose（三选一）

#### 方法A：使用apt安装（推荐，最快）
```bash
apt update
apt install -y docker-compose-plugin
# 验证
docker compose version
```

#### 方法B：从GitHub下载
```bash
curl -L "https://github.com/docker/compose/releases/download/v2.29.1/docker-compose-Linux-x86_64" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose
docker-compose --version
```

#### 方法C：使用国内镜像
```bash
curl -L "https://ghproxy.com/https://github.com/docker/compose/releases/download/v2.29.1/docker-compose-Linux-x86_64" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose
docker-compose --version
```

### 第3步：配置Docker镜像加速器
```bash
cat > /etc/docker/daemon.json <<EOF
{
  "registry-mirrors": [
    "https://docker.m.daocloud.io",
    "https://huecker.io",
    "https://dockerhub.timeweb.cloud"
  ],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  }
}
EOF
systemctl restart docker
```

### 第4步：准备项目目录
```bash
cd /opt/gacha-system
git pull
```

### 第5步：配置环境变量
```bash
cp .env.docker .env
nano .env
```

修改以下配置：
```bash
DB_PASSWORD=你的强密码
REDIS_PASSWORD=你的强密码
RABBITMQ_PASSWORD=你的强密码
MAIL_PASSWORD=你的QQ邮箱授权码
```

保存：`Ctrl+O` → `Enter` → `Ctrl+X`

### 第6步：停止旧服务
```bash
# 停止旧的JAR服务
pkill -f "mall-service.jar" || true
pkill -f "auth-service.jar" || true
pkill -f "game-service.jar" || true
pkill -f "gacha-service.jar" || true

# 停止旧的Docker容器
docker-compose down 2>/dev/null || true
```

### 第7步：构建并启动服务

#### 方案A：完整部署（包含Elasticsearch）

**如果Elasticsearch镜像能正常下载：**
```bash
docker compose build
docker compose up -d
```

**如果遇到Elasticsearch 403错误，先手动拉取：**
```bash
# 临时移除镜像加速器
mv /etc/docker/daemon.json /etc/docker/daemon.json.bak
systemctl restart docker

# 直接拉取ES镜像
docker pull elasticsearch:7.17.29

# 恢复镜像加速器
mv /etc/docker/daemon.json.bak /etc/docker/daemon.json
systemctl restart docker

# 然后正常启动
docker compose up -d
```

#### 方案B：跳过Elasticsearch（快速测试）

如果ES一直下载失败，可以暂时注释掉：

```bash
# 编辑docker-compose.yml
nano docker-compose.yml

# 找到 elasticsearch: 部分，在每行前加 # 注释掉
# 同时注释掉其他服务中的 depends_on elasticsearch

# 然后启动
docker compose up -d
```

### 第8步：验证部署
```bash
# 查看所有容器状态
docker compose ps

# 查看日志
docker compose logs -f mall-service

# 测试API
curl http://localhost:8081/api/actuator/health
```

---

## 🔧 Elasticsearch专项处理

### 问题1：403 Forbidden错误

**原因**：镜像加速器不支持ES特定版本

**解决方案**：
```bash
# 方案1：直接从Docker Hub拉取（不使用加速器）
docker pull elasticsearch:7.17.29

# 方案2：使用官方中国镜像
docker pull registry.docker-cn.com/library/elasticsearch:7.17.29
docker tag registry.docker-cn.com/library/elasticsearch:7.17.29 elasticsearch:7.17.29

# 方案3：降级到可用版本
# 修改docker-compose.yml中的 elasticsearch:7.17.29 为 elasticsearch:7.17.0
```

### 问题2：内存不足

ES默认需要2GB内存，如果服务器内存紧张：

```bash
# 编辑docker-compose.yml，修改ES_JAVA_OPTS
environment:
  - "ES_JAVA_OPTS=-Xms256m -Xmx256m"  # 改为256MB
```

### 问题3：IK分词器安装

如果需要中文搜索，需要安装IK分词器：

```bash
# 进入ES容器
docker exec -it gacha-elasticsearch bash

# 安装IK分词器
bin/elasticsearch-plugin install https://ghproxy.com/https://github.com/infinilabs/analysis-ik/releases/download/v7.17.29/elasticsearch-analysis-ik-7.17.29.zip

# 退出并重启容器
exit
docker restart gacha-elasticsearch
```

---

## 🌐 访问地址

部署成功后：

- **前端页面**: http://111.228.12.167
- **Mall API**: http://111.228.12.167:8081/api
- **Auth API**: http://111.228.12.167:8084/api/auth
- **Game API**: http://111.228.12.167:8082/api/game
- **Gacha API**: http://111.228.12.167:8083/api/gacha
- **RabbitMQ管理**: http://111.228.12.167:15672 (admin/密码)
- **MySQL**: 111.228.12.167:3307
- **Redis**: 111.228.12.167:6379
- **Elasticsearch**: http://111.228.12.167:9200

---

## 🔥 防火墙配置

```bash
# Ubuntu UFW
ufw allow 80/tcp
ufw allow 8081/tcp
ufw allow 8082/tcp
ufw allow 8083/tcp
ufw allow 8084/tcp
ufw allow 15672/tcp
ufw allow 3307/tcp
ufw allow 6379/tcp
ufw allow 9200/tcp
ufw reload
```

---

## 🛠️ 常用维护命令

```bash
# 查看日志
docker compose logs -f [服务名]

# 重启服务
docker compose restart [服务名]

# 停止所有服务
docker compose down

# 更新部署
git pull
docker compose build
docker compose up -d

# 清理空间
docker system prune -a
```

---

## ⚠️ 注意事项

1. **首次构建需要5-10分钟**，请耐心等待
2. **确保磁盘空间充足**（至少10GB）
3. **Elasticsearch可选**，如果不需要搜索功能可以跳过
4. **数据库会自动初始化**，无需手动导入SQL
5. **GamePapers图片目录**会自动挂载到容器

---

## 📊 资源占用

```
MySQL:           ~512MB
Redis:           ~128MB  
RabbitMQ:        ~256MB
Elasticsearch:   ~512MB (可调整为256MB)
Mall Service:    ~512MB
Auth Service:    ~256MB
Game Service:    ~256MB
Gacha Service:   ~256MB
Frontend:        ~64MB
系统预留:        ~2GB
--------------------------
总计:            ~3.5GB (占16GB的22%)
```

**你的4C16G服务器完全够用！** ✅
