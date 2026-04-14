# 远程服务器Docker容器化部署 - 超详细新手指南

> 📌 **本指南专为技术小白设计，每一步都有详细说明和命令解释**

---

## 📋 部署前准备清单

### 1. 你需要准备的东西

- ✅ 一台远程Linux服务器（Ubuntu/CentOS）
- ✅ 服务器的IP地址、用户名和密码
- ✅ 本地电脑已安装Git和SSH客户端
- ✅ 项目代码在本地 `E:\CFDemo\gacha-system` 目录

### 2. 服务器最低配置要求

- CPU: 2核及以上
- 内存: 8GB及以上（推荐16GB）
- 硬盘: 至少20GB可用空间
- 操作系统: Ubuntu 20.04+ 或 CentOS 7+

---

## 🚀 完整部署步骤（共8步）

### 第1步：连接远程服务器

#### 1.1 打开PowerShell

在你的Windows电脑上：
1. 按 `Win + R` 键
2. 输入 `powershell`
3. 按回车键

#### 1.2 使用SSH连接服务器

在PowerShell中输入以下命令（替换成你的实际信息）：

```powershell
ssh root@你的服务器IP地址
```

例如：
```powershell
ssh root@111.228.12.167
```

**首次连接会提示：**
```
The authenticity of host 'xxx.xxx.xxx.xxx' can't be established.
Are you sure you want to continue connecting (yes/no)?
```

输入 `yes` 然后按回车。

接着输入服务器密码（输入时不会显示字符，这是正常的），按回车。

**成功标志：** 命令行提示符变成类似 `root@your-server:~#`

---

### 第2步：检查并安装Docker环境

#### 2.1 检查Docker是否已安装

在服务器上执行：

```bash
docker --version
```

- **如果显示版本号**（如 `Docker version 29.1.3`），说明已安装，跳到第2.3步
- **如果提示 `command not found`**，说明未安装，继续第2.2步

#### 2.2 安装Docker（如果未安装）

**方法A：一键安装脚本（推荐）**

```bash
curl -fsSL https://get.docker.com | bash
```

等待安装完成（可能需要2-5分钟）。

**方法B：手动安装（Ubuntu系统）**

```bash
# 更新软件包列表
apt update

# 安装依赖包
apt install -y apt-transport-https ca-certificates curl software-properties-common

# 添加Docker官方GPG密钥
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | apt-key add -

# 添加Docker仓库
add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"

# 更新软件包列表
apt update

# 安装Docker
apt install -y docker-ce docker-ce-cli containerd.io
```

**方法C：手动安装（CentOS系统）**

```bash
# 安装依赖
yum install -y yum-utils device-mapper-persistent-data lvm2

# 添加Docker仓库
yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo

# 安装Docker
yum install -y docker-ce docker-ce-cli containerd.io
```

#### 2.3 启动Docker服务

```bash
# 启动Docker
systemctl start docker

# 设置开机自启
systemctl enable docker

# 验证Docker运行状态
systemctl status docker
```

看到 `active (running)` 表示成功。

#### 2.4 安装Docker Compose

**检查是否已安装：**

```bash
docker compose version
```

或（旧版本）：

```bash
docker-compose --version
```

- **如果显示版本号**，跳到第2.5步
- **如果提示找不到命令**，继续安装

**安装Docker Compose V2（推荐）：**

```bash
# Ubuntu系统
apt install -y docker-compose-plugin

# CentOS系统
yum install -y docker-compose-plugin
```

**或者从GitHub下载：**

```bash
# 下载Docker Compose
curl -L "https://github.com/docker/compose/releases/download/v2.29.1/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose

# 赋予执行权限
chmod +x /usr/local/bin/docker-compose

# 创建软链接（可选）
ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose

# 验证安装
docker-compose --version
```

#### 2.5 配置Docker镜像加速器（加速下载）

由于国内访问Docker Hub较慢，需要配置镜像加速器：

```bash
# 创建或编辑Docker配置文件
cat > /etc/docker/daemon.json <<EOF
{
  "registry-mirrors": [
    "https://docker.m.daocloud.io",
    "https://huecker.io",
    "https://dockerhub.timeweb.cloud",
    "https://registry.docker-cn.com"
  ],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  }
}
EOF

# 重启Docker使配置生效
systemctl restart docker

# 验证配置
docker info | grep -A 5 "Registry Mirrors"
```

看到镜像加速器地址列表表示成功。

---

### 第3步：上传项目代码到服务器

有两种方法上传代码，选择其中一种即可：

#### 方法A：使用Git拉取（推荐）

