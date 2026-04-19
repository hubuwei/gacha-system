# 中间件配置切换说明

## 📋 配置变更概述

已将本地开发环境的中间件（Redis、RabbitMQ、Elasticsearch）从本地切换到服务器 **111.228.12.167**。

## ✅ 已完成的配置修改

### 1. Redis 配置
- **服务器地址**: `111.228.12.167:6379`
- **密码**: `Xc037417!`
- **影响服务**:
  - ✅ auth-service
  - ✅ game-service
  - ✅ mall-service

### 2. RabbitMQ 配置
- **服务器地址**: `111.228.12.167:5672`
- **用户名**: `admin`
- **密码**: `Xc037417!`
- **影响服务**:
  - ✅ mall-service

### 3. Elasticsearch 配置
- **服务器地址**: `http://111.228.12.167:9200`
- **影响服务**:
  - ✅ mall-service (当前已禁用 ES 仓库)

## 📝 修改的文件清单

### 环境变量文件 (.env)
各服务的 `.env` 文件已配置指向服务器：
- `auth-service/.env` - Redis 已配置
- `game-service/.env` - Redis 已配置
- `mall-service/.env` - Redis、ES、RabbitMQ 已配置

### Spring Boot 配置文件 (application.yml)
更新了默认值，确保即使没有环境变量也能连接到服务器：
- `auth-service/src/main/resources/application.yml`
- `game-service/src/main/resources/application.yml`
- `mall-service/src/main/resources/application.yml`

### 模板文件
- `.env.example` - 更新为服务器配置作为默认示例

## 🚀 使用说明

### 本地开发启动
现在本地启动服务时，会自动连接到服务器的中间件：

```powershell
# 启动各个服务
cd auth-service
mvn spring-boot:run

cd game-service
mvn spring-boot:run

cd gacha-service
mvn spring-boot:run

cd mall-service
mvn spring-boot:run
```

### 注意事项

1. **网络连接**: 确保本地可以访问服务器 111.228.12.167 的以下端口：
   - `6379` - Redis
   - `5672` - RabbitMQ
   - `9200` - Elasticsearch

2. **防火墙设置**: 如果连接失败，检查服务器防火墙是否开放了上述端口

3. **性能考虑**: 由于中间件在远程服务器，网络延迟可能会影响性能，建议：
   - 开发时注意超时设置
   - 必要时调整连接池配置

4. **数据隔离**: 所有开发环境共享同一套中间件，注意：
   - Redis key 命名规范
   - RabbitMQ 队列管理
   - ES 索引区分

## 🔧 如何切换回本地中间件

如果需要切换回本地中间件，修改对应服务的 `.env` 文件：

```properties
# Redis
REDIS_HOST=localhost
REDIS_PASSWORD=

# RabbitMQ
RABBITMQ_HOST=localhost
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest

# Elasticsearch
ES_URIS=http://localhost:9200
```

## 📊 服务器中间件状态检查

```bash
# 检查 Redis
redis-cli -h 111.228.12.167 -p 6379 -a Xc037417! ping

# 检查 RabbitMQ
curl -u admin:Xc037417! http://111.228.12.167:15672/api/health/checks/alarm

# 检查 Elasticsearch
curl http://111.228.12.167:9200/_cluster/health
```

## ⚠️ 重要提醒

1. **不要提交敏感信息**: `.env` 文件已在 `.gitignore` 中，但请确保不要手动提交
2. **生产环境**: 生产环境使用 Docker Compose，通过服务名通信，不受此配置影响
3. **测试环境**: 如有独立测试环境，需要单独配置

---

**配置更新时间**: 2026-04-17  
**配置人员**: AI Assistant
