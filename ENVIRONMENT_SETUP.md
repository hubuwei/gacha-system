# 环境配置指南

## 📋 目录结构

```
gacha-system/
├── database/
│   ├── schema-production.sql    # 生产版本表结构（无数据）
│   ├── init-dev.sql             # 开发环境初始化（含测试数据）
│   └── ...
├── auth-service/
│   ├── .env.example             # 环境变量示例
│   ├── .env                     # 实际环境配置（不提交到 Git）
│   └── src/main/resources/
│       ├── application.yml      # Spring Boot 配置
│       └── application-dev.yml  # 开发环境覆盖配置
├── game-service/
│   ├── .env.example
│   ├── .env
│   └── ...
├── gacha-service/
│   ├── .env.example
│   ├── .env
│   └── ...
└── mall-service/
    ├── .env.example
    ├── .env
    └── ...
```

---

## 🔧 数据库配置

### 1. 创建数据库

#### 开发环境
```sql
-- 创建开发数据库
CREATE DATABASE IF NOT EXISTS gacha_system_dev 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_unicode_ci;

-- 使用开发数据库
USE gacha_system_dev;

-- 执行初始化脚本（包含表结构和测试数据）
SOURCE database/init-dev.sql;
```

#### 生产环境
```sql
-- 创建生产数据库
CREATE DATABASE IF NOT EXISTS gacha_system_prod 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_unicode_ci;

-- 使用生产数据库
USE gacha_system_prod;

-- 执行表结构脚本（仅表结构，无数据）
SOURCE database/schema-production.sql;

-- 手动创建管理员账号和必要的基础数据
```

### 2. 快速初始化

```bash
# Windows PowerShell - 开发环境
mysql -u root -p < database/init-dev.sql

# Linux/Mac - 开发环境
mysql -u root -p gacha_system_dev < database/init-dev.sql

# 生产环境
mysql -u root -p gacha_system_prod < database/schema-production.sql
```

---

## ⚙️ 服务配置

### 方法一：使用 .env 文件（推荐）

#### 1. 复制示例文件
```bash
# 为每个服务复制 .env.example 为 .env
cd auth-service
copy .env.example .env

cd ../game-service
copy .env.example .env

cd ../gacha-service
copy .env.example .env

cd ../mall-service
copy .env.example .env
```

#### 2. 修改配置
编辑每个服务的 `.env` 文件，根据环境修改以下关键配置：

**开发环境 (.env)**
```env
DB_NAME=gacha_system_dev
JPA_DDL_AUTO=update
JPA_SHOW_SQL=true
LOG_LEVEL_AUTH=DEBUG
```

**生产环境 (.env)**
```env
DB_NAME=gacha_system_prod
JPA_DDL_AUTO=validate
JPA_SHOW_SQL=false
LOG_LEVEL_AUTH=WARN
```

#### 3. 在 application.yml 中引用环境变量

Spring Boot 支持通过 `${ENV_VAR:default}` 语法读取环境变量：

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:gacha_system_dev}?useSSL=${DB_USE_SSL:false}&serverTimezone=${DB_TIMEZONE:UTC}&characterEncoding=${DB_ENCODING:utf-8}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:123456}
```

---

### 方法二：使用 Profile 配置文件

为每个服务创建不同的 profile 配置文件：

```
src/main/resources/
├── application.yml          # 通用配置
├── application-dev.yml      # 开发环境
├── application-prod.yml     # 生产环境
└── application-demo.yml     # 演示环境
```

#### application-dev.yml 示例
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/gacha_system_dev?useSSL=false&serverTimezone=UTC&characterEncoding=utf-8
    username: root
    password: 123456
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

logging:
  level:
    com.cheng.auth: DEBUG
    org.hibernate.SQL: DEBUG
```

#### application-prod.yml 示例
```yaml
spring:
  datasource:
    url: jdbc:mysql://prod-db-server:3306/gacha_system_prod?useSSL=true&serverTimezone=Asia/Shanghai&characterEncoding=utf-8
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

logging:
  level:
    com.cheng.auth: WARN
    org.hibernate.SQL: WARN
```

#### 激活 Profile

**方式 1: 命令行参数**
```bash
java -jar auth-service.jar --spring.profiles.active=prod
```

**方式 2: 环境变量**
```bash
export SPRING_PROFILES_ACTIVE=prod
java -jar auth-service.jar
```

**方式 3: application.yml 中指定**
```yaml
spring:
  profiles:
    active: dev  # 默认激活 dev
```

---

## 🚀 启动服务

### 开发环境

