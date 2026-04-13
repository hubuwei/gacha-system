# Mall Service 生产环境部署指南

## 📦 打包信息

- **打包时间**: 2026-04-12
- **服务名称**: mall-service
- **版本号**: 1.0.0-SNAPSHOT
- **JAR 文件大小**: 114.75 MB
- **配置文件**: application-prod.yml
- **环境变量模板**: .env.prod

---

## 📋 目录结构

```
deploy-packages/
├── mall-service.jar              # 主应用程序 JAR 文件
├── MALL_SERVICE_DEPLOY_NOTES.md  # 本部署文档
└── (其他服务的 JAR 文件)
```

---

## 🔧 部署前准备

### 1. 数据库准备

#### 1.1 创建生产数据库

在生产环境的 MySQL 中执行以下 SQL：

```sql
-- 创建生产数据库
CREATE DATABASE IF NOT EXISTS gacha_system_prod 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_unicode_ci;

-- 创建专用用户（建议）
CREATE USER 'mall_user'@'localhost' IDENTIFIED BY 'StrongPassword123!';
GRANT ALL PRIVILEGES ON gacha_system_prod.* TO 'mall_user'@'localhost';
FLUSH PRIVILEGES;
```

#### 1.2 初始化数据库表结构

执行项目中的数据库初始化脚本：

```bash
# 在项目根目录执行
mysql -u root -p gacha_system_prod < database/init-production.sql

# 或者使用 mall_user 用户
mysql -u mall_user -p gacha_system_prod < database/init-production.sql
```

**重要提示**: 
- 确保 `database/init-production.sql` 包含所有必要的表结构
- 检查是否包含游戏数据、商品数据等初始数据
- 生产环境不要使用 `ddl-auto: create`，应该使用 `update` 或 `none`

---

### 2. 中间件准备

确保以下中间件已在生产环境安装并运行：

#### 2.1 MySQL 8.0+

```bash
# 检查 MySQL 状态
systemctl status mysql

# 如果未安装，参考官方文档安装
# https://dev.mysql.com/doc/refman/8.0/en/linux-installation.html
```

**配置要求**:
- 端口: 3306
- 字符集: utf8mb4
- 时区: Asia/Shanghai

#### 2.2 Redis

```bash
# 检查 Redis 状态
systemctl status redis

# 启动 Redis
systemctl start redis

# 设置密码（在 redis.conf 中）
requirepass Xc037417!
```

**配置要求**:
- 端口: 6379
- 密码: Xc037417!（生产环境请修改为更强的密码）
- 启用键空间通知（用于订单超时自动取消）

在 `redis.conf` 中添加：
```conf
notify-keyspace-events Ex
```

#### 2.3 RabbitMQ

```bash
# 检查 RabbitMQ 状态
systemctl status rabbitmq-server

# 启动 RabbitMQ
systemctl start rabbitmq-server

# 启用管理插件
rabbitmq-plugins enable rabbitmq_management

# 创建用户
rabbitmqctl add_user admin Xc037417!
rabbitmqctl set_user_tags admin administrator
rabbitmqctl set_permissions -p / admin ".*" ".*" ".*"
```

**配置要求**:
- 端口: 5672 (AMQP), 15672 (管理界面)
- 用户名: admin
- 密码: Xc037417!（生产环境请修改）
- Virtual Host: /

访问管理界面: `http://your-server-ip:15672`

#### 2.4 Elasticsearch 7.17.x

```bash
# 检查 Elasticsearch 状态
systemctl status elasticsearch

# 启动 Elasticsearch
systemctl start elasticsearch
```

**配置要求**:
- 版本: 7.17.x（必须与 IK 分词器版本匹配）
- 端口: 9200
- 安装 IK 分词器插件

安装 IK 分词器：
```bash
# 进入 Elasticsearch 插件目录
cd /usr/share/elasticsearch/bin

# 安装 IK 分词器（版本必须与 ES 一致）
./elasticsearch-plugin install https://github.com/medcl/elasticsearch-analysis-ik/releases/download/v7.17.29/elasticsearch-analysis-ik-7.17.29.zip

# 重启 Elasticsearch
systemctl restart elasticsearch
```

