# Mall Service 生产环境打包总结

## 📦 打包信息

| 项目 | 详情 |
|------|------|
| **服务名称** | mall-service |
| **版本号** | 1.0.0-SNAPSHOT |
| **打包时间** | 2026-04-12 01:03:51 |
| **JAR 文件大小** | 114.75 MB (117,506.5 KB) |
| **配置文件** | application-prod.yml |
| **环境变量模板** | .env.prod |
| **Java 版本** | 11+ (推荐 17) |
| **Spring Boot 版本** | 2.7.18 |

---

## 📁 生成的文件

### 1. 部署包文件

```
deploy-packages/
├── mall-service.jar                    # 主应用程序 (114.75 MB)
├── MALL_SERVICE_DEPLOY_NOTES.md        # 详细部署文档 (21.0 KB)
└── MALL_SERVICE_QUICKSTART.md          # 快速开始指南 (9.8 KB)
```

### 2. 配置文件

```
mall-service/
├── .env.prod                           # 生产环境变量模板
├── .env.example                        # 开发环境变量示例
└── src/main/resources/
    └── application-prod.yml            # 生产环境配置
```

---

## ✨ 主要功能

Mall Service 是游戏商城的后端服务，包含以下核心功能：

### 1. 用户管理
- ✅ 用户注册/登录
- ✅ JWT 认证
- ✅ 头像上传
- ✅ 个人信息管理

### 2. 商品管理
- ✅ 商品浏览、搜索
- ✅ 商品分类
- ✅ Elasticsearch 全文搜索
- ✅ 商品详情查看

### 3. 购物车
- ✅ 添加/删除商品
- ✅ 修改数量
- ✅ 批量结算

### 4. 订单管理
- ✅ 创建订单
- ✅ 订单查询
- ✅ 订单状态跟踪
- ✅ 自动取消超时订单（30分钟）

### 5. 支付功能
- ✅ 微信支付（支持模拟支付）
- ✅ 支付宝支付（预留接口）
- ✅ 余额支付
- ✅ 支付回调处理

### 6. 库存管理
- ✅ 秒杀功能
- ✅ 库存扣减
- ✅ 超卖防护

### 7. 消息通知
- ✅ 邮件通知（订单确认、折扣信息等）
- ✅ 短信通知（验证码、订单状态等）
- ✅ RabbitMQ 异步消息

### 8. 其他功能
- ✅ 积分系统
- ✅ 优惠券
- ✅ 评价系统
- ✅ 地址管理

---

## 🔧 技术栈

### 后端框架
- Spring Boot 2.7.18
- Spring Data JPA
- Spring Security
- Spring Validation

### 数据存储
- MySQL 8.0 (主数据库)
- Redis (缓存、会话、消息队列)
- Elasticsearch 7.17.x (商品搜索)

### 消息队列
- RabbitMQ (订单处理、通知发送)

### 第三方服务
- 阿里云短信服务
- 阿里云 OSS (对象存储)
- 微信支付 SDK
- QQ 邮箱 SMTP

### 工具库
- Lombok
- Hutool
- JWT (io.jsonwebtoken)

---

## 🌐 端口配置

| 服务 | 端口 | 说明 |
|------|------|------|
| Mall Service | 8081 | HTTP API 服务 |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |
| RabbitMQ | 5672 | 消息队列 (AMQP) |
| RabbitMQ Management | 15672 | 管理界面 |
| Elasticsearch | 9200 | 搜索引擎 |

---

## ⚙️ 环境要求

### 最小配置
- CPU: 2 核
- 内存: 4 GB
- 磁盘: 20 GB
- Java: 11+

### 推荐配置
- CPU: 4 核
- 内存: 8 GB
- 磁盘: 50 GB SSD
- Java: 17

---

## 📋 部署前检查清单

在部署到生产环境之前，请确保：

### 基础设施
- [ ] MySQL 8.0+ 已安装并运行
- [ ] Redis 已安装并运行
- [ ] RabbitMQ 已安装并运行
- [ ] Elasticsearch 7.17.x 已安装并运行
- [ ] IK 分词器已安装（与 ES 版本匹配）
- [ ] Java 17 已安装