```bash
# 创建项目目录
mkdir -p /opt/gacha-system

# 进入目录
cd /opt/gacha-system

# 克隆代码仓库（替换成你的GitHub地址）
git clone https://github.com/hubuwei/gacha-system.git .
```

注意最后的 `.` 表示克隆到当前目录。

**如果提示需要登录：**
```bash
# 配置Git用户信息
git config --global user.name "你的名字"
git config --global user.email "你的邮箱"

# 如果是私有仓库，需要使用Token
# 在GitHub生成Personal Access Token后：
git clone https://你的用户名:你的Token@github.com/hubuwei/gacha-system.git .
```

#### 方法B：从本地上传（如果没有Git仓库）

**在本地PowerShell中执行：**

```powershell
# 压缩项目文件夹（排除不必要的文件）
cd E:\CFDemo\gacha-system

# 创建压缩包（使用7-Zip或WinRAR手动压缩，或使用PowerShell）
Compress-Archive -Path * -DestinationPath ..\gacha-system.zip -Force
```

**然后上传到服务器：**

```powershell
# 使用SCP上传（在本地PowerShell执行）
scp E:\CFDemo\gacha-system.zip root@你的服务器IP:/opt/
```

**在服务器上解压：**

```bash
# 连接到服务器后
cd /opt
mkdir -p gacha-system
unzip gacha-system.zip -d gacha-system/
cd gacha-system
```

如果没有unzip命令：

```bash
# 安装unzip
apt install -y unzip   # Ubuntu
yum install -y unzip   # CentOS
```

---

### 第4步：配置环境变量

#### 4.1 复制环境配置文件

```bash
cd /opt/gacha-system

# 复制示例配置文件
cp .env.docker .env
```

#### 4.2 编辑配置文件

```bash
# 使用nano编辑器（简单易用）
nano .env
```

你会看到以下内容：

```bash
# 数据库配置
DB_PASSWORD=Xc037417!
DB_NAME=gacha_system_prod

# Redis配置
REDIS_PASSWORD=Xc037417!

# RabbitMQ配置
RABBITMQ_USERNAME=admin
RABBITMQ_PASSWORD=Xc037417!

# 邮件配置
MAIL_USERNAME=1712133303@qq.com
MAIL_PASSWORD=wkokvlcppuuaehhd
```

**修改建议：**

1. **修改所有密码为强密码**（建议使用密码生成器）
2. **保留DB_NAME不变**（必须是 `gacha_system_prod`）
3. **修改邮件配置**为你的QQ邮箱和授权码

**如何获取QQ邮箱授权码：**

1. 登录QQ邮箱网页版
2. 点击"设置" → "账户"
3. 找到"POP3/IMAP/SMTP/Exchange/CardDAV/CalDAV服务"
4. 开启"IMAP/SMTP服务"
5. 点击"生成授权码"
6. 按提示发送短信，获得16位授权码
7. 将授权码填入 `MAIL_PASSWORD`

**编辑操作：**
- 使用方向键移动光标
- 直接输入修改内容
- 修改完成后按 `Ctrl + O` 保存
- 按 `Enter` 确认文件名
- 按 `Ctrl + X` 退出编辑器

#### 4.3 验证配置文件

```bash
# 查看配置文件内容
cat .env
```

确认修改已保存。

---

### 第5步：配置防火墙

#### 5.1 检查防火墙状态

**Ubuntu系统（UFW）：**

```bash
# 查看防火墙状态
ufw status
```

**CentOS系统（firewalld）：**

```bash
# 查看防火墙状态
systemctl status firewalld
```

#### 5.2 开放必要端口

**Ubuntu UFW：**

```bash
# 允许SSH（重要！不要关闭）
ufw allow 22/tcp

# 允许HTTP（前端访问）
ufw allow 80/tcp

# 允许后端服务端口
ufw allow 8081/tcp  # Mall Service
ufw allow 8082/tcp  # Game Service
ufw allow 8083/tcp  # Gacha Service
ufw allow 8084/tcp  # Auth Service

# 允许中间件端口（可选，生产环境建议只开放内网）
ufw allow 3307/tcp  # MySQL
ufw allow 6379/tcp  # Redis
ufw allow 9200/tcp  # Elasticsearch
ufw allow 15672/tcp # RabbitMQ管理界面

# 启用防火墙
ufw enable

# 重新加载规则
ufw reload

# 查看规则
ufw status numbered
```

**CentOS firewalld：**