验证 IK 分词器：
```bash
curl -X POST "localhost:9200/_analyze" -H 'Content-Type: application/json' -d'
{
  "analyzer": "ik_max_word",
  "text": "游戏商城"
}'
```

---

### 3. 环境变量配置

#### 3.1 复制并编辑环境变量文件

```bash
# 上传 .env.prod 到服务器后
cd /opt/gacha-system
cp .env.prod .env

# 编辑配置文件
vim .env
```

#### 3.2 必须修改的配置项

**⚠️ 安全警告**: 以下配置项在生产环境中**必须**修改为真实值：

##### JWT 密钥（高优先级）
```bash
# 生成强随机密钥
openssl rand -base64 64

# 将生成的密钥填入
JWT_SECRET=your-generated-strong-secret-key-here
```

##### 阿里云短信配置
```bash
SMS_ACCESS_KEY_ID=你的阿里云AccessKey ID
SMS_ACCESS_KEY_SECRET=你的阿里云AccessKey Secret
SMS_SIGN_NAME=游戏商城
SMS_TEMPLATE_CODE=SMS_你的模板代码
```

获取方式：
1. 登录阿里云控制台
2. 进入「短信服务」
3. 创建 AccessKey
4. 创建签名和模板

##### 阿里云 OSS 配置
```bash
OSS_ACCESS_KEY_ID=你的阿里云AccessKey ID
OSS_ACCESS_KEY_SECRET=你的阿里云AccessKey Secret
OSS_BUCKET_NAME=game-mall
OSS_ENDPOINT=oss-cn-hangzhou.aliyuncs.com
OSS_BASE_URL=https://game-mall.oss-cn-hangzhou.aliyuncs.com
```

获取方式：
1. 登录阿里云控制台
2. 进入「对象存储 OSS」
3. 创建 Bucket
4. 获取 Endpoint 和域名

##### 微信支付配置
```bash
WECHAT_PAY_APP_ID=你的应用ID
WECHAT_PAY_MCH_ID=你的商户号
WECHAT_PAY_API_V3_KEY=你的APIv3密钥
WECHAT_PAY_SERIAL_NO=你的证书序列号
WECHAT_PAY_NOTIFY_URL=https://你的域名.com/api/payment/wechat/notify
```

获取方式：
1. 登录微信支付商户平台
2. 进入「账户中心」->「API安全」
3. 申请 API 证书
4. 设置 APIv3 密钥

##### 邮件配置
```bash
MAIL_HOST=smtp.qq.com
MAIL_PORT=587
MAIL_USERNAME=1712133303@qq.com
MAIL_PASSWORD=你的授权码
```

获取 QQ 邮箱授权码：
1. 登录 QQ 邮箱
2. 设置 -> 账户
3. 开启 SMTP 服务
4. 生成授权码

##### 数据库密码（建议修改）
```bash
DB_PASSWORD=你的强密码
```

##### Redis 密码（建议修改）
```bash
REDIS_PASSWORD=你的强密码
```

##### RabbitMQ 密码（建议修改）
```bash
RABBITMQ_PASSWORD=你的强密码
```

---

### 4. 创建必要的目录

```bash
# 创建应用目录
sudo mkdir -p /opt/gacha-system
sudo mkdir -p /opt/gacha-system/logs
sudo mkdir -p /opt/gacha-system/uploads/avatars

# 设置权限
sudo chown -R $USER:$USER /opt/gacha-system
chmod 755 /opt/gacha-system
```

---

## 🚀 部署步骤

### 方式一：直接部署（推荐初学者）

#### Step 1: 上传文件到服务器

```bash
# 从本地 Windows 机器上传
scp E:\CFDemo\gacha-system\deploy-packages\mall-service.jar user@your-server:/opt/gacha-system/
scp E:\CFDemo\gacha-system\mall-service\.env.prod user@your-server:/opt/gacha-system/.env
```

或者使用 FTP/SFTP 工具（如 WinSCP、FileZilla）。

#### Step 2: 连接服务器并配置

```bash
# SSH 连接到服务器
ssh user@your-server

# 进入应用目录
cd /opt/gacha-system

# 确认文件已上传
ls -lh
# 应该看到:
# - mall-service.jar
# - .env
```

