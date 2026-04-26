# 部署指南

## 📋 目录

1. [环境要求](#1-环境要求)
2. [开发环境配置](#2-开发环境配置)
3. [测试环境部署](#3-测试环境部署)
4. [生产环境部署](#4-生产环境部署)
5. [一键部署脚本](#5-一键部署脚本)
6. [监控与运维](#6-监控与运维)

---

## 1. 环境要求

### 1.1 通用环境要求

| 组件 | 版本要求 | 说明 |
|------|---------|------|
| **JDK** | 11+ | 推荐 Temurin JDK 11 |
| **Maven** | 3.6+ | 构建工具 |
| **Node.js** | 16+ | 前端构建 |
| **MySQL** | 8.0+ | 主数据库 |
| **Redis** | 6.x | 缓存/消息队列 (可选) |
| **RabbitMQ** | 3.x | 消息中间件 (可选) |
| **Elasticsearch** | 8.x | 搜索引擎 (可选) |

### 1.2 服务器配置要求

| 环境 | CPU | 内存 | 磁盘 | 网络 |
|------|-----|------|------|------|
| **开发环境** | 2核 | 4GB | 50GB | 本地网络 |
| **测试环境** | 4核 | 8GB | 100GB | 内网 |
| **生产环境** | 8核+ | 16GB+ | 200GB SSD | 公网 |

---

## 2. 开发环境配置

### 2.1 后端服务配置

#### 2.1.1 依赖安装

```bash
# 1. 进入项目根目录
cd gacha-system

# 2. 构建 common 模块
cd common
mvn clean install -DskipTests

# 3. 构建各服务模块
cd ../mall-service
mvn clean compile -DskipTests

cd ../cms-service
mvn clean compile -DskipTests

cd ../game-service
mvn clean compile -DskipTests

cd ../gacha-service
mvn clean compile -DskipTests

cd ../auth-service
mvn clean compile -DskipTests
```

#### 2.1.2 数据库配置

```bash
# 1. 登录 MySQL
mysql -u root -p

# 2. 创建开发数据库
CREATE DATABASE gacha_system_dev DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 3. 创建测试数据库
CREATE DATABASE gacha_system_test DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 4. 授权用户
CREATE USER 'gacha_dev'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON gacha_system_dev.* TO 'gacha_dev'@'localhost';
GRANT ALL PRIVILEGES ON gacha_system_test.* TO 'gacha_dev'@'localhost';
FLUSH PRIVILEGES;
```

#### 2.1.3 配置文件修改

**修改 `mall-service/src/main/resources/application.yml`**:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/gacha_system_dev?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: gacha_dev
    password: your_password

# 本地开发可禁用以下服务
spring:
  redis:
    enabled: false
  rabbitmq:
    enabled: false
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
      - org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration
```

#### 2.1.4 启动服务

```bash
# 启动 mall-service
cd mall-service
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 启动 cms-service
cd ../cms-service
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 启动 game-service
cd ../game-service
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 启动 gacha-service
cd ../gacha-service
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 启动 auth-service
cd ../auth-service
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 2.2 前端服务配置

#### 2.2.1 游戏商城前端

```bash
# 进入前端目录
cd game-mall

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 访问地址: http://localhost:5173
```

#### 2.2.2 CMS 后台前端

```bash
# 进入前端目录
cd cms-admin

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 访问地址: http://localhost:5174
```

---

## 3. 测试环境部署

### 3.1 服务器准备

```bash
# 1. 更新系统
sudo apt update && sudo apt upgrade -y

# 2. 安装依赖
sudo apt install -y openjdk-11-jdk maven git docker docker-compose

# 3. 创建项目目录
sudo mkdir -p /opt/gacha-system
cd /opt/gacha-system

# 4. 克隆代码
git clone https://github.com/your-repo/gacha-system.git .
```

### 3.2 环境变量配置

**创建 `.env.test` 文件**:

```bash
cat > .env.test << EOF
# 数据库配置
DB_HOST=localhost
DB_PORT=3306
DB_NAME=gacha_system_test
DB_USERNAME=gacha_test
DB_PASSWORD=your_test_password

# 服务端口
MALL_SERVICE_PORT=8081
CMS_SERVICE_PORT=8085
GAME_SERVICE_PORT=8082
GACHA_SERVICE_PORT=8083
AUTH_SERVICE_PORT=8084

# JWT 配置
JWT_SECRET=your-test-jwt-secret-key

# 微信支付配置
WECHAT_PAY_MOCK=true

# 日志配置
LOG_LEVEL=info
EOF
```

### 3.3 数据库初始化

```bash
# 1. 登录 MySQL
mysql -u root -p

# 2. 创建测试数据库
CREATE DATABASE gacha_system_test DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 3. 授权用户
CREATE USER 'gacha_test'@'%' IDENTIFIED BY 'your_test_password';
GRANT ALL PRIVILEGES ON gacha_system_test.* TO 'gacha_test'@'%';
FLUSH PRIVILEGES;

# 4. 导入测试数据
mysql -u gacha_test -p gacha_system_test < database/test_data.sql
```

### 3.4 服务部署

```bash
# 1. 构建后端服务
mvn clean package -Ptest -DskipTests

# 2. 构建前端服务
cd game-mall
npm install && npm run build
cd ../cms-admin
npm install && npm run build

# 3. 启动服务
docker-compose -f docker-compose.test.yml up -d

# 4. 查看服务状态
docker-compose -f docker-compose.test.yml ps
```

### 3.5 测试验证

```bash
# 1. 检查服务健康状态
curl http://localhost:8081/api/actuator/health

# 2. 测试 API 接口
curl http://localhost:8081/api/games/all-with-tags

# 3. 检查前端访问
curl -I http://localhost:80
```

---

## 4. 生产环境部署

### 4.1 服务器安全配置

```bash
# 1. 配置防火墙
sudo ufw enable
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw allow 22/tcp

# 2. 禁用 root 远程登录
sudo sed -i 's/PermitRootLogin yes/PermitRootLogin no/g' /etc/ssh/sshd_config
sudo systemctl restart sshd

# 3. 创建专用部署用户
sudo useradd -m deploy
sudo usermod -aG docker deploy

# 4. 配置 SSH 密钥
sudo mkdir -p /home/deploy/.ssh
sudo chmod 700 /home/deploy/.ssh
```

### 4.2 环境变量配置

**创建 `.env.prod` 文件**:

```bash
cat > .env.prod << EOF
# 数据库配置
DB_HOST=mysql
DB_PORT=3306
DB_NAME=gacha_system_prod
DB_USERNAME=gacha_prod
DB_PASSWORD=your_production_password

# Redis 配置
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password

# RabbitMQ 配置
RABBITMQ_HOST=rabbitmq
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=admin
RABBITMQ_PASSWORD=your_rabbitmq_password

# 服务端口
MALL_SERVICE_PORT=8081
CMS_SERVICE_PORT=8085

# JWT 配置
JWT_SECRET=your-production-jwt-secret-key

# 微信支付配置
WECHAT_PAY_APP_ID=your-wechat-app-id
WECHAT_PAY_MCH_ID=your-wechat-mch-id
WECHAT_PAY_API_V3_KEY=your-wechat-api-v3-key

# 邮件配置
MAIL_HOST=smtp.qq.com
MAIL_PORT=587
MAIL_USERNAME=your-email@qq.com
MAIL_PASSWORD=your-email-password

# 日志配置
LOG_LEVEL=warn
EOF
```

### 4.3 Docker Compose 部署

**创建 `docker-compose.prod.yml` 文件**:

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: gacha-mysql
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: ${DB_PASSWORD}
      MYSQL_DATABASE: ${DB_NAME}
      MYSQL_USER: ${DB_USERNAME}
      MYSQL_PASSWORD: ${DB_PASSWORD}
    volumes:
      - mysql-data:/var/lib/mysql
      - ./database/init.sql:/docker-entrypoint-initdb.d/init.sql
    ports:
      - "3306:3306"

  redis:
    image: redis:6-alpine
    container_name: gacha-redis
    restart: always
    environment:
      REDIS_PASSWORD: ${REDIS_PASSWORD}
    volumes:
      - redis-data:/data
    ports:
      - "6379:6379"

  rabbitmq:
    image: rabbitmq:3-management
    container_name: gacha-rabbitmq
    restart: always
    environment:
      RABBITMQ_DEFAULT_USER: ${RABBITMQ_USERNAME}
      RABBITMQ_DEFAULT_PASS: ${RABBITMQ_PASSWORD}
    volumes:
      - rabbitmq-data:/var/lib/rabbitmq
    ports:
      - "5672:5672"
      - "15672:15672"

  mall-service:
    build: ./mall-service
    container_name: gacha-mall-service
    restart: always
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_HOST=${DB_HOST}
      - DB_PORT=${DB_PORT}
      - DB_NAME=${DB_NAME}
      - DB_USERNAME=${DB_USERNAME}
      - DB_PASSWORD=${DB_PASSWORD}
      - REDIS_HOST=${REDIS_HOST}
      - REDIS_PORT=${REDIS_PORT}
      - REDIS_PASSWORD=${REDIS_PASSWORD}
      - RABBITMQ_HOST=${RABBITMQ_HOST}
      - RABBITMQ_PORT=${RABBITMQ_PORT}
      - RABBITMQ_USERNAME=${RABBITMQ_USERNAME}
      - RABBITMQ_PASSWORD=${RABBITMQ_PASSWORD}
      - JWT_SECRET=${JWT_SECRET}
    ports:
      - "${MALL_SERVICE_PORT}:8081"
    depends_on:
      - mysql
      - redis
      - rabbitmq

  cms-service:
    build: ./cms-service
    container_name: gacha-cms-service
    restart: always
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_HOST=${DB_HOST}
      - DB_PORT=${DB_PORT}
      - DB_NAME=${DB_NAME}
      - DB_USERNAME=${DB_USERNAME}
      - DB_PASSWORD=${DB_PASSWORD}
      - JWT_SECRET=${JWT_SECRET}
    ports:
      - "${CMS_SERVICE_PORT}:8085"
    depends_on:
      - mysql

  nginx:
    image: nginx:alpine
    container_name: gacha-nginx
    restart: always
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./game-mall/dist:/usr/share/nginx/html:ro
      - ./cms-admin/dist:/usr/share/nginx/html/cms:ro
      - ./logs/nginx:/var/log/nginx
    ports:
      - "80:80"
      - "443:443"
    depends_on:
      - mall-service
      - cms-service

volumes:
  mysql-data:
  redis-data:
  rabbitmq-data:
```

### 4.4 Nginx 配置

**创建 `nginx/nginx.conf` 文件**:

```nginx
user nginx;
worker_processes auto;
error_log /var/log/nginx/error.log warn;
pid /var/run/nginx.pid;

events {
    worker_connections 1024;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;

    log_format main '$remote_addr - $remote_user [$time_local] "$request" '
                      '$status $body_bytes_sent "$http_referer" '
                      '"$http_user_agent" "$http_x_forwarded_for"';

    access_log /var/log/nginx/access.log main;

    sendfile on;
    keepalive_timeout 65;

    # 前端静态资源
    server {
        listen 80;
        server_name your-domain.com;

        # 重定向到 HTTPS
        return 301 https://$host$request_uri;
    }

    server {
        listen 443 ssl;
        server_name your-domain.com;

        # SSL 配置
        ssl_certificate /etc/nginx/ssl/fullchain.pem;
        ssl_certificate_key /etc/nginx/ssl/privkey.pem;
        ssl_protocols TLSv1.2 TLSv1.3;
        ssl_ciphers HIGH:!aNULL:!MD5;

        # 游戏商城前端
        location / {
            root /usr/share/nginx/html;
            try_files $uri $uri/ /index.html;
            expires 30d;
        }

        # CMS 后台前端
        location /cms/ {
            alias /usr/share/nginx/html/cms/;
            try_files $uri $uri/ /cms/index.html;
            expires 30d;
        }

        # Mall API 代理
        location /api/ {
            proxy_pass http://mall-service:8081/api/;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        # CMS API 代理
        location /cms-api/ {
            proxy_pass http://cms-service:8085/;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        # 静态资源缓存
        location ~* \.(js|css|png|jpg|jpeg|gif|ico)$ {
            root /usr/share/nginx/html;
            expires 30d;
            add_header Cache-Control "public, max-age=2592000";
        }
    }
}
```

### 4.5 部署执行

```bash
# 1. 构建前端
cd game-mall
npm install && npm run build
cd ../cms-admin
npm install && npm run build

# 2. 构建后端
mvn clean package -Pprod -DskipTests

# 3. 启动服务
docker-compose -f docker-compose.prod.yml up -d

# 4. 查看服务状态
docker-compose -f docker-compose.prod.yml ps

# 5. 检查日志
docker-compose -f docker-compose.prod.yml logs -f

# 6. 同步游戏数据到 ES
curl -X POST http://localhost:8081/api/sync/games
```

---

## 5. 一键部署脚本

### 5.1 开发环境一键启动脚本

**创建 `scripts/start-dev.sh`**:

```bash
#!/bin/bash

# 开发环境一键启动脚本
echo "=== Gacha System 开发环境启动 ==="

# 启动数据库
echo "1. 启动 MySQL 服务..."
sudo systemctl start mysql

# 构建 common 模块
echo "2. 构建 common 模块..."
cd common
mvn clean install -DskipTests
cd ..

# 启动各服务
echo "3. 启动后端服务..."

# 启动 mall-service
cd mall-service
echo "   - 启动 mall-service..."
mvn spring-boot:run -Dspring-boot.run.profiles=dev > ../logs/mall-service.log 2>&1 &
cd ..

# 启动 cms-service
cd cms-service
echo "   - 启动 cms-service..."
mvn spring-boot:run -Dspring-boot.run.profiles=dev > ../logs/cms-service.log 2>&1 &
cd ..

# 启动前端
echo "4. 启动前端服务..."

# 启动 game-mall
cd game-mall
echo "   - 启动 game-mall..."
npm run dev > ../logs/game-mall.log 2>&1 &
cd ..

# 启动 cms-admin
cd cms-admin
echo "   - 启动 cms-admin..."
npm run dev > ../logs/cms-admin.log 2>&1 &
cd ..

echo "=== 启动完成 ==="
echo "访问地址:"
echo "  游戏商城: http://localhost:5173"
echo "  CMS后台: http://localhost:5174"
echo "  Mall API: http://localhost:8081/api"
echo "  CMS API: http://localhost:8085"
echo "  Actuator: http://localhost:8081/api/actuator"
```

### 5.2 测试环境一键部署脚本

**创建 `scripts/deploy-test.sh`**:

```bash
#!/bin/bash

# 测试环境一键部署脚本
echo "=== Gacha System 测试环境部署 ==="

# 停止现有服务
echo "1. 停止现有服务..."
docker-compose -f docker-compose.test.yml down

# 清理旧文件
echo "2. 清理旧文件..."
rm -rf game-mall/dist cms-admin/dist

# 构建前端
echo "3. 构建前端..."
cd game-mall
npm install && npm run build
cd ../cms-admin
npm install && npm run build
cd ..

# 构建后端
echo "4. 构建后端..."
mvn clean package -Ptest -DskipTests

# 启动服务
echo "5. 启动服务..."
docker-compose -f docker-compose.test.yml up -d

# 等待服务启动
echo "6. 等待服务启动..."
sleep 30

# 验证服务
echo "7. 验证服务状态..."
docker-compose -f docker-compose.test.yml ps

# 测试 API
echo "8. 测试 API 接口..."
curl -s http://localhost:8081/api/actuator/health
curl -s http://localhost:8081/api/games/all-with-tags | head -50

echo "=== 部署完成 ==="
echo "测试环境地址: http://test.your-domain.com"
```

### 5.3 生产环境一键部署脚本

**创建 `scripts/deploy-prod.sh`**:

```bash
#!/bin/bash

# 生产环境一键部署脚本
echo "=== Gacha System 生产环境部署 ==="

# 备份配置
echo "1. 备份配置文件..."
cp .env.prod .env.prod.bak

# 拉取最新代码
echo "2. 拉取最新代码..."
git pull origin master

# 停止现有服务
echo "3. 停止现有服务..."
docker-compose -f docker-compose.prod.yml down

# 清理旧文件
echo "4. 清理旧文件..."
rm -rf game-mall/dist cms-admin/dist

# 构建前端
echo "5. 构建前端..."
cd game-mall
npm install && npm run build
cd ../cms-admin
npm install && npm run build
cd ..

# 构建后端
echo "6. 构建后端..."
mvn clean package -Pprod -DskipTests

# 启动服务
echo "7. 启动服务..."
docker-compose -f docker-compose.prod.yml up -d

# 等待服务启动
echo "8. 等待服务启动..."
sleep 60

# 验证服务
echo "9. 验证服务状态..."
docker-compose -f docker-compose.prod.yml ps

# 测试 API
echo "10. 测试 API 接口..."
curl -s https://your-domain.com/api/actuator/health
curl -s https://your-domain.com/api/games/all-with-tags | head -50

# 同步游戏数据
echo "11. 同步游戏数据..."
curl -X POST https://your-domain.com/api/sync/games

echo "=== 部署完成 ==="
echo "生产环境地址: https://your-domain.com"
```

### 5.4 脚本使用方法

```bash
# 给脚本添加执行权限
chmod +x scripts/*.sh

# 开发环境启动
./scripts/start-dev.sh

# 测试环境部署
./scripts/deploy-test.sh

# 生产环境部署
./scripts/deploy-prod.sh
```

---

## 6. 监控与运维

### 6.1 Spring Boot Actuator 监控

#### 6.1.1 监控端点

| 端点 | URL | 功能 |
|------|-----|------|
| **健康检查** | `/api/actuator/health` | 服务健康状态 |
| **信息** | `/api/actuator/info` | 服务信息 |
| **指标** | `/api/actuator/metrics` | 系统指标 |
| **HTTP 追踪** | `/api/actuator/httptrace` | HTTP 请求追踪 |
| **日志** | `/api/actuator/loggers` | 日志配置 |
| **Bean** | `/api/actuator/beans` | Spring Bean 信息 |

#### 6.1.2 健康检查示例

```bash
# 检查服务健康状态
curl http://localhost:8081/api/actuator/health

# 响应示例
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "result": 1,
        "validationQuery": "SELECT 1"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 107374182400,
        "free": 53687091200,
        "threshold": 10485760
      }
    }
  }
}
```

### 6.2 日志管理

#### 6.2.1 日志文件位置

```bash
# 后端日志
/opt/gacha-system/logs/mall-service_*.log
/opt/gacha-system/logs/cms-service_*.log

# Nginx 日志
/opt/gacha-system/logs/nginx/access.log
/opt/gacha-system/logs/nginx/error.log

# Docker 容器日志
docker logs gacha-mall-service
docker logs gacha-cms-service
```

#### 6.2.2 日志级别管理

```bash
# 动态调整日志级别
curl -X POST http://localhost:8081/api/actuator/loggers/com.cheng.mall \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "DEBUG"}'

# 查看当前日志级别
curl http://localhost:8081/api/actuator/loggers/com.cheng.mall
```

### 6.3 常见问题排查

#### 6.3.1 服务启动失败

```bash
# 查看启动日志
docker logs gacha-mall-service

# 检查数据库连接
mysql -h localhost -u gacha_prod -p gacha_system_prod

# 检查端口占用
netstat -tlnp | grep 8081
```

#### 6.3.2 数据库连接问题

```bash
# 检查 MySQL 服务状态
sudo systemctl status mysql

# 查看数据库错误日志
tail -f /var/log/mysql/error.log

# 测试数据库连接
mysql -h localhost -u gacha_prod -p
```

#### 6.3.3 性能问题排查

```bash
# 查看系统资源使用
htop

# 查看 JVM 内存使用
jstat -gc $(ps aux | grep mall-service | grep -v grep | awk '{print $2}')

# 查看数据库慢查询
mysql -u root -p -e "SHOW GLOBAL VARIABLES LIKE '%slow_query%'"
mysql -u root -p -e "SHOW GLOBAL VARIABLES LIKE '%long_query_time%'"
```

---

## 附录

### A. 端口映射表

| 服务 | 内部端口 | 外部端口 | 说明 |
|------|---------|---------|------|
| **MySQL** | 3306 | 3306 | 数据库 |
| **Redis** | 6379 | 6379 | 缓存 |
| **RabbitMQ** | 5672 | 5672 | 消息队列 |
| **RabbitMQ Management** | 15672 | 15672 | 管理界面 |
| **mall-service** | 8081 | 8081 | 商城后端 |
| **cms-service** | 8085 | 8085 | CMS 后端 |
| **game-service** | 8082 | 8082 | 游戏服务 |
| **gacha-service** | 8083 | 8083 | 抽奖服务 |
| **auth-service** | 8084 | 8084 | 认证服务 |
| **Nginx** | 80 | 80 | HTTP |
| **Nginx** | 443 | 443 | HTTPS |

### B. 环境变量参考

| 环境变量 | 说明 | 默认值 |
|---------|------|-------|
| `DB_HOST` | 数据库主机 | localhost |
| `DB_PORT` | 数据库端口 | 3306 |
| `DB_NAME` | 数据库名称 | gacha_system_prod |
| `DB_USERNAME` | 数据库用户名 | gacha_prod |
| `DB_PASSWORD` | 数据库密码 | - |
| `REDIS_HOST` | Redis 主机 | localhost |
| `REDIS_PORT` | Redis 端口 | 6379 |
| `REDIS_PASSWORD` | Redis 密码 | - |
| `RABBITMQ_HOST` | RabbitMQ 主机 | localhost |
| `RABBITMQ_PORT` | RabbitMQ 端口 | 5672 |
| `JWT_SECRET` | JWT 密钥 | - |
| `LOG_LEVEL` | 日志级别 | info |

### C. 安全最佳实践

1. **密码管理**:
   - 使用强密码并定期更换
   - 避免硬编码密码，使用环境变量
   - 数据库密码使用加密存储

2. **网络安全**:
   - 配置防火墙，只开放必要端口
   - 使用 HTTPS 加密传输
   - 限制数据库远程访问

3. **监控告警**:
   - 配置服务健康检查监控
   - 设置关键指标告警
   - 定期检查日志异常

4. **备份策略**:
   - 定期备份数据库
   - 备份配置文件和重要数据
   - 测试备份恢复流程

---

**文档版本**: v1.0.0
**最后更新**: 2026-04-26
**维护者**: 项目开发团队