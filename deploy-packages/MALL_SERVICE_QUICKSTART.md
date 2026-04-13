# Mall Service 生产环境 - 快速开始指南

## ✅ 打包完成清单

- [x] JAR 文件已生成: `deploy-packages/mall-service.jar` (114.75 MB)
- [x] 生产环境配置: `mall-service/.env.prod`
- [x] 详细部署文档: `deploy-packages/MALL_SERVICE_DEPLOY_NOTES.md`

---

## 🚀 5 分钟快速部署（测试环境）

### 步骤 1: 准备服务器环境

确保服务器已安装以下软件：

```bash
# Java 17
java -version

# MySQL 8.0
mysql --version

# Redis
redis-cli --version

# RabbitMQ
rabbitmqctl status

# Elasticsearch
curl http://localhost:9200
```

### 步骤 2: 上传文件

```powershell
# 从 Windows 上传到 Linux 服务器（替换为你的服务器信息）
scp E:\CFDemo\gacha-system\deploy-packages\mall-service.jar user@your-server:/opt/gacha-system/
scp E:\CFDemo\gacha-system\mall-service\.env.prod user@your-server:/opt/gacha-system/.env
```

### 步骤 3: 修改配置

SSH 连接到服务器：

```bash
ssh user@your-server
cd /opt/gacha-system

# 编辑环境变量文件
vim .env
```

**最少需要修改的配置**：

```bash
# 数据库密码（如果使用了不同的密码）
DB_PASSWORD=你的MySQL密码

# Redis 密码（如果使用了不同的密码）
REDIS_PASSWORD=你的Redis密码

# RabbitMQ 密码（如果使用了不同的密码）
RABBITMQ_PASSWORD=你的RabbitMQ密码

# JWT 密钥（强烈建议修改）
JWT_SECRET=生成一个随机字符串
```

生成随机 JWT 密钥：
```bash
openssl rand -base64 64
```

### 步骤 4: 初始化数据库

```bash
# 创建数据库
mysql -u root -p

CREATE DATABASE IF NOT EXISTS gacha_system_prod 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_unicode_ci;

exit;

# 导入数据库结构
mysql -u root -p gacha_system_prod < /path/to/database/init-production.sql
```

### 步骤 5: 启动服务

```bash
# 创建日志目录
mkdir -p logs

# 加载环境变量并启动
export $(cat .env | grep -v '^#' | xargs)

nohup java -jar \
  -Dspring.profiles.active=prod \
  -Xms512m \
  -Xmx1024m \
  mall-service.jar > logs/mall-startup.log 2>&1 &

# 查看启动日志
tail -f logs/mall-startup.log
```

### 步骤 6: 验证服务

```bash
# 等待 30-60 秒让服务完全启动

# 检查进程
ps aux | grep mall-service

# 检查端口
netstat -tlnp | grep 8081

# 健康检查
curl http://localhost:8081/api/actuator/health

# 预期输出: {"status":"UP"}
```

---

## 🔧 常用操作

### 查看日志

```bash
# 实时日志
tail -f logs/mall-startup.log
tail -f logs/mall.log

# 最近 100 行
tail -n 100 logs/mall.log

# 搜索错误
grep "ERROR" logs/mall.log
```

### 重启服务

```bash
# 停止服务
pkill -f mall-service.jar

# 等待几秒
sleep 3

# 重新启动
export $(cat .env | grep -v '^#' | xargs)
nohup java -jar -Dspring.profiles.active=prod -Xms512m -Xmx1024m mall-service.jar > logs/mall-startup.log 2>&1 &
```

### 停止服务

```bash
pkill -f mall-service.jar
```

---

## ⚙️ 生产环境推荐配置

### 使用 Systemd 管理（推荐）

创建服务文件：

```bash
sudo vim /etc/systemd/system/mall-service.service
```

内容：

```ini
[Unit]
Description=Mall Service
After=network.target mysql.service redis.service rabbitmq-server.service elasticsearch.service

[Service]
Type=simple
User=www-data
WorkingDirectory=/opt/gacha-system
EnvironmentFile=/opt/gacha-system/.env

ExecStart=/usr/bin/java -Xms512m -Xmx1024m -Dspring.profiles.active=prod -jar mall-service.jar
Restart=on-failure
RestartSec=10

StandardOutput=append:/opt/gacha-system/logs/mall-startup.log
StandardError=append:/opt/gacha-system/logs/mall-error.log

[Install]
WantedBy=multi-user.target
```

启用服务：

```bash
sudo systemctl daemon-reload
sudo systemctl enable mall-service
sudo systemctl start mall-service

# 查看状态
sudo systemctl status mall-service

# 查看日志
sudo journalctl -u mall-service -f
```

---

## 📝 必须配置的外部服务

### 1. 阿里云短信（可选）

如果不配置，短信功能将无法使用。

