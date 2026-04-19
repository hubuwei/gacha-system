# 服务器数据库连接问题修复记录

## 问题描述
2026-04-19，服务器上的游戏列表接口返回错误：
```
Unable to acquire JDBC Connection; nested exception is org.hibernate.exception.JDBCConnectionException
```

## 根本原因
1. MySQL、Redis、RabbitMQ 中间件容器未运行
2. 应用容器（game-service、auth-service 等）与中间件容器不在同一个 Docker 网络中
3. MySQL 8.0 使用 `caching_sha2_password` 认证方式，导致 "Public Key Retrieval is not allowed" 错误

## 修复步骤

### 1. 启动中间件容器
```bash
cd /opt/gacha-system
docker compose up -d mysql redis rabbitmq
```

### 2. 将中间件连接到应用网络
```bash
# 给 MySQL 添加别名（支持 mysql 和 gacha-mysql）
docker network connect --alias mysql --alias gacha-mysql gacha-system_gacha-network gacha-mysql

# 给 Redis 添加别名
docker network connect --alias redis gacha-system_gacha-network gacha-redis

# 给 RabbitMQ 添加别名
docker network connect --alias rabbitmq gacha-system_gacha-network gacha-rabbitmq
```

### 3. 修复 MySQL 认证方式
```bash
# 创建 SQL 文件
cat > /tmp/fix-mysql-auth.sql <<EOF
ALTER USER 'root'@'%' IDENTIFIED WITH mysql_native_password BY 'Xc037417!';
FLUSH PRIVILEGES;
EOF

# 执行 SQL
docker exec -i gacha-mysql mysql -uroot -pXc037417! < /tmp/fix-mysql-auth.sql
```

### 4. 重启所有应用容器
```bash
docker restart gacha-game-service gacha-auth-service gacha-gacha-service gacha-mall-service
```

### 5. 验证服务状态
```bash
# 等待 1 分钟后检查
sleep 60
docker ps

# 检查日志确认启动成功
docker logs --tail 5 gacha-game-service | grep Started
docker logs --tail 5 gacha-auth-service | grep Started
docker logs --tail 5 gacha-gacha-service | grep Started
docker logs --tail 5 gacha-mall-service | grep Started
```

## 相关文件
- `start-middleware.sh` - 中间件启动脚本
- `fix-db-connection.sh` - systemd 服务环境变量配置脚本（未使用，因为服务器使用 Docker）
- `fix-game-db-connection.sh` - game-service 数据库连接修复脚本
- `fix-mysql-auth.sql` - MySQL 认证修复 SQL

## 预防措施
1. 确保 docker-compose.yml 中包含所有必要的中间件服务
2. 所有应用容器应使用相同的 Docker 网络
3. MySQL 8.0 建议使用 `mysql_native_password` 认证方式以提高兼容性
4. 定期检查容器健康状态：`docker ps`

## 验证结果
✅ 所有服务正常启动
✅ 数据库连接成功
✅ 前端可以正常获取游戏列表
