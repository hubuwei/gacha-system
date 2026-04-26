# 服务器部署 Actuator 监控和日志系统指南

## 📋 当前状态

- ✅ 代码已提交到 GitHub 和码云
- ❌ 服务器上的服务还是旧版本（未包含 Actuator 和新日志配置）
- ⏳ 需要重新构建并部署服务

---

## 🚀 快速部署步骤

### 方法一：使用自动化部署脚本（推荐）

在**本地 PowerShell** 中执行以下命令：

```powershell
# 1. 上传最新代码到服务器
scp -r E:\CFDemo\gacha-system\* root@111.228.12.167:/opt/gacha-system/

# 2. SSH 连接到服务器
ssh root@111.228.12.167

# 3. 在服务器上执行（连接后）
cd /opt/gacha-system

# 4. 停止旧服务
docker-compose down

# 5. 重新构建镜像（这会使用最新的代码）
docker compose build --no-cache mall-service cms-service

# 6. 启动服务
docker compose up -d

# 7. 等待服务启动（约1-2分钟）
sleep 30

# 8. 检查服务状态
docker compose ps

# 9. 测试 Actuator 端点
curl http://localhost:8081/api/actuator/health
curl http://localhost:8085/actuator/health

# 10. 查看日志
docker compose logs -f mall-service
```

---

### 方法二：手动逐步部署（更详细）

#### 步骤1：在本地推送代码（已完成 ✅）

你已经完成了这一步，代码已经在 GitHub 和码云上。

#### 步骤2：上传代码到服务器

在**本地 PowerShell** 中执行：

```powershell
# 方式A：使用 scp 上传整个项目
scp -r E:\CFDemo\gacha-system\* root@111.228.12.167:/opt/gacha-system/

# 方式B：如果 scp 不可用，先压缩再上传
# 在本地压缩
cd E:\CFDemo
Compress-Archive -Path gacha-system\* -DestinationPath gacha-system.zip

# 上传压缩包
scp gacha-system.zip root@111.228.12.167:/opt/

# 在服务器上解压
ssh root@111.228.12.167 "cd /opt && unzip -o gacha-system.zip -d gacha-system/"
```

#### 步骤3：SSH 连接到服务器

```powershell
ssh root@111.228.12.167
```

输入密码后，你会看到服务器命令行提示符。

#### 步骤4：进入项目目录

```bash
cd /opt/gacha-system
```

#### 步骤5：拉取最新代码（如果服务器上有 Git）

```bash
# 检查是否有 Git
git status

# 如果有 Git，直接拉取
git pull origin master

# 如果没有 Git，跳过此步骤（因为你已经用 scp 上传了）
```

#### 步骤6：查看当前运行的容器

```bash
# 查看所有运行中的容器
docker ps

# 你应该看到这些服务：
# - gacha-mall-service (端口 8081)
# - gacha-cms-service (端口 8085)
# - gacha-auth-service (端口 8084)
# - gacha-game-service (端口 8082)
# - gacha-nginx (端口 80)
# - gacha-mysql (端口 3306)
# - gacha-redis (端口 6379)
# - gacha-rabbitmq (端口 5672, 15672)
# - gacha-elasticsearch (端口 9200, 9300)
```

#### 步骤7：停止旧服务

```bash
# 停止所有服务
docker compose down

# 或者只停止需要更新的服务
docker compose stop mall-service cms-service
```

#### 步骤8：重新构建镜像

```bash
# 重新构建 mall-service 和 cms-service（使用最新代码）
docker compose build --no-cache mall-service cms-service

# 这个过程可能需要 5-10 分钟，请耐心等待
# 会看到类似这样的输出：
# Step 1/10 : FROM openjdk:11-jre-slim
# Step 2/10 : WORKDIR /app
# ...
# Successfully built xxxxx
# Successfully tagged gacha-system-mall-service:v10
```

**说明**：
- `--no-cache` 参数表示不使用缓存，确保使用最新代码
- 只会重新构建 mall-service 和 cms-service，其他服务不受影响
- 构建过程中会编译 Java 代码、打包 JAR

#### 步骤9：启动服务

```bash
# 启动所有服务
docker compose up -d

# -d 参数表示后台运行（detached mode）
```

#### 步骤10：等待服务启动

```bash
# 等待 30-60 秒让服务完全启动
sleep 30

# 查看服务状态
docker compose ps
```

**预期输出**：
```
NAME                     IMAGE                          COMMAND                  STATUS
gacha-mall-service       gacha-system-mall-service:v10  "java -jar app.jar"      Up (healthy)
gacha-cms-service        gacha-system-cms-service:v1    "java -jar app.jar"      Up (healthy)
...
```

#### 步骤11：验证服务是否正常