```bash
# 开放端口
firewall-cmd --permanent --add-port=22/tcp
firewall-cmd --permanent --add-port=80/tcp
firewall-cmd --permanent --add-port=8081/tcp
firewall-cmd --permanent --add-port=8082/tcp
firewall-cmd --permanent --add-port=8083/tcp
firewall-cmd --permanent --add-port=8084/tcp
firewall-cmd --permanent --add-port=3307/tcp
firewall-cmd --permanent --add-port=6379/tcp
firewall-cmd --permanent --add-port=9200/tcp
firewall-cmd --permanent --add-port=15672/tcp

# 重载防火墙
firewall-cmd --reload

# 查看开放的端口
firewall-cmd --list-ports
```

**云服务器安全组配置（阿里云/腾讯云等）：**

如果你的服务器是云服务商提供的，还需要在控制台配置安全组：

1. 登录云服务商控制台
2. 找到"安全组"或"防火墙"设置
3. 添加入站规则，开放上述端口
4. 保存配置

---

### 第6步：停止旧服务（如果是重新部署）

如果你是第一次部署，可以跳过此步。

```bash
# 停止可能运行的JAR服务
pkill -f "mall-service.jar" || true
pkill -f "auth-service.jar" || true
pkill -f "game-service.jar" || true
pkill -f "gacha-service.jar" || true

# 停止旧的Docker容器
cd /opt/gacha-system
docker-compose down 2>/dev/null || true

# 清理旧镜像（可选，节省空间）
docker system prune -a -f
```

---

### 第7步：构建并启动Docker容器

#### 7.1 开始构建镜像

```bash
cd /opt/gacha-system

# 构建所有服务的Docker镜像
docker-compose build
```

**这个过程需要5-15分钟**，取决于网络速度和服务器性能。

你会看到类似这样的输出：

```
[+] Building 8/12
 => [mall-service internal] load build definition from Dockerfile
 => [mall-service] FROM docker.io/library/maven:3.8-openjdk-17
 => [mall-service] COPY pom.xml .
 => [mall-service] RUN mvn dependency:go-offline
 ...
```

**耐心等待，不要中断！**

#### 7.2 如果遇到Elasticsearch下载失败

Elasticsearch镜像有时会因为网络问题下载失败，有两种解决方案：

**方案A：临时移除镜像加速器直接下载**

```bash
# 备份配置文件
mv /etc/docker/daemon.json /etc/docker/daemon.json.bak

# 重启Docker
systemctl restart docker

# 手动拉取ES镜像
docker pull elasticsearch:7.17.29

# 恢复配置文件
mv /etc/docker/daemon.json.bak /etc/docker/daemon.json
systemctl restart docker

# 继续构建
docker-compose build
```

**方案B：暂时跳过Elasticsearch（不影响核心功能）**

```bash
# 编辑docker-compose.yml
nano docker-compose.yml
```

找到 `elasticsearch:` 部分，在每一行前面加 `#` 注释掉：

```yaml
#  elasticsearch:
#    image: elasticsearch:7.17.29
#    container_name: gacha-elasticsearch
#    ...
```

同时注释掉其他服务中的 `elasticsearch` 依赖：

```yaml
#      elasticsearch:
#        condition: service_healthy
```

保存后继续构建。

#### 7.3 启动所有服务

```bash
# 后台启动所有容器
docker-compose up -d
```

看到类似输出表示成功：

```
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
```

#### 7.4 等待服务启动

```bash
# 等待30秒让服务完全启动
sleep 30
```

---

### 第8步：验证部署结果

#### 8.1 查看所有容器状态

```bash
docker-compose ps
```

你应该看到所有容器的状态都是 `Up`：

```
NAME                    STATUS         PORTS
gacha-mysql             Up 2 minutes   0.0.0.0:3307->3306/tcp
gacha-redis             Up 2 minutes   0.0.0.0:6379->6379/tcp
gacha-rabbitmq          Up 2 minutes   0.0.0.0:5672->5672/tcp, 0.0.0.0:15672->15672/tcp
gacha-elasticsearch     Up 2 minutes   0.0.0.0:9200->9200/tcp
gacha-mall-service      Up 2 minutes   0.0.0.0:8081->8081/tcp
gacha-auth-service      Up 2 minutes   0.0.0.0:8084->8084/tcp
gacha-game-service      Up 2 minutes   0.0.0.0:8082->8082/tcp
gacha-gacha-service     Up 2 minutes   0.0.0.0:8083->8083/tcp
gacha-frontend          Up 2 minutes   0.0.0.0:80->80/tcp
```

如果有容器状态是 `Restarting` 或 `Exited`，说明有问题，需要查看日志。

#### 8.2 查看服务日志

```bash
# 查看Mall Service日志
docker-compose logs -f mall-service

# 查看Auth Service日志
docker-compose logs -f auth-service

# 查看所有服务日志
docker-compose logs -f
```

