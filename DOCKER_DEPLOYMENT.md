# Docker 容器化部署完整指南

## 🎯 概述

本项目已完成完整的Docker容器化配置，支持**一键部署**所有服务（包括中间件）。

---

## 📦 包含的服务

### 后端服务
- ✅ **mall-service** (8081) - 商城服务
- ✅ **auth-service** (8084) - 认证服务
- ✅ **game-service** (8082) - 游戏服务
- ✅ **gacha-service** (8083) - 抽卡服务

### 前端服务
- ✅ **frontend** (80) - React前端（Nginx托管）

### 中间件
- ✅ **MySQL 8.0** (3306) - 数据库
- ✅ **Redis 7** (6379) - 缓存
- ✅ **RabbitMQ 3** (5672, 15672) - 消息队列
- ✅ **Elasticsearch 7.17.29** (9200) - 搜索引擎

---

## 🚀 快速开始

### 本地开发环境（Windows）

#### 前置要求

1. **安装Docker Desktop**
   ```
   下载地址: https://www.docker.com/products/docker-desktop
   要求: Windows 10/11 专业版或家庭版
   ```

2. **启动Docker Desktop**
   - 打开Docker Desktop应用
   - 等待状态变为"Running"

#### 一键部署

```powershell
# 1. 克隆项目（如果还没有）
git clone https://github.com/hubuwei/gacha-system.git
cd gacha-system

# 2. 配置环境变量
Copy-Item .env.docker .env
notepad .env  # 修改密码等配置

# 3. 一键启动所有服务
.\docker-deploy.ps1 up
```

**就这么简单！** 🎉

等待约2-5分钟（首次构建较慢），然后访问：
- 前端：http://localhost
- API：http://localhost:8081/api
- RabbitMQ管理：http://localhost:15672 (admin/你的密码)

---

### 服务器部署（Linux）

#### 步骤1：安装Docker和Docker Compose

```bash
# 安装Docker
curl -fsSL https://get.docker.com | bash

# 启动Docker
sudo systemctl start docker
sudo systemctl enable docker

# 安装Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# 验证安装
docker --version
docker-compose --version
```

#### 步骤2：上传项目到服务器

```bash
# 方式A：使用Git
cd /opt
git clone https://github.com/hubuwei/gacha-system.git
cd gacha-system

# 方式B：使用SCP
scp -r gacha-system user@server:/opt/
```

#### 步骤3：配置环境变量

```bash
cd /opt/gacha-system
cp .env.docker .env
nano .env  # 编辑配置
```

需要修改的配置：
```bash
DB_PASSWORD=你的强密码
REDIS_PASSWORD=你的强密码
RABBITMQ_PASSWORD=你的强密码
MAIL_PASSWORD=你的QQ邮箱授权码
```

#### 步骤4：一键部署

```bash
# 赋予执行权限
chmod +x docker-deploy.sh

# 一键启动
sudo ./docker-deploy.sh up
```

---

## 📖 常用命令

### 查看所有服务状态
```bash
# Linux
./docker-deploy.sh status

# Windows
.\docker-deploy.ps1 status

# 或直接使用
docker compose ps
```

### 查看日志
```bash
# 查看所有服务日志
./docker-deploy.sh logs

# 查看特定服务日志
./docker-deploy.sh logs mall-service

# 或使用
docker compose logs -f mall-service
```

### 重启服务
```bash
# 重启所有服务
./docker-deploy.sh restart

# 重启单个服务
docker compose restart mall-service
```

### 停止服务
```bash
# 停止并删除容器（保留数据）
./docker-deploy.sh down

# 停止并删除容器和数据卷（⚠️ 谨慎使用）
docker compose down -v
```

### 重新构建
```bash
# 代码修改后重新构建
./docker-deploy.sh rebuild
```

---

## 🔧 配置说明

### 环境变量 (.env)

```bash
# 数据库
DB_PASSWORD=your_password          # MySQL root密码
DB_NAME=gacha_system_prod          # 数据库名称

# Redis
REDIS_PASSWORD=your_password       # Redis密码

# RabbitMQ
RABBITMQ_USERNAME=admin            # RabbitMQ用户名
RABBITMQ_PASSWORD=your_password    # RabbitMQ密码

# 邮件
MAIL_USERNAME=1712133303@qq.com    # QQ邮箱
MAIL_PASSWORD=your_auth_code       # 邮箱授权码
```

### 端口映射

| 服务 | 容器端口 | 主机端口 | 说明 |
|------|---------|---------|------|
| Frontend | 80 | 80 | 前端网页 |
| Mall Service | 8081 | 8081 | 商城API |
| Auth Service | 8084 | 8084 | 认证API |
| Game Service | 8082 | 8082 | 游戏API |
| Gacha Service | 8083 | 8083 | 抽卡API |
| MySQL | 3306 | 3306 | 数据库 |
| Redis | 6379 | 6379 | 缓存 |
| RabbitMQ | 5672 | 5672 | AMQP协议 |
| RabbitMQ Management | 15672 | 15672 | 管理界面 |
| Elasticsearch | 9200 | 9200 | 搜索引擎 |

---

## 📊 资源占用

