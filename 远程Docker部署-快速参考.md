# 远程Docker部署 - 快速参考卡片

> 💡 **这是精简版命令速查表，详细步骤请查看《远程服务器Docker部署-超详细指南.md》**

---

## 🚀 一键部署流程（复制粘贴即可）

### 1️⃣ 连接服务器
```bash
ssh root@你的服务器IP
```

### 2️⃣ 安装Docker和Compose
```bash
# 安装Docker
curl -fsSL https://get.docker.com | bash
systemctl start docker
systemctl enable docker

# 安装Docker Compose
apt install -y docker-compose-plugin   # Ubuntu
# 或
yum install -y docker-compose-plugin   # CentOS
```

### 3️⃣ 配置镜像加速器
```bash
cat > /etc/docker/daemon.json <<EOF
{
  "registry-mirrors": [
    "https://docker.m.daocloud.io",
    "https://huecker.io",
    "https://dockerhub.timeweb.cloud"
  ]
}
EOF
systemctl restart docker
```

### 4️⃣ 拉取代码
```bash
mkdir -p /opt/gacha-system
cd /opt/gacha-system
git clone https://github.com/hubuwei/gacha-system.git .
```

### 5️⃣ 配置环境变量
```bash
cp .env.docker .env
nano .env
# 修改密码后保存（Ctrl+O, Enter, Ctrl+X）
```

### 6️⃣ 开放防火墙端口
```bash
# Ubuntu
ufw allow 22/tcp
ufw allow 80/tcp
ufw allow 8081:8084/tcp
ufw allow 3307/tcp
ufw allow 6379/tcp
ufw allow 9200/tcp
ufw allow 15672/tcp
ufw enable

# CentOS
firewall-cmd --permanent --add-port={22,80,8081,8082,8083,8084,3307,6379,9200,15672}/tcp
firewall-cmd --reload
```

### 7️⃣ 构建并启动
```bash
cd /opt/gacha-system
docker-compose build
docker-compose up -d
sleep 30
```

### 8️⃣ 验证部署
```bash
docker-compose ps
docker-compose logs -f mall-service
curl http://localhost:8081/api/actuator/health
```

---

## 📋 常用命令速查

### 容器管理
```bash
# 查看所有容器
docker-compose ps

# 启动所有服务
docker-compose up -d

# 停止所有服务
docker-compose down

# 重启某个服务
docker-compose restart mall-service

# 查看实时日志
docker-compose logs -f 服务名

# 查看最近100行日志
docker-compose logs --tail=100 服务名
```

### 进入容器
```bash
# 进入Mall Service
docker exec -it gacha-mall-service bash

# 进入MySQL
docker exec -it gacha-mysql mysql -uroot -p

# 进入Redis
docker exec -it gacha-redis redis-cli -a 密码

# 退出容器
exit
```

### 更新部署
```bash
cd /opt/gacha-system
git pull
docker-compose build
docker-compose up -d
docker-compose logs -f
```

### 清理空间
```bash
# 清理未使用的资源
docker system prune -a

# 查看磁盘占用
docker system df
```

---

## 🌐 访问地址

| 服务 | 地址 |
|------|------|
| 前端 | `http://服务器IP` |
| Mall API | `http://服务器IP:8081/api` |
| Auth API | `http://服务器IP:8084/api/auth` |
| Game API | `http://服务器IP:8082/api/game` |
| Gacha API | `http://服务器IP:8083/api/gacha` |
| RabbitMQ | `http://服务器IP:15672` |
| MySQL | `服务器IP:3307` |
| Redis | `服务器IP:6379` |
| ES | `http://服务器IP:9200` |

---

## ⚠️ 常见问题

### 容器启动失败
```bash
docker-compose logs 服务名
```

### 端口被占用
```bash
netstat -tulpn | grep 端口号
kill -9 进程ID
```

### 内存不足
```bash
free -h
docker stats
```

### Elasticsearch下载失败
```bash
# 方案1：临时移除加速器
mv /etc/docker/daemon.json /etc/docker/daemon.json.bak
systemctl restart docker
docker pull elasticsearch:7.17.29
mv /etc/docker/daemon.json.bak /etc/docker/daemon.json
systemctl restart docker

# 方案2：注释掉ES（不影响核心功能）
nano docker-compose.yml
# 注释elasticsearch相关配置
```

---

## 🔧 故障排查三步法

1. **看状态**：`docker-compose ps`
2. **看日志**：`docker-compose logs -f 服务名`
3. **搜错误**：复制错误信息到搜索引擎

---

## 📞 需要帮助？

查看详细文档：`远程服务器Docker部署-超详细指南.md`

祝你好运！🎉