按 `Ctrl + C` 退出日志查看。

**成功的标志：** 看到类似 `Started Application in XX seconds` 的信息。

#### 8.3 测试API接口

```bash
# 测试Mall Service健康检查
curl http://localhost:8081/api/actuator/health

# 测试Auth Service健康检查
curl http://localhost:8084/actuator/health

# 测试Game Service健康检查
curl http://localhost:8082/actuator/health

# 测试Gacha Service健康检查
curl http://localhost:8083/actuator/health
```

应该返回JSON格式的健康状态信息。

#### 8.4 测试前端访问

在浏览器中访问：

```
http://你的服务器IP
```

例如：`http://111.228.12.167`

应该能看到游戏商城的前端页面。

#### 8.5 测试数据库连接

```bash
# 进入MySQL容器
docker exec -it gacha-mysql mysql -uroot -p

# 输入你在.env中设置的DB_PASSWORD密码

# 查看数据库
SHOW DATABASES;

# 应该看到 gacha_system_prod 数据库

# 退出
EXIT;
```

#### 8.6 测试Redis连接

```bash
# 进入Redis容器
docker exec -it gacha-redis redis-cli -a 你的REDIS_PASSWORD

# 测试
PING

# 应该返回 PONG

# 退出
EXIT
```

#### 8.7 测试RabbitMQ管理界面

在浏览器中访问：

```
http://你的服务器IP:15672
```

使用你在 `.env` 中设置的用户名和密码登录（默认 admin / 你的密码）。

---

## 🎉 部署成功！

如果以上所有测试都通过，恭喜你！部署成功了！

### 访问地址汇总

| 服务 | 访问地址 | 说明 |
|------|---------|------|
| 前端页面 | `http://你的服务器IP` | 游戏商城主页面 |
| Mall API | `http://你的服务器IP:8081/api` | 商城后端接口 |
| Auth API | `http://你的服务器IP:8084/api/auth` | 认证服务接口 |
| Game API | `http://你的服务器IP:8082/api/game` | 游戏服务接口 |
| Gacha API | `http://你的服务器IP:8083/api/gacha` | 抽卡服务接口 |
| RabbitMQ管理 | `http://你的服务器IP:15672` | 消息队列管理界面 |
| MySQL | `你的服务器IP:3307` | 数据库（外部访问） |
| Redis | `你的服务器IP:6379` | 缓存（外部访问） |
| Elasticsearch | `http://你的服务器IP:9200` | 搜索引擎 |

---

## 🛠️ 常用维护命令

### 查看服务状态

```bash
# 查看所有容器
docker-compose ps

# 查看某个容器的详细信息
docker inspect gacha-mall-service

# 查看资源使用情况
docker stats
```

### 查看日志

```bash
# 实时查看某个服务的日志
docker-compose logs -f mall-service

# 查看最近100行日志
docker-compose logs --tail=100 mall-service

# 查看所有服务日志
docker-compose logs -f
```

### 重启服务

```bash
# 重启单个服务
docker-compose restart mall-service

# 重启所有服务
docker-compose restart

# 停止并重新启动某个服务
docker-compose down mall-service
docker-compose up -d mall-service
```

### 停止服务

```bash
# 停止所有服务（保留数据）
docker-compose down

# 停止并删除数据卷（谨慎使用！会清空数据库）
docker-compose down -v
```

### 更新部署

```bash
# 进入项目目录
cd /opt/gacha-system

# 拉取最新代码
git pull

# 重新构建镜像
docker-compose build

# 重启服务
docker-compose up -d

# 查看日志确认启动成功
docker-compose logs -f
```

### 清理空间

```bash
# 清理未使用的镜像
docker image prune -a

# 清理未使用的容器
docker container prune

# 清理所有未使用的资源
docker system prune -a

# 查看磁盘占用
docker system df
```

### 进入容器内部

```bash
# 进入Mall Service容器
docker exec -it gacha-mall-service bash

# 进入MySQL容器
docker exec -it gacha-mysql bash

# 进入Redis容器
docker exec -it gacha-redis sh

# 退出容器
exit
```

---

## ⚠️ 常见问题排查

### 问题1：容器启动失败

```bash
# 查看具体错误信息
docker-compose logs 服务名

# 例如
docker-compose logs mall-service
```

常见原因：
- 端口被占用
- 数据库连接失败
- 内存不足

### 问题2：端口被占用

```bash
# 查看端口占用情况
netstat -tulpn | grep 8081

# 或者
ss -tulpn | grep 8081

# 杀死占用端口的进程
kill -9 进程ID
```

