# ============================================
# 远程服务器 Docker 部署完整指南
# 服务器: 111.228.12.167
# 配置: 4核 16GB
# ============================================

## 📋 前置准备

### 1. 连接服务器

```bash
# Windows PowerShell
ssh root@111.228.12.167

# 或使用 PuTTY / Xshell 等工具
# 主机: 111.228.12.167
# 端口: 22
# 用户: root
```

### 2. 检查系统信息

```bash
# 查看系统版本
cat /etc/os-release

# 查看CPU和内存
nproc        # 应该显示 4
free -h      # 应该显示约16GB

# 查看磁盘空间
df -h
```

---

## 🔧 第二步：安装 Docker 和 Docker Compose

### 方法A：一键安装脚本（推荐）

```bash
# 下载并执行官方安装脚本
curl -fsSL https://get.docker.com | bash

# 启动 Docker
sudo systemctl start docker
sudo systemctl enable docker

# 验证安装
docker --version
docker compose version
```

### 方法B：手动安装（如果方法A失败）

```bash
# 1. 更新系统
sudo yum update -y  # CentOS/RHEL
# 或
sudo apt update -y  # Ubuntu/Debian

# 2. 安装依赖
sudo yum install -y yum-utils device-mapper-persistent-data lvm2  # CentOS
# 或
sudo apt install -y apt-transport-https ca-certificates curl software-properties-common  # Ubuntu

# 3. 添加 Docker 仓库
sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo  # CentOS
# 或
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg  # Ubuntu
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null  # Ubuntu

# 4. 安装 Docker
sudo yum install -y docker-ce docker-ce-cli containerd.io  # CentOS
# 或
sudo apt install -y docker-ce docker-ce-cli containerd.io  # Ubuntu

# 5. 启动 Docker
sudo systemctl start docker
sudo systemctl enable docker

# 6. 安装 Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# 7. 验证
docker --version
docker-compose --version
```

### 配置 Docker 权限（可选但推荐）

```bash
# 将当前用户添加到 docker 组（避免每次都用 sudo）
sudo usermod -aG docker $USER

# 重新登录使配置生效
exit
# 然后重新 ssh 连接
ssh root@111.228.12.167
```

---

## 📦 第三步：上传项目到服务器

### 方法A：使用 Git（推荐）

```bash
# 1. 创建项目目录
sudo mkdir -p /opt/gacha-system
cd /opt/gacha-system

# 2. 克隆项目
git clone https://github.com/hubuwei/gacha-system.git .

# 3. 确认文件已下载
ls -la
```

### 方法B：使用 SCP 从本地上传

在**本地电脑**执行：

```powershell
# Windows PowerShell
scp -r E:\CFDemo\gacha-system\* root@111.228.12.167:/opt/gacha-system/
```

或

```bash
# Linux/Mac
scp -r /path/to/gacha-system/* root@111.228.12.167:/opt/gacha-system/
```

### 方法C：使用 SFTP 工具

使用 WinSCP、FileZilla 等工具上传整个项目文件夹到 `/opt/gacha-system/`

---

## ⚙️ 第四步：配置环境变量

```bash
# 进入项目目录
cd /opt/gacha-system

# 复制环境变量模板
cp .env.docker .env

# 编辑配置文件
nano .env
```

修改以下配置（**重要**）：

```bash
# 数据库密码（改成强密码）
DB_PASSWORD=YourStrongPassword123!

# Redis密码
REDIS_PASSWORD=YourRedisPassword456!

# RabbitMQ密码
RABBITMQ_USERNAME=admin
RABBITMQ_PASSWORD=YourRabbitMQPassword789!

# 邮件配置（保持不变或修改为你的）
MAIL_USERNAME=1712133303@qq.com
MAIL_PASSWORD=wkokvlcppuuaehhd
```

保存退出：
- `Ctrl + O` 保存
- `Enter` 确认
- `Ctrl + X` 退出

---

## 🎯 第五步：一键部署

```bash
# 1. 赋予执行权限
chmod +x docker-deploy.sh

# 2. 一键启动所有服务
sudo ./docker-deploy.sh up
```

**等待时间：**
- 首次构建：5-10分钟（下载基础镜像 + 编译代码）
- 后续启动：1-2分钟

### 观察部署过程