获取方式：
1. 登录阿里云控制台
2. 搜索「短信服务」
3. 创建 AccessKey
4. 创建签名和模板

在 `.env` 中配置：
```bash
SMS_ACCESS_KEY_ID=LTAI5t...
SMS_ACCESS_KEY_SECRET=xxxxx
SMS_SIGN_NAME=游戏商城
SMS_TEMPLATE_CODE=SMS_123456789
```

### 2. 阿里云 OSS（可选）

用于存储用户上传的头像、商品图片等。

在 `.env` 中配置：
```bash
OSS_ACCESS_KEY_ID=LTAI5t...
OSS_ACCESS_KEY_SECRET=xxxxx
OSS_BUCKET_NAME=game-mall
OSS_ENDPOINT=oss-cn-hangzhou.aliyuncs.com
OSS_BASE_URL=https://game-mall.oss-cn-hangzhou.aliyuncs.com
```

### 3. 微信支付（可选）

如果需要支持微信支付，必须配置。

获取方式：
1. 注册微信支付商户号
2. 登录商户平台
3. 申请 API 证书
4. 设置 APIv3 密钥

在 `.env` 中配置：
```bash
WECHAT_PAY_APP_ID=wx1234567890
WECHAT_PAY_MCH_ID=1234567890
WECHAT_PAY_API_V3_KEY=32位随机字符串
WECHAT_PAY_SERIAL_NO=证书序列号
WECHAT_PAY_NOTIFY_URL=https://你的域名.com/api/payment/wechat/notify
```

### 4. 邮件服务（推荐配置）

用于发送订单通知、找回密码等邮件。

QQ 邮箱配置示例：
```bash
MAIL_HOST=smtp.qq.com
MAIL_PORT=587
MAIL_USERNAME=1712133303@qq.com
MAIL_PASSWORD=授权码  # 在 QQ 邮箱设置中生成
```

获取 QQ 邮箱授权码：
1. 登录 QQ 邮箱网页版
2. 设置 -> 账户
3. 开启 POP3/SMTP 服务
4. 生成授权码

---

## 🐛 常见问题

### Q1: 启动失败，提示端口被占用

```bash
# 查找占用端口的进程
netstat -tlnp | grep 8081

# 杀死进程
kill -9 <PID>

# 或者修改端口
vim .env
# 修改 APP_PORT=8082
```

### Q2: 数据库连接失败

```bash
# 测试数据库连接
mysql -u mall_user -p -h localhost gacha_system_prod

# 检查 MySQL 是否运行
systemctl status mysql

# 检查防火墙
sudo ufw status
```

### Q3: Redis 连接失败

```bash
# 测试 Redis 连接
redis-cli -h localhost -a Xc037417! ping

# 应该返回 PONG

# 检查 Redis 是否运行
systemctl status redis
```

### Q4: Elasticsearch 连接失败

```bash
# 测试 ES 连接
curl http://localhost:9200

# 检查 ES 是否运行
systemctl status elasticsearch

# 检查 IK 分词器
curl -X POST "localhost:9200/_analyze" -H 'Content-Type: application/json' -d'
{
  "analyzer": "ik_max_word",
  "text": "测试"
}'
```

### Q5: 内存不足

```bash
# 减少 JVM 堆内存
vim .env
# 修改启动命令中的 -Xms 和 -Xmx

# 或者增加服务器内存
```

---

## 📊 监控检查清单

部署后请检查以下项目：

- [ ] 服务正常运行: `curl http://localhost:8081/api/actuator/health`
- [ ] 数据库连接正常: 查看日志无数据库错误
- [ ] Redis 连接正常: 查看日志无 Redis 错误
- [ ] RabbitMQ 连接正常: 查看日志无 MQ 错误
- [ ] Elasticsearch 连接正常: 查看日志无 ES 错误
- [ ] 可以访问 API: `curl http://localhost:8081/api/products`
- [ ] 日志文件正常生成: `ls -lh logs/`
- [ ] 磁盘空间充足: `df -h`
- [ ] 内存使用正常: `free -h`
- [ ] CPU 使用正常: `top`

---

## 🔗 相关链接

- **详细部署文档**: `MALL_SERVICE_DEPLOY_NOTES.md`
- **项目 README**: `E:\CFDemo\gacha-system\README.md`
- **API 文档**: `E:\CFDemo\gacha-system\mall-service\API接口文档.md`

---

## 💡 下一步

1. ✅ 完成基本部署
2. ⏭️ 配置 HTTPS（使用 Nginx + Let's Encrypt）
3. ⏭️ 配置域名解析
4. ⏭️ 设置自动备份
5. ⏭️ 配置监控告警
6. ⏭️ 性能优化和压力测试

---

**部署成功！** 🎉

如有问题，请查看详细部署文档或查看日志排查。