#### Step 3: 加载环境变量并启动

```bash
# 方法 A: 手动加载环境变量
export $(cat .env | grep -v '^#' | xargs)

# 启动服务
nohup java -jar \
  -Dspring.profiles.active=prod \
  -Xms512m \
  -Xmx1024m \
  mall-service.jar > logs/mall-startup.log 2>&1 &

# 记录进程 ID
echo $! > mall-service.pid

# 查看启动日志
tail -f logs/mall-startup.log
```

```bash
# 方法 B: 使用启动脚本（推荐）
# 创建启动脚本
cat > start.sh << 'EOF'
#!/bin/bash

# 加载环境变量
set -a
source .env
set +a

# JVM 参数
JVM_OPTS="-Xms512m -Xmx1024m"
SPRING_OPTS="-Dspring.profiles.active=prod"

# 启动应用
nohup java $JVM_OPTS $SPRING_OPTS -jar mall-service.jar > logs/mall-startup.log 2>&1 &

# 保存 PID
echo $! > mall-service.pid

echo "Mall Service started with PID: $!"
echo "Check logs: tail -f logs/mall-startup.log"
EOF

# 赋予执行权限
chmod +x start.sh

# 启动服务
./start.sh
```

#### Step 4: 验证服务状态

```bash
# 检查进程是否运行
ps aux | grep mall-service

# 检查端口监听
netstat -tlnp | grep 8081
# 或
ss -tlnp | grep 8081

# 健康检查
curl http://localhost:8081/api/actuator/health

# 预期响应:
# {"status":"UP"}
```

#### Step 5: 测试 API

```bash
# 测试首页接口
curl http://localhost:8081/api/

# 测试商品列表
curl http://localhost:8081/api/products

# 查看应用日志
tail -f /opt/gacha-system/logs/mall.log
```

---

### 方式二：Systemd 服务部署（推荐生产环境）

#### Step 1: 创建 systemd 服务文件

```bash
sudo vim /etc/systemd/system/mall-service.service
```

添加以下内容：

```ini
[Unit]
Description=Mall Service - Game Shopping Platform
After=network.target mysql.service redis.service rabbitmq-server.service elasticsearch.service

[Service]
Type=simple
User=www-data
Group=www-data
WorkingDirectory=/opt/gacha-system

# 加载环境变量
EnvironmentFile=/opt/gacha-system/.env

# JVM 参数
ExecStart=/usr/bin/java \
    -Xms512m \
    -Xmx1024m \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -Dspring.profiles.active=prod \
    -jar /opt/gacha-system/mall-service.jar

# 重启策略
Restart=on-failure
RestartSec=10

# 日志
StandardOutput=append:/opt/gacha-system/logs/mall-startup.log
StandardError=append:/opt/gacha-system/logs/mall-error.log

# 安全限制
LimitNOFILE=65536

[Install]
WantedBy=multi-user.target
```

#### Step 2: 启用并启动服务

```bash
# 重新加载 systemd 配置
sudo systemctl daemon-reload

# 启用开机自启
sudo systemctl enable mall-service

# 启动服务
sudo systemctl start mall-service

# 查看服务状态
sudo systemctl status mall-service

# 查看实时日志
sudo journalctl -u mall-service -f
```

#### Step 3: 常用管理命令

```bash
# 停止服务
sudo systemctl stop mall-service

# 重启服务
sudo systemctl restart mall-service

# 查看状态
sudo systemctl status mall-service

# 查看日志
sudo journalctl -u mall-service -n 100

# 查看最近 1 小时的错误日志
sudo journalctl -u mall-service --since "1 hour ago" -p err
```

---

### 方式三：Docker 部署

#### Step 1: 创建 Dockerfile

```bash
cd /opt/gacha-system
vim Dockerfile
```

添加以下内容：

```dockerfile
FROM openjdk:17-jdk-slim

LABEL maintainer="your-email@example.com"
LABEL description="Mall Service for Game Shopping Platform"

WORKDIR /app

# 复制 JAR 文件
COPY mall-service.jar app.jar

# 复制环境变量文件
COPY .env .env

# 暴露端口
EXPOSE 8081

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
    CMD curl -f http://localhost:8081/api/actuator/health || exit 1

# 启动命令
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
```