### 问题3：内存不足

```bash
# 查看内存使用情况
free -h

# 查看Docker资源占用
docker stats

# 如果内存不足，可以调整ES的内存配置
nano docker-compose.yml
# 修改 ES_JAVA_OPTS=-Xms256m -Xmx256m
```

### 问题4：数据库初始化失败

```bash
# 查看MySQL日志
docker-compose logs mysql

# 手动导入SQL文件
docker exec -i gacha-mysql mysql -uroot -p你的密码 gacha_system_prod < database/init.sql
```

### 问题5：前端页面无法访问

```bash
# 检查Nginx配置
docker exec -it gacha-frontend nginx -t

# 查看前端容器日志
docker-compose logs frontend

# 检查防火墙
ufw status
```

### 问题6：服务之间无法通信

```bash
# 检查Docker网络
docker network ls
docker network inspect gacha-system_gacha-network

# 测试容器间连通性
docker exec gacha-mall-service ping gacha-mysql
```

---

## 📊 资源占用参考

根据你的4核16GB服务器，预计资源占用：

| 服务 | CPU | 内存 | 磁盘 |
|------|-----|------|------|
| MySQL | 5-10% | 512MB | 2GB |
| Redis | 1-2% | 128MB | 500MB |
| RabbitMQ | 2-5% | 256MB | 500MB |
| Elasticsearch | 5-10% | 512MB | 1GB |
| Mall Service | 5-10% | 512MB | 200MB |
| Auth Service | 2-5% | 256MB | 100MB |
| Game Service | 2-5% | 256MB | 100MB |
| Gacha Service | 2-5% | 256MB | 100MB |
| Frontend | 1-2% | 64MB | 50MB |
| **总计** | **25-54%** | **~2.8GB** | **~4.5GB** |

**你的服务器配置完全够用！** ✅

---

## 🔒 安全建议

### 1. 修改默认密码

确保 `.env` 文件中所有密码都是强密码：
- 至少12位
- 包含大小写字母、数字、特殊字符
- 不要使用常见单词

### 2. 限制数据库访问

生产环境建议只允许内网访问数据库：

```bash
# 编辑docker-compose.yml
nano docker-compose.yml

# 修改MySQL端口映射，只绑定到127.0.0.1
ports:
  - "127.0.0.1:3307:3306"
```

### 3. 配置HTTPS

使用Let's Encrypt免费SSL证书：

```bash
# 安装certbot
apt install -y certbot python3-certbot-nginx

# 申请证书
certbot --nginx -d 你的域名.com

# 自动续期
crontab -e
# 添加：0 0 1 * * certbot renew --quiet
```

### 4. 定期备份

```bash
# 创建备份脚本
cat > /opt/gacha-system/backup.sh <<'EOF'
#!/bin/bash
BACKUP_DIR="/opt/backups"
DATE=$(date +%Y%m%d_%H%M%S)
mkdir -p $BACKUP_DIR

# 备份数据库
docker exec gacha-mysql mysqldump -uroot -p$DB_PASSWORD gacha_system_prod > $BACKUP_DIR/db_$DATE.sql

# 压缩备份
tar -czf $BACKUP_DIR/backup_$DATE.tar.gz $BACKUP_DIR/db_$DATE.sql

# 删除7天前的备份
find $BACKUP_DIR -name "backup_*.tar.gz" -mtime +7 -delete

echo "Backup completed: backup_$DATE.tar.gz"
EOF

chmod +x /opt/gacha-system/backup.sh

# 添加到定时任务（每天凌晨2点备份）
crontab -e
# 添加：0 2 * * * /opt/gacha-system/backup.sh
```

---

## 📞 获取帮助

如果遇到问题：

1. **查看日志**：`docker-compose logs -f 服务名`
2. **检查文档**：查看项目根目录的README.md
3. **搜索错误信息**：将错误信息复制到搜索引擎
4. **检查GitHub Issues**：查看是否有类似问题

---

## ✅ 部署检查清单

部署完成后，逐项检查：

- [ ] 所有容器状态为 `Up`
- [ ] 前端页面可以正常访问
- [ ] 用户可以注册和登录
- [ ] 商品列表可以正常显示
- [ ] 可以正常下单购买
- [ ] 数据库中有初始数据
- [ ] Redis缓存正常工作
- [ ] RabbitMQ消息队列正常运行
- [ ] 日志没有ERROR级别的错误
- [ ] 防火墙规则正确配置
- [ ] 已设置定期备份

---

**祝你部署顺利！🎉**

如有问题，请查看详细日志并根据错误信息进行排查。