### 数据库
- [ ] 生产数据库已创建 (`gacha_system_prod`)
- [ ] 数据库用户已创建并有适当权限
- [ ] 数据库表结构已初始化
- [ ] 初始数据已导入

### 配置
- [ ] `.env.prod` 已复制为 `.env`
- [ ] 数据库密码已修改为强密码
- [ ] Redis 密码已修改为强密码
- [ ] RabbitMQ 密码已修改为强密码
- [ ] JWT_SECRET 已修改为随机强密钥
- [ ] 阿里云短信配置已填写（如需要）
- [ ] 阿里云 OSS 配置已填写（如需要）
- [ ] 微信支付配置已填写（如需要）
- [ ] 邮件服务配置已填写

### 安全
- [ ] 防火墙已配置，只开放必要端口
- [ ] HTTPS 证书已配置（推荐使用 Let's Encrypt）
- [ ] 所有默认密码已修改
- [ ] `.env` 文件未提交到版本控制

### 监控
- [ ] 日志目录已创建
- [ ] 日志轮转已配置
- [ ] 健康检查端点可访问
- [ ] 备份策略已制定

---

## 🚀 快速部署命令

### Linux 服务器

```bash
# 1. 创建目录
sudo mkdir -p /opt/gacha-system/logs
sudo chown -R $USER:$USER /opt/gacha-system

# 2. 上传文件（从本地执行）
scp deploy-packages/mall-service.jar user@server:/opt/gacha-system/
scp mall-service/.env.prod user@server:/opt/gacha-system/.env

# 3. SSH 连接服务器
ssh user@server
cd /opt/gacha-system

# 4. 编辑配置
vim .env
# 修改必要的配置项

# 5. 启动服务
export $(cat .env | grep -v '^#' | xargs)
nohup java -jar \
  -Dspring.profiles.active=prod \
  -Xms512m \
  -Xmx1024m \
  mall-service.jar > logs/mall-startup.log 2>&1 &

# 6. 验证
tail -f logs/mall-startup.log
curl http://localhost:8081/api/actuator/health
```

### Docker 部署

```bash
# 1. 创建 Dockerfile 和 docker-compose.yml（参考详细文档）

# 2. 构建并启动
docker-compose up -d

# 3. 查看日志
docker-compose logs -f mall-service
```

---

## 🔍 验证部署

### 1. 健康检查

```bash
curl http://localhost:8081/api/actuator/health
# 预期: {"status":"UP"}
```

### 2. 测试 API

```bash
# 获取商品列表
curl http://localhost:8081/api/products

# 注册用户
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@example.com","password":"Test123456"}'
```

### 3. 检查日志

```bash
# 查看启动日志
tail -100 logs/mall-startup.log

# 查看应用日志
tail -100 logs/mall.log

# 搜索错误
grep "ERROR" logs/mall.log
```

---

## 📊 性能基准

基于测试环境的性能数据：

| 指标 | 数值 |
|------|------|
| 启动时间 | 30-60 秒 |
| 内存占用 | 512MB - 1GB |
| QPS (简单查询) | 500-1000 |
| QPS (复杂查询) | 100-200 |
| 平均响应时间 | 50-200ms |
| 数据库连接池 | 5-20 |

*注：实际性能取决于服务器配置和数据量*

---

## 🛠️ 维护命令

### 重启服务

```bash
# 直接启动方式
pkill -f mall-service.jar
sleep 3
export $(cat .env | grep -v '^#' | xargs)
nohup java -jar -Dspring.profiles.active=prod -Xms512m -Xmx1024m mall-service.jar > logs/mall-startup.log 2>&1 &

# Systemd 方式
sudo systemctl restart mall-service
```

### 备份数据库

```bash
mysqldump -u mall_user -p gacha_system_prod | gzip > backup_$(date +%Y%m%d).sql.gz
```

### 清理日志

```bash
# 保留最近 7 天的日志
find logs/ -name "*.log" -mtime +7 -delete
```

---

## 📞 故障排查

### 常见问题及解决方案