#### Step 2: 创建 docker-compose.yml

```bash
vim docker-compose.yml
```

添加以下内容：

```yaml
version: '3.8'

services:
  mall-service:
    build: .
    container_name: mall-service
    ports:
      - "8081:8081"
    env_file:
      - .env
    volumes:
      - ./logs:/app/logs
      - ./uploads:/app/uploads
    networks:
      - gacha-network
    depends_on:
      - mysql
      - redis
      - rabbitmq
      - elasticsearch
    restart: unless-stopped
    deploy:
      resources:
        limits:
          memory: 2G
          cpus: '2.0'

  mysql:
    image: mysql:8.0
    container_name: mall-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root-password
      MYSQL_DATABASE: gacha_system_prod
      MYSQL_USER: mall_user
      MYSQL_PASSWORD: StrongPassword123!
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
      - ./database/init-production.sql:/docker-entrypoint-initdb.d/init.sql
    networks:
      - gacha-network
    restart: unless-stopped

  redis:
    image: redis:7-alpine
    container_name: mall-redis
    command: redis-server --requirepass Xc037417! --notify-keyspace-events Ex
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    networks:
      - gacha-network
    restart: unless-stopped

  rabbitmq:
    image: rabbitmq:3-management
    container_name: mall-rabbitmq
    environment:
      RABBITMQ_DEFAULT_USER: admin
      RABBITMQ_DEFAULT_PASS: Xc037417!
    ports:
      - "5672:5672"
      - "15672:15672"
    volumes:
      - rabbitmq-data:/var/lib/rabbitmq
    networks:
      - gacha-network
    restart: unless-stopped

  elasticsearch:
    image: elasticsearch:7.17.29
    container_name: mall-elasticsearch
    environment:
      - discovery.type=single-node
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
    ports:
      - "9200:9200"
    volumes:
      - elasticsearch-data:/usr/share/elasticsearch/data
    networks:
      - gacha-network
    restart: unless-stopped

volumes:
  mysql-data:
  redis-data:
  rabbitmq-data:
  elasticsearch-data:

networks:
  gacha-network:
    driver: bridge
```

#### Step 3: 构建并启动

```bash
# 构建镜像
docker-compose build

# 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f mall-service

# 查看服务状态
docker-compose ps
```

---

## 🔍 监控与维护

### 1. 日志管理

#### 查看日志

```bash
# 实时查看启动日志
tail -f /opt/gacha-system/logs/mall-startup.log

# 实时查看应用日志
tail -f /opt/gacha-system/logs/mall.log

# 查看错误日志
tail -f /opt/gacha-system/logs/mall-error.log

# 搜索特定错误
grep "ERROR" /opt/gacha-system/logs/mall.log

# 查看最近 100 行
tail -n 100 /opt/gacha-system/logs/mall.log

# 按日期查看历史日志
ls -lh /opt/gacha-system/logs/
```

#### 日志轮转（防止日志文件过大）

创建日志轮转配置：

```bash
sudo vim /etc/logrotate.d/mall-service
```

添加：

```conf
/opt/gacha-system/logs/*.log {
    daily
    rotate 30
    compress
    delaycompress
    missingok
    notifempty
    create 0644 www-data www-data
    sharedscripts
    postrotate
        systemctl reload mall-service > /dev/null 2>&1 || true
    endscript
}
```

---

### 2. 性能监控

#### JVM 监控

```bash
# 获取进程 ID
PID=$(cat /opt/gacha-system/mall-service.pid)

# 查看 JVM 内存使用
jmap -heap $PID

# 查看 GC 统计
jstat -gc $PID 1000

# 查看线程信息
jstack $PID | head -100
```

#### 系统资源监控

```bash
# CPU 和内存使用
top -p $PID

# 磁盘使用
df -h /opt/gacha-system

# 网络连接
netstat -an | grep 8081 | wc -l
```

#### Actuator 端点

```bash
# 健康检查
curl http://localhost:8081/api/actuator/health

# 指标信息
curl http://localhost:8081/api/actuator/metrics

# JVM 信息
curl http://localhost:8081/api/actuator/metrics/jvm.memory.used

# HTTP 请求统计
curl http://localhost:8081/api/actuator/metrics/http.server.requests
```