```bash
# 查看 mall-service 日志
docker compose logs mall-service

# 应该看到类似这样的启动成功信息：
# Started MallServiceApplication in X seconds
```

#### 步骤12：测试 Actuator 端点

```bash
# 测试 mall-service 健康检查
curl http://localhost:8081/api/actuator/health

# 预期结果（JSON格式）：
# {"status":"UP","components":{"db":{"status":"UP"},...}}

# 测试 cms-service 健康检查
curl http://localhost:8085/actuator/health

# 测试指标端点
curl http://localhost:8081/api/actuator/metrics

# 测试 HTTP 追踪
curl http://localhost:8081/api/actuator/httptrace
```

#### 步骤13：检查日志文件

```bash
# 进入 mall-service 容器
docker compose exec mall-service bash

# 在容器内查看日志目录
cd /app/logs
ls -lh

# 应该看到：
# mall-service_info.log
# mall-service_warn.log
# mall-service_error.log
# mall-service_debug.log

# 查看最新的 info 日志
tail -f mall-service_info.log

# 退出容器
exit
```

#### 步骤14：从外部访问 Actuator（可选）

如果你想从本地浏览器访问服务器的 Actuator 端点，可以使用 SSH 隧道：

在**本地 PowerShell** 中新开一个窗口，执行：

```powershell
# 建立 SSH 隧道
ssh -L 9081:localhost:8081 root@111.228.12.167
```

然后在本地浏览器访问：
```
http://localhost:9081/api/actuator/health
http://localhost:9081/api/actuator/metrics
http://localhost:9081/api/actuator/httptrace
```

---

## 🔍 验证清单

部署完成后，逐一检查以下项目：

### ✅ Actuator 监控功能

```bash
# 1. 健康检查
curl http://localhost:8081/api/actuator/health | python3 -m json.tool

# 2. 应用信息
curl http://localhost:8081/api/actuator/info

# 3. 指标监控
curl http://localhost:8081/api/actuator/metrics

# 4. JVM 内存
curl http://localhost:8081/api/actuator/metrics/jvm.memory.used

# 5. HTTP 追踪
curl http://localhost:8081/api/actuator/httptrace

# 6. 日志器
curl http://localhost:8081/api/actuator/loggers

# 7. Prometheus
curl http://localhost:8081/api/actuator/prometheus

# 8. 线程快照
curl http://localhost:8081/api/actuator/threaddump | head -50

# 9. CMS 服务健康检查
curl http://localhost:8085/actuator/health
```

### ✅ 日志系统

```bash
# 1. 进入容器
docker compose exec mall-service bash

# 2. 检查日志文件是否存在
ls -lh /app/logs/

# 3. 查看日志格式
head -5 /app/logs/mall-service_info.log

# 预期格式：
# 2026-04-26 10:30:45.123 [http-nio-8081-exec-1] INFO  com.cheng.mall.xxx - 消息内容

# 4. 检查各级别日志文件
ls -lh /app/logs/mall-service_*.log

# 应该看到：
# mall-service_info.log
# mall-service_warn.log
# mall-service_error.log
# mall-service_debug.log

# 5. 退出容器
exit
```

---

## 🐛 常见问题排查

### 问题1：构建失败

**可能原因**：Maven 依赖下载失败、代码编译错误

**解决方法**：
```bash
# 查看详细错误信息
docker compose build mall-service

# 清理 Docker 缓存
docker system prune -a

# 重新构建
docker compose build --no-cache mall-service
```

### 问题2：服务启动失败

**可能原因**：端口被占用、数据库连接失败、配置错误

**解决方法**：
```bash
# 查看详细日志
docker compose logs mall-service

# 检查端口占用
netstat -tlnp | grep 8081

# 检查数据库连接
docker compose logs mysql | tail -20
```

### 问题3：Actuator 端点返回 404

**可能原因**：
- 镜像没有正确重建
- 配置文件没有生效

**解决方法**：
```bash
# 确认使用的是新镜像
docker images | grep mall-service

# 应该看到最新的镜像（时间最近）

# 检查容器内的配置文件
docker compose exec mall-service cat /app/application.yml | grep -A 5 management

# 应该能看到 actuator 配置

# 如果配置不对，重新构建
docker compose down
docker compose build --no-cache mall-service
docker compose up -d
```

### 问题4：日志文件没有生成

**可能原因**：
- logback-spring.xml 没有打包到 JAR 中
- 日志目录权限问题

**解决方法**：
```bash
# 检查 JAR 包中是否包含 logback-spring.xml
docker compose exec mall-service jar tf /app/app.jar | grep logback

# 应该看到：
# BOOT-INF/classes/logback-spring.xml

# 如果没有，检查 pom.xml 资源配置
# 然后重新构建

# 检查日志目录权限
docker compose exec mall-service ls -la /app/
docker compose exec mall-service mkdir -p /app/logs
docker compose exec mall-service chmod 777 /app/logs

# 重启服务
docker compose restart mall-service
```