你会看到类似输出：
```
==========================================
游戏商城系统 - Docker 部署
==========================================
✓ Docker环境检查通过

步骤 1/4: 构建镜像...
[+] Building 120.5s (15/15) FINISHED

步骤 2/4: 启动服务...
[+] Running 9/9
 ✔ Container gacha-mysql          Started
 ✔ Container gacha-redis          Started
 ✔ Container gacha-rabbitmq       Started
 ✔ Container gacha-elasticsearch  Started
 ✔ Container gacha-mall-service   Started
 ✔ Container gacha-auth-service   Started
 ✔ Container gacha-game-service   Started
 ✔ Container gacha-gacha-service  Started
 ✔ Container gacha-frontend       Started

步骤 3/4: 等待服务启动...

步骤 4/4: 检查服务状态...
NAME                     STATUS                    PORTS
gacha-mysql              Up (healthy)              3306/tcp
gacha-redis              Up (healthy)              6379/tcp
gacha-rabbitmq           Up (healthy)              5672/tcp, 15672/tcp
gacha-elasticsearch      Up (healthy)              9200/tcp
gacha-mall-service       Up (healthy)              8081/tcp
gacha-auth-service       Up (healthy)              8084/tcp
gacha-game-service       Up (healthy)              8082/tcp
gacha-gacha-service      Up (healthy)              8083/tcp
gacha-frontend           Up (healthy)              80/tcp

==========================================
部署完成！
==========================================

访问地址:
  前端: http://localhost
  API:  http://localhost:8081/api
  RabbitMQ管理: http://localhost:15672 (admin/你的密码)
```

---

## ✅ 第六步：验证部署

### 1. 检查所有容器状态

```bash
# 查看所有容器
docker compose ps

# 应该看到所有服务都是 "Up" 状态
```

### 2. 测试前端访问

在浏览器访问：
```
http://111.228.12.167
```

应该能看到游戏商城首页。

### 3. 测试API

```bash
# 测试商城API
curl http://111.228.12.167:8081/api/games

# 测试认证API
curl http://111.228.12.167:8084/api/auth/health

# 应该返回JSON数据
```

### 4. 检查日志

```bash
# 查看所有服务日志
sudo ./docker-deploy.sh logs

# 查看特定服务日志
sudo ./docker-deploy.sh logs mall-service

# 实时跟踪日志
sudo ./docker-deploy.sh logs -f mall-service
```

### 5. 检查资源占用

```bash
# 查看容器资源使用
docker stats

# 查看系统资源
htop  # 如果安装了
# 或
top
```

---

## 🔐 第七步：配置防火墙

### CentOS/RHEL (firewalld)

```bash
# 开放必要端口
sudo firewall-cmd --permanent --add-port=80/tcp
sudo firewall-cmd --permanent --add-port=443/tcp
sudo firewall-cmd --permanent --add-port=8081/tcp
sudo firewall-cmd --permanent --add-port=8082/tcp
sudo firewall-cmd --permanent --add-port=8083/tcp
sudo firewall-cmd --permanent --add-port=8084/tcp

# 重载防火墙
sudo firewall-cmd --reload

# 验证
sudo firewall-cmd --list-ports
```

### Ubuntu (ufw)

```bash
# 启用防火墙
sudo ufw enable

# 开放端口
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw allow 8081/tcp
sudo ufw allow 8082/tcp
sudo ufw allow 8083/tcp
sudo ufw allow 8084/tcp

# 验证
sudo ufw status
```

### 云服务器安全组

如果你使用的是阿里云、腾讯云等，还需要在控制台配置安全组：

1. 登录云服务商控制台
2. 找到实例的安全组配置
3. 添加入站规则：
   - 端口 80 (HTTP)
   - 端口 443 (HTTPS)
   - 端口 8081-8084 (API)
   - 源IP: 0.0.0.0/0 (允许所有IP) 或指定IP

---

## 🌐 第八步：配置域名（可选）

如果你有域名，可以配置DNS解析：

1. 添加A记录：
   ```
   类型: A
   主机: @ 或 www
   值: 111.228.12.167
   TTL: 600
   ```

2. 等待DNS生效（通常几分钟到几小时）

3. 通过域名访问：
   ```
   http://your-domain.com
   ```

---

## 📊 第九步：监控和维护

### 常用维护命令

```bash
# 查看服务状态
sudo ./docker-deploy.sh status

# 重启某个服务
sudo docker compose restart mall-service

# 查看实时日志
sudo ./docker-deploy.sh logs -f

# 停止所有服务
sudo ./docker-deploy.sh down

# 重新构建并启动
sudo ./docker-deploy.sh rebuild

# 清理未使用的镜像和容器
docker system prune -a
```

### 备份数据

```bash
# 创建备份目录
sudo mkdir -p /opt/backups

# 备份MySQL数据
sudo docker run --rm \
  -v gacha-system_mysql-data:/data \
  -v /opt/backups:/backup \
  alpine tar czf /backup/mysql-backup-$(date +%Y%m%d).tar.gz /data

# 备份所有数据卷
sudo docker run --rm \
  -v /var/lib/docker/volumes:/data \
  -v /opt/backups:/backup \
  alpine tar czf /backup/all-volumes-backup-$(date +%Y%m%d).tar.gz /data

# 查看备份
ls -lh /opt/backups/
```