```bash
# 1. 确保数据库已初始化
mysql -u root -p < database/init-dev.sql

# 2. 为每个服务配置 .env 文件（开发环境）
# 修改 DB_NAME=gacha_system_dev

# 3. 启动各个服务
cd auth-service
mvn spring-boot:run

cd ../game-service
mvn spring-boot:run

cd ../gacha-service
mvn spring-boot:run

cd ../mall-service
mvn spring-boot:run
```

### 生产环境

```bash
# 1. 创建生产数据库（仅表结构）
mysql -u root -p gacha_system_prod < database/schema-production.sql

# 2. 手动创建管理员账号和基础数据

# 3. 为每个服务配置 .env 文件（生产环境）
# 修改 DB_NAME=gacha_system_prod
# 修改 JPA_DDL_AUTO=validate
# 修改日志级别为 WARN

# 4. 打包应用
mvn clean package -DskipTests

# 5. 启动服务
java -jar auth-service/target/auth-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod
java -jar game-service/target/game-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod
java -jar gacha-service/target/gacha-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod
java -jar mall-service/target/mall-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod
```

---

## 🔄 环境切换流程

### 从开发切换到生产

1. **数据库切换**
   ```sql
   -- 备份开发数据（如需要）
   mysqldump -u root -p gacha_system_dev > backup_dev.sql
   
   -- 使用生产数据库
   USE gacha_system_prod;
   ```

2. **修改 .env 文件**
   ```env
   # 开发环境
   DB_NAME=gacha_system_dev
   JPA_DDL_AUTO=update
   JPA_SHOW_SQL=true
   
   # 改为生产环境
   DB_NAME=gacha_system_prod
   JPA_DDL_AUTO=validate
   JPA_SHOW_SQL=false
   ```

3. **重新打包部署**
   ```bash
   mvn clean package -DskipTests
   java -jar service.jar --spring.profiles.active=prod
   ```

---

## 📊 各服务端口规划

| 服务名称 | 开发端口 | 生产端口 | 说明 |
|---------|---------|---------|------|
| auth-service | 8084 | 8084 | 认证服务 |
| game-service | 8082 | 8082 | 游戏服务 |
| gacha-service | 8083 | 8083 | 抽卡服务 |
| mall-service | 8081 | 8081 | 商城服务 |
| MySQL | 3306 | 3306 | 数据库 |
| Redis | 6379 | 6379 | 缓存 |
| Elasticsearch | 9200 | 9200 | 搜索引擎 |
| RabbitMQ | 5672 | 5672 | 消息队列 |

---

## 🔐 安全建议

### 生产环境
1. ✅ 修改所有默认密码
2. ✅ 启用 SSL/TLS 连接
3. ✅ 使用环境变量或密钥管理服务存储敏感信息
4. ✅ 限制数据库访问 IP
5. ✅ 关闭 SQL 日志输出
6. ✅ 设置 `JPA_DDL_AUTO=validate` 防止自动修改表结构
7. ✅ 启用防火墙规则

### 开发环境
1. ⚠️ 可以使用简化配置
2. ⚠️ 可以开启详细日志
3. ⚠️ 允许自动建表（ddl-auto=update）
4. ❌ 不要将真实的生产数据用于开发

---

## 🐛 常见问题

### Q1: 如何快速重置开发数据库？
```bash
# 删除并重新创建
mysql -u root -p -e "DROP DATABASE gacha_system_dev; CREATE DATABASE gacha_system_dev DEFAULT CHARACTER SET utf8mb4;"
mysql -u root -p gacha_system_dev < database/init-dev.sql
```

### Q2: 如何在不同环境间切换？
```bash
# 方法 1: 修改 .env 文件中的 DB_NAME
# 方法 2: 使用不同的 profile
java -jar app.jar --spring.profiles.active=dev  # 开发
java -jar app.jar --spring.profiles.active=prod # 生产
```

### Q3: .env 文件是否应该提交到 Git？
❌ **不应该**。`.env` 文件包含敏感信息，应添加到 `.gitignore`。
✅ 只提交 `.env.example` 作为配置模板。

### Q4: 如何在 Docker 中使用环境变量？
```dockerfile
# Dockerfile
FROM openjdk:11-jre-slim
COPY target/app.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

```bash
# docker-compose.yml
version: '3'
services:
  auth-service:
    build: ./auth-service
    environment:
      - DB_HOST=mysql
      - DB_NAME=gacha_system_prod
      - DB_USERNAME=${DB_USERNAME}
      - DB_PASSWORD=${DB_PASSWORD}
    ports:
      - "8084:8084"
```

---

## 📚 相关文档

- [Git 分支管理工作流](GIT_WORKFLOW.md)
- [数据库设计说明](database/数据库设计说明.md)
- [快速启动指南](mall-service/快速启动指南.md)