---

### 3. 数据库维护

#### 备份数据库

```bash
# 完整备份
mysqldump -u mall_user -p gacha_system_prod > backup_$(date +%Y%m%d_%H%M%S).sql

# 压缩备份
mysqldump -u mall_user -p gacha_system_prod | gzip > backup_$(date +%Y%m%d_%H%M%S).sql.gz

# 定时备份（添加到 crontab）
crontab -e

# 每天凌晨 2 点备份
0 2 * * * mysqldump -u mall_user -p'YourPassword' gacha_system_prod | gzip > /backup/mall_$(date +\%Y\%m\%d).sql.gz
```

#### 恢复数据库

```bash
# 从备份恢复
gunzip < backup_20260412.sql.gz | mysql -u mall_user -p gacha_system_prod

# 或直接导入 SQL 文件
mysql -u mall_user -p gacha_system_prod < backup.sql
```

---

### 4. 常见问题排查

#### 问题 1: 服务无法启动

```bash
# 检查日志
tail -100 /opt/gacha-system/logs/mall-startup.log

# 常见原因:
# 1. 端口被占用
netstat -tlnp | grep 8081

# 2. 数据库连接失败
mysql -u mall_user -p -h localhost

# 3. Redis 连接失败
redis-cli -h localhost -a Xc037417! ping

# 4. 环境变量未正确加载
cat /opt/gacha-system/.env
```

#### 问题 2: 数据库连接超时

```bash
# 检查 MySQL 状态
systemctl status mysql

# 检查防火墙
sudo ufw status
sudo ufw allow 3306/tcp

# 检查 MySQL 最大连接数
mysql -u root -p -e "SHOW VARIABLES LIKE 'max_connections';"
```

#### 问题 3: Redis 连接失败

```bash
# 检查 Redis 状态
systemctl status redis

# 测试连接
redis-cli -h localhost -a Xc037417! ping

# 检查 Redis 配置
grep "requirepass" /etc/redis/redis.conf
grep "notify-keyspace-events" /etc/redis/redis.conf
```

#### 问题 4: Elasticsearch 索引创建失败

```bash
# 检查 ES 状态
curl http://localhost:9200/_cluster/health?pretty

# 检查 IK 分词器
curl -X POST "localhost:9200/_analyze" -H 'Content-Type: application/json' -d'
{
  "analyzer": "ik_max_word",
  "text": "测试中文分词"
}'

# 重建索引
curl -X DELETE "localhost:9200/products"
# 重启应用会自动重建索引
```

#### 问题 5: 内存溢出 (OOM)

```bash
# 增加 JVM 堆内存
# 编辑 systemd 服务文件或启动脚本
-Xms1024m -Xmx2048m

# 监控内存使用
jstat -gcutil $PID 1000

# 分析堆转储
jmap -dump:format=b,file=heap.hprof $PID
```

---

## 🔒 安全加固

### 1. 防火墙配置

```bash
# 只开放必要端口
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 80/tcp    # HTTP
sudo ufw allow 443/tcp   # HTTPS
sudo ufw allow 8081/tcp  # Mall Service

# 禁止直接访问中间件端口（仅允许本地访问）
sudo ufw deny 3306/tcp   # MySQL
sudo ufw deny 6379/tcp   # Redis
sudo ufw deny 5672/tcp   # RabbitMQ
sudo ufw deny 9200/tcp   # Elasticsearch

# 启用防火墙
sudo ufw enable
```

### 2. HTTPS 配置（使用 Nginx 反向代理）

```bash
# 安装 Nginx
sudo apt-get install nginx

# 安装 Certbot
sudo apt-get install certbot python3-certbot-nginx

# 获取 SSL 证书
sudo certbot --nginx -d your-domain.com

# Nginx 配置
sudo vim /etc/nginx/sites-available/mall-service
```

Nginx 配置示例：

```nginx
server {
    listen 80;
    server_name your-domain.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name your-domain.com;

    ssl_certificate /etc/letsencrypt/live/your-domain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;

    location /api/ {
        proxy_pass http://localhost:8081/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # 超时设置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
}
```