### 问题5：磁盘空间不足

**可能原因**：旧的 Docker 镜像和容器占用空间

**解决方法**：
```bash
# 查看磁盘使用情况
df -h

# 清理未使用的 Docker 资源
docker system prune -a

# 删除旧的镜像
docker images | grep mall-service
docker rmi <旧镜像ID>

# 清理日志文件
docker compose exec mall-service rm -f /app/logs/*.log
```

---

## 📊 部署后监控

### 实时监控服务状态

```bash
# 实时查看所有服务日志
docker compose logs -f

# 只看 mall-service
docker compose logs -f mall-service

# 只看错误日志
docker compose logs mall-service | grep ERROR
```

### 定期检查健康状态

```bash
# 创建检查脚本
cat > check-health.sh << 'EOF'
#!/bin/bash
echo "=== 服务健康检查 ==="
echo ""

echo "1. Mall Service:"
curl -s http://localhost:8081/api/actuator/health | python3 -m json.tool

echo ""
echo "2. CMS Service:"
curl -s http://localhost:8085/actuator/health | python3 -m json.tool

echo ""
echo "3. 容器状态:"
docker compose ps
EOF

chmod +x check-health.sh

# 运行检查
./check-health.sh
```

### 设置定时任务（可选）

```bash
# 编辑 crontab
crontab -e

# 添加每小时检查一次
0 * * * * /opt/gacha-system/check-health.sh >> /var/log/health-check.log 2>&1
```

---

## 💡 小贴士

### tip1：备份数据

在更新之前，建议备份重要数据：

```bash
# 备份数据库
docker compose exec mysql mysqldump -u root -p'$DB_PASSWORD' gacha_system_prod > backup_$(date +%Y%m%d).sql

# 备份 uploads 目录
tar czf uploads_backup_$(date +%Y%m%d).tar.gz uploads/
```

### tip2：回滚方案

如果新版本有问题，可以快速回滚：

```bash
# 停止新服务
docker compose down

# 使用旧镜像启动（如果有保留）
docker compose up -d

# 或者重新拉取旧代码
git checkout <旧版本commit>
docker compose build mall-service
docker compose up -d
```

### tip3：零停机更新（高级）

如果需要保证服务不中断，可以使用蓝绿部署：

```bash
# 1. 启动新版本服务（不同端口）
docker compose -f docker-compose-new.yml up -d

# 2. 测试新版本
curl http://localhost:8082/api/actuator/health

# 3. 切换 Nginx 配置指向新版本
# 修改 nginx.conf，将后端指向新端口

# 4. 重载 Nginx
docker compose exec nginx nginx -s reload

# 5. 确认正常后，停止旧版本
docker compose down
```

---

## 🎯 完整部署命令清单

如果你想一键完成所有操作，可以在服务器上执行：

```bash
#!/bin/bash
# 一键部署脚本

set -e

echo "=========================================="
echo "开始部署 Actuator 监控和日志系统"
echo "=========================================="

# 1. 进入项目目录
cd /opt/gacha-system

# 2. 停止旧服务
echo "停止旧服务..."
docker compose down

# 3. 重新构建
echo "重新构建镜像..."
docker compose build --no-cache mall-service cms-service

# 4. 启动服务
echo "启动服务..."
docker compose up -d

# 5. 等待启动
echo "等待服务启动..."
sleep 30

# 6. 检查状态
echo "检查服务状态..."
docker compose ps

# 7. 测试 Actuator
echo "测试 Actuator 端点..."
curl -s http://localhost:8081/api/actuator/health | python3 -m json.tool || echo "Actuator 测试失败"

# 8. 查看日志
echo "查看最新日志..."
docker compose logs --tail=20 mall-service

echo "=========================================="
echo "部署完成！"
echo "=========================================="
```

保存为 `deploy-actuator.sh`，然后执行：

```bash
chmod +x deploy-actuator.sh
./deploy-actuator.sh
```

---

## 📝 总结

1. ✅ 代码已提交到 Git
2. ⏳ 需要上传代码到服务器
3. ⏳ 需要重新构建 Docker 镜像
4. ⏳ 需要重启服务
5. ⏳ 验证 Actuator 和日志功能

**预计时间**：15-30 分钟（取决于网络速度和服务器性能）

**注意事项**：
- 部署过程中服务会短暂中断（1-2分钟）
- 建议在低峰期进行部署
- 部署前备份重要数据
- 部署后全面测试功能

---

祝你部署顺利！如有问题随时告诉我！🚀
