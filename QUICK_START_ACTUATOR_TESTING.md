# 🚀 快速开始 - Actuator 监控和日志系统测试

## ⏱️ 预计时间：15分钟

---

## 第一步：启动服务（5分钟）

### 选项A：使用 IDEA 启动（推荐新手）

#### 启动 cms-service
1. 打开 IDEA
2. 找到文件：`cms-service/src/main/java/com/cheng/cms/CmsApplication.java`
3. 右键点击 → Run 'CmsApplication'
4. 等待控制台显示：`Started CmsApplication in X seconds`

#### 启动 mall-service
1. 找到文件：`mall-service/src/main/java/com/cheng/mall/MallServiceApplication.java`
2. 右键点击 → Run 'MallServiceApplication'
3. 等待控制台显示：`Started MallServiceApplication in X seconds`

### 选项B：使用命令行启动

```powershell
# 启动 cms-service
cd E:\CFDemo\gacha-system\cms-service
mvn spring-boot:run

# 新开一个 PowerShell 窗口，启动 mall-service
cd E:\CFDemo\gacha-system\mall-service
mvn spring-boot:run
```

---

## 第二步：运行自动化测试（2分钟）

```powershell
# 在项目根目录运行测试脚本
cd E:\CFDemo\gacha-system
.\test-actuator-and-logging.ps1
```

**预期输出**：
```
========================================
Spring Boot Actuator & Logging 测试脚本
========================================

第一步: 检查服务是否运行...

检查 cms-service (端口 8085)... ✅ 运行中
检查 mall-service (端口 8081)... ✅ 运行中

第二步: 测试 Actuator 端点...

----------------------------------------
测试服务: cms-service (http://localhost:8085)
----------------------------------------
[cms-service] 测试: 健康检查端点 ✅ 通过
[cms-service] 测试: 应用信息端点 ✅ 通过
[cms-service] 测试: 指标监控端点 ✅ 通过
...

========================================
测试总结
========================================
总测试数: 25
通过数量: 25
失败数量: 0
通过率: 100%

🎉 所有测试通过！Actuator 监控和日志系统配置成功！
```

---

## 第三步：手动验证（可选，5分钟）

### 1. 浏览器验证健康检查

打开浏览器访问：
- **cms-service**: http://localhost:8085/actuator/health
- **mall-service**: http://localhost:8081/api/actuator/health

**预期结果**：看到 JSON 格式的健康状态，status 为 "UP"

### 2. 查看日志文件

```powershell
# 查看 cms-service 日志
cd E:\CFDemo\gacha-system\cms-service
ls logs\
Get-Content logs\cms-service_info.log -Tail 10

# 查看 mall-service 日志
cd E:\CFDemo\gacha-system\mall-service
ls logs\
Get-Content logs\mall-service_info.log -Tail 10
```

**预期结果**：
- 看到多个日志文件（info.log, warn.log, error.log）
- 日志格式包含：时间戳、线程名、级别、类名、消息

---

## 第四步：提交代码（3分钟）

```powershell
cd E:\CFDemo\gacha-system

# 查看变更
git status

# 添加所有文件
git add .

# 提交代码
git commit -m "feat: 集成Spring Boot Actuator监控和规范化日志系统

- 为cms-service和mall-service添加Actuator监控
- 实现四级日志分离和自动切割
- 支持Prometheus监控数据导出
- 提供动态日志级别调整功能"

# 推送到远程仓库
git push origin main
```

---

## ✅ 验证清单

完成以下检查，确认一切正常：

- [ ] cms-service 成功启动（端口 8085）
- [ ] mall-service 成功启动（端口 8081）
- [ ] 自动化测试脚本运行通过
- [ ] 浏览器可以访问 health 端点
- [ ] logs 目录下生成了日志文件
- [ ] 日志格式包含时间戳、线程、级别、类名
- [ ] 代码已提交到 Git

---

## 🐛 遇到问题？

### 问题1：服务启动失败

**检查数据库连接**：
```powershell
# 确认 MySQL 正在运行
Get-Service -Name MySQL*

# 如果未运行，启动 MySQL
Start-Service MySQL80  # 或其他版本名称
```

### 问题2：端口被占用

**查看端口占用**：
```powershell
netstat -ano | findstr :8085
netstat -ano | findstr :8081
```

**解决方法**：关闭占用端口的进程，或修改 `application.yml` 中的端口配置

### 问题3：测试脚本报错

**检查 PowerShell 执行策略**：
```powershell
# 以管理员身份运行 PowerShell
Set-ExecutionPolicy -Scope CurrentUser -ExecutionPolicy RemoteSigned -Force
```

### 问题4：日志文件没有生成

**手动创建目录**：
```powershell
mkdir E:\CFDemo\gacha-system\cms-service\logs
mkdir E:\CFDemo\gacha-system\mall-service\logs
```

然后重启服务。

---

## 📚 详细文档

- **完整验证指南**: [ACTUATOR_AND_LOGGING_VERIFICATION.md](./ACTUATOR_AND_LOGGING_VERIFICATION.md)
- **实施总结**: [ACTUATOR_IMPLEMENTATION_SUMMARY.md](./ACTUATOR_IMPLEMENTATION_SUMMARY.md)

---

## 🎯 下一步

1. ✅ 本地测试通过
2. ⏳ 提交代码到 Git
3. ⏳ 部署到服务器
4. ⏳ 配置 Prometheus + Grafana（可选）
5. ⏳ 集成 ELK 日志分析（可选）

---

**祝你测试顺利！** 🎉