### 定时备份（Crontab）

```bash
# 编辑定时任务
sudo crontab -e

# 添加每天凌晨2点备份
0 2 * * * /opt/gacha-system/backup.sh

# 创建备份脚本
sudo nano /opt/gacha-system/backup.sh
```

备份脚本内容：
```bash
#!/bin/bash
BACKUP_DIR="/opt/backups"
DATE=$(date +%Y%m%d_%H%M%S)

mkdir -p $BACKUP_DIR

# 备份MySQL
docker run --rm \
  -v gacha-system_mysql-data:/data \
  -v $BACKUP_DIR:/backup \
  alpine tar czf /backup/mysql-$DATE.tar.gz /data

# 删除7天前的备份
find $BACKUP_DIR -name "mysql-*.tar.gz" -mtime +7 -delete

echo "Backup completed: $DATE"
```

---

## 🐛 故障排查

### 问题1：容器启动失败

```bash
# 1. 查看具体错误
sudo docker compose logs [service-name]

# 2. 检查依赖服务
sudo docker compose ps

# 3. 重新启动
sudo docker compose restart [service-name]
```

### 问题2：端口被占用

```bash
# 查看端口占用
sudo netstat -tlnp | grep 8081

# 停止占用端口的进程
sudo kill -9 [PID]

# 或修改 docker-compose.yml 中的端口映射
```

### 问题3：内存不足

```bash
# 查看内存使用
free -h
docker stats

# 限制容器内存（修改 docker-compose.yml）
deploy:
  resources:
    limits:
      memory: 512M
```

### 问题4：数据库连接失败

```bash
# 检查MySQL是否启动
sudo docker compose logs mysql

# 进入MySQL容器
sudo docker exec -it gacha-mysql mysql -uroot -p

# 检查数据库是否存在
SHOW DATABASES;
USE gacha_system_prod;
SHOW TABLES;
```

### 问题5：图片无法访问

```bash
# 检查GamePapers目录
ls -la /opt/gacha-system/GamePapers/

# 检查权限
sudo chmod -R 755 /opt/gacha-system/GamePapers/

# 重启mall-service
sudo docker compose restart mall-service
```

---

## 🔒 安全加固建议

### 1. 修改默认SSH端口

```bash
# 编辑SSH配置
sudo nano /etc/ssh/sshd_config

# 修改端口
Port 2222

# 重启SSH
sudo systemctl restart sshd

# 记得开放新端口
sudo firewall-cmd --permanent --add-port=2222/tcp
sudo firewall-cmd --reload
```

### 2. 禁用root登录

```bash
# 创建新用户
sudo adduser admin
sudo usermod -aG sudo admin  # Ubuntu
# 或
sudo usermod -aG wheel admin  # CentOS

# 禁用root登录
sudo nano /etc/ssh/sshd_config
PermitRootLogin no

# 重启SSH
sudo systemctl restart sshd
```

### 3. 配置Fail2Ban

```bash
# 安装
sudo yum install -y fail2ban  # CentOS
# 或
sudo apt install -y fail2ban  # Ubuntu

# 启动
sudo systemctl enable fail2ban
sudo systemctl start fail2ban
```

### 4. 定期更新系统

```bash
# 每周更新
sudo yum update -y  # CentOS
# 或
sudo apt update && sudo apt upgrade -y  # Ubuntu

# 重启服务
sudo docker compose restart
```

---

## 📈 性能优化

### 1. JVM参数调优

编辑 `docker-compose.yml`，调整JAVA_OPTS：

```yaml
mall-service:
  environment:
    JAVA_OPTS: "-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

### 2. MySQL优化

```bash
# 进入MySQL
sudo docker exec -it gacha-mysql mysql -uroot -p

# 执行优化
SET GLOBAL innodb_buffer_pool_size = 536870912;  # 512MB
```

### 3. Nginx优化

已在 `game-mall/nginx.conf` 中配置：
- Gzip压缩
- 静态资源缓存
- 连接池优化

---

## 🎓 学习资源

- [Docker官方文档](https://docs.docker.com/)
- [Docker Compose文档](https://docs.docker.com/compose/)
- [本项目Docker文档](DOCKER_DEPLOYMENT.md)

---

## 🆘 获取帮助

遇到问题？

1. 查看日志：`sudo ./docker-deploy.sh logs`
2. 检查状态：`sudo ./docker-deploy.sh status`
3. 查阅文档：`cat DOCKER_DEPLOYMENT.md`
4. 提交Issue：https://github.com/hubuwei/gacha-system/issues

---

**祝你部署顺利！** 🚀

如有任何问题，随时联系。