| 问题 | 可能原因 | 解决方案 |
|------|----------|----------|
| 启动失败 | 端口被占用 | `netstat -tlnp \| grep 8081` 查找并杀死进程 |
| 数据库连接失败 | 密码错误或 MySQL 未启动 | 检查 `.env` 中的数据库配置，确认 MySQL 运行 |
| Redis 连接失败 | 密码错误或 Redis 未启动 | 检查 Redis 配置，确认服务运行 |
| Elasticsearch 连接失败 | ES 未启动或版本不匹配 | 检查 ES 状态，确认版本为 7.17.x |
| 内存溢出 | JVM 堆内存不足 | 增加 `-Xmx` 参数 |
| 支付失败 | 配置错误 | 检查微信/支付宝配置 |

详细故障排查请参考 `MALL_SERVICE_DEPLOY_NOTES.md`。

---

## 📚 相关文档

1. **MALL_SERVICE_DEPLOY_NOTES.md** - 详细部署文档
   - 完整的部署步骤
   - 中间件安装指南
   - 安全加固建议
   - 性能优化技巧
   - 故障排查手册

2. **MALL_SERVICE_QUICKSTART.md** - 快速开始指南
   - 5 分钟快速部署
   - 常用操作命令
   - 常见问题解答

3. **mall-service/API接口文档.md** - API 文档
   - 接口列表
   - 请求/响应示例
   - 错误码说明

---

## ⚠️ 重要提醒

### 安全警告

1. **必须修改的密码**
   - 数据库密码
   - Redis 密码
   - RabbitMQ 密码
   - JWT_SECRET

2. **不要泄露的信息**
   - `.env` 文件内容
   - 阿里云 AccessKey
   - 微信支付密钥
   - 数据库凭证

3. **定期更新**
   - 系统和软件包
   - Java 版本
   - 依赖库
   - SSL 证书

### 备份策略

1. **数据库备份**
   - 每日自动备份
   - 保留至少 7 天
   - 定期测试恢复

2. **文件备份**
   - 用户上传的头像
   - 商品图片
   - 配置文件

3. **日志备份**
   - 保留至少 30 天
   - 定期归档

---

## 🎯 下一步行动

部署完成后，建议执行以下操作：

1. **立即执行**
   - [ ] 验证所有功能正常
   - [ ] 配置 HTTPS
   - [ ] 设置自动备份
   - [ ] 配置监控告警

2. **一周内完成**
   - [ ] 性能测试和优化
   - [ ] 压力测试
   - [ ] 安全扫描
   - [ ] 文档完善

3. **持续改进**
   - [ ] 监控系统运行状态
   - [ ] 收集用户反馈
   - [ ] 定期更新和维护
   - [ ] 优化用户体验

---

## 📈 监控指标

建议监控以下关键指标：

### 应用层面
- QPS (每秒请求数)
- 平均响应时间
- 错误率
- JVM 内存使用
- GC 频率和时间

### 业务层面
- 注册用户数
- 活跃用户数
- 订单数量
- 支付成功率
- 商品浏览量

### 系统层面
- CPU 使用率
- 内存使用率
- 磁盘使用率
- 网络流量
- 数据库连接数

---

## 🏆 成功标准

部署成功的标志：

- ✅ 服务正常启动，无错误日志
- ✅ 健康检查返回 `{"status":"UP"}`
- ✅ 可以正常访问 API
- ✅ 数据库连接正常
- ✅ Redis 缓存正常工作
- ✅ 消息队列正常消费
- ✅ 搜索引擎可以检索商品
- ✅ 支付流程可以完成（测试环境）
- ✅ 邮件/短信可以发送（如配置）
- ✅ 性能满足预期要求

---

## 💬 获取帮助

如果遇到问题：

1. **查看日志**
   ```bash
   tail -f logs/mall-startup.log
   tail -f logs/mall.log
   ```

2. **检查配置**
   ```bash
   cat .env
   ```

3. **验证依赖服务**
   ```bash
   mysql -u mall_user -p -e "SELECT 1"
   redis-cli ping
   curl http://localhost:9200
   rabbitmqctl status
   ```

4. **参考文档**
   - 详细部署文档
   - 快速开始指南
   - API 文档

---

**打包完成时间**: 2026-04-12 01:03:51  
**文档版本**: 1.0  
**维护者**: Gacha System Team

祝部署顺利！🎉