```bash
# 启用配置
sudo ln -s /etc/nginx/sites-available/mall-service /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

### 3. 定期更新

```bash
# 更新系统
sudo apt-get update && sudo apt-get upgrade -y

# 更新 Java
sudo apt-get install openjdk-17-jdk

# 重新部署应用
# 上传新的 JAR 文件后
sudo systemctl restart mall-service
```

---

## 📊 性能优化建议

### 1. JVM 调优

根据服务器配置调整 JVM 参数：

```bash
# 4GB 内存服务器
-Xms2g -Xmx2g -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m

# 8GB 内存服务器
-Xms4g -Xmx4g -XX:MetaspaceSize=512m -XX:MaxMetaspaceSize=1g

# 启用 G1 GC
-XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:G1ReservePercent=10
```

### 2. 数据库优化

```sql
-- 添加索引
ALTER TABLE products ADD INDEX idx_category (category_id);
ALTER TABLE orders ADD INDEX idx_user_status (user_id, status);
ALTER TABLE order_items ADD INDEX idx_order (order_id);

-- 优化查询
EXPLAIN SELECT * FROM products WHERE category_id = 1;
```

### 3. Redis 缓存优化

```bash
# 设置最大内存
maxmemory 2gb

# 设置淘汰策略
maxmemory-policy allkeys-lru
```

### 4. 连接池优化

在 `.env` 中调整：

```bash
# HikariCP 配置
DB_POOL_MIN_IDLE=10
DB_POOL_MAX_SIZE=50

# Redis 连接池
REDIS_POOL_MAX_ACTIVE=50
REDIS_POOL_MAX_IDLE=20
```

---

## 🆘 技术支持

### 日志位置

- 启动日志: `/opt/gacha-system/logs/mall-startup.log`
- 应用日志: `/opt/gacha-system/logs/mall.log`
- 错误日志: `/opt/gacha-system/logs/mall-error.log`
- Systemd 日志: `journalctl -u mall-service`

### 关键配置文件

- 环境变量: `/opt/gacha-system/.env`
- 应用配置: JAR 包内的 `application-prod.yml`
- Systemd 服务: `/etc/systemd/system/mall-service.service`

### 常用命令速查

```bash
# 启动服务
sudo systemctl start mall-service

# 停止服务
sudo systemctl stop mall-service

# 重启服务
sudo systemctl restart mall-service

# 查看状态
sudo systemctl status mall-service

# 查看日志
sudo journalctl -u mall-service -f

# 备份数据库
mysqldump -u mall_user -p gacha_system_prod | gzip > backup.sql.gz

# 检查端口
netstat -tlnp | grep 8081

# 健康检查
curl http://localhost:8081/api/actuator/health
```

---

## 📝 更新日志

- **2026-04-12**: 初始版本，支持生产环境部署
  - 集成 MySQL、Redis、RabbitMQ、Elasticsearch
  - 支持微信支付、支付宝支付
  - 支持短信通知、邮件通知
  - 支持商品搜索、订单管理

---

## ⚠️ 注意事项

1. **安全性**
   - ✅ 务必修改所有默认密码
   - ✅ 使用 HTTPS 保护 API 通信
   - ✅ 定期更新 JWT_SECRET
   - ✅ 不要将 `.env` 文件提交到版本控制
   - ✅ 定期备份数据库和用户上传文件

2. **性能**
   - ✅ 根据服务器配置调整 JVM 参数
   - ✅ 监控数据库连接池使用情况
   - ✅ 定期检查 Redis 内存使用
   - ✅ 启用 Gzip 压缩
   - ✅ 配置 CDN 加速静态资源

3. **监控**
   - ✅ 配置应用健康检查
   - ✅ 监控 CPU、内存、磁盘使用率
   - ✅ 设置错误日志告警
   - ✅ 监控关键业务指标（订单量、支付成功率等）

4. **备份**
   - ✅ 每日自动备份数据库
   - ✅ 定期备份用户上传的文件
   - ✅ 保留至少 7 天的日志文件
   - ✅ 测试备份恢复流程

---

**祝部署顺利！** 🎉

如有问题，请查看详细日志并参考故障排查章节。