### 内存占用（估算）

| 服务 | 内存占用 |
|------|---------|
| MySQL | ~512MB |
| Redis | ~128MB |
| RabbitMQ | ~256MB |
| Elasticsearch | ~512MB |
| Mall Service | ~512MB |
| Auth Service | ~256MB |
| Game Service | ~256MB |
| Gacha Service | ~256MB |
| Frontend | ~64MB |
| **总计** | **~2.7GB** |

**你的配置（32GB）完全足够！** ✅

### CPU占用

- 空闲状态：~5-10%
- 正常请求：~20-40%
- 高并发：~60-80%

---

## 🗄️ 数据持久化

所有重要数据都通过Docker Volume持久化：

```yaml
volumes:
  mysql-data      # MySQL数据库文件
  redis-data      # Redis数据
  rabbitmq-data   # RabbitMQ消息
  es-data         # Elasticsearch索引
  mall-logs       # Mall服务日志
  auth-logs       # Auth服务日志
  auth-uploads    # 用户上传文件
  game-logs       # Game服务日志
  gacha-logs      # Gacha服务日志
```

**数据位置**（Linux）：
```bash
/var/lib/docker/volumes/
```

**备份数据**：
```bash
# 备份MySQL数据
docker run --rm -v gacha-system_mysql-data:/data -v $(pwd):/backup alpine tar czf /backup/mysql-backup.tar.gz /data

# 恢复数据
docker run --rm -v gacha-system_mysql-data:/data -v $(pwd):/backup alpine tar xzf /backup/mysql-backup.tar.gz -C /
```

---

## 🐛 故障排查

### 问题1：服务启动失败

```bash
# 1. 查看具体错误
docker compose logs [service-name]

# 2. 检查依赖服务是否正常
docker compose ps

# 3. 检查端口是否被占用
netstat -tlnp | grep 8081
```

### 问题2：数据库连接失败

```bash
# 检查MySQL是否启动
docker compose logs mysql

# 进入MySQL容器调试
docker exec -it gacha-mysql mysql -uroot -p
```

### 问题3：图片无法访问

确保GamePapers目录存在且有权限：
```bash
ls -la GamePapers/
chmod -R 755 GamePapers/
```

### 问题4：内存不足

```bash
# 查看各容器内存占用
docker stats

# 限制某个服务的内存（修改docker-compose.yml）
deploy:
  resources:
    limits:
      memory: 512M
```

### 问题5：重建镜像不生效

```bash
# 清除缓存重新构建
docker compose build --no-cache
docker compose up -d
```

---

## 🔐 安全建议

### 生产环境必做

1. **修改默认密码**
   ```bash
   # .env文件中设置强密码
   DB_PASSWORD=VeryStrongPassword123!
   REDIS_PASSWORD=AnotherStrongPassword!
   ```

2. **限制端口暴露**
   ```yaml
   # 只暴露必要的端口
   ports:
     - "127.0.0.1:3306:3306"  # 只允许本地访问
   ```

3. **使用HTTPS**
   - 配置Nginx SSL证书
   - 使用Let's Encrypt免费证书

4. **定期更新镜像**
   ```bash
   docker compose pull
   docker compose up -d
   ```

5. **备份数据**
   ```bash
   # 设置定时备份（crontab）
   0 2 * * * /opt/gacha-system/backup.sh
   ```

---

## 📈 性能优化

### JVM参数调优

在Dockerfile中已配置：
```dockerfile
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"
```

可根据实际情况调整：
- `-Xms`: 初始堆大小
- `-Xmx`: 最大堆大小
- `-XX:+UseG1GC`: 使用G1垃圾收集器

### Nginx优化

已在nginx.conf中配置：
- Gzip压缩
- 静态资源缓存
- 连接超时设置

### 数据库优化

```sql
-- 添加索引
CREATE INDEX idx_game_name ON games(name);
CREATE INDEX idx_user_email ON users(email);
```

---

## 🔄 CI/CD集成

### GitHub Actions示例

```yaml
name: Docker Build and Deploy

on:
  push:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      
      - name: Build and Deploy
        run: |
          docker compose build
          docker compose up -d
```

---

## 📝 开发工作流

### 本地开发流程

1. **修改代码**
   ```bash
   # 编辑源代码
   nano mall-service/src/main/java/...
   ```

2. **重新构建并启动**
   ```bash
   ./docker-deploy.sh rebuild
   ```

3. **测试功能**
   ```
   浏览器访问: http://localhost
   ```

4. **提交代码**
   ```bash
   git add .
   git commit -m "描述修改"
   git push
   ```

---

## 🎓 学习资源

- [Docker官方文档](https://docs.docker.com/)
- [Docker Compose文档](https://docs.docker.com/compose/)
- [Docker最佳实践](https://docs.docker.com/develop/dev-best-practices/)

---

## 🆘 获取帮助

遇到问题？

1. 查看日志：`./docker-deploy.sh logs`
2. 检查状态：`./docker-deploy.sh status`
3. 查阅文档：[DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)
4. 提交Issue：https://github.com/hubuwei/gacha-system/issues

---

**祝你部署顺利！** 🚀
