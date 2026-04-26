# Spring Boot Actuator 监控与日志系统验证指南

## 📋 目录
- [功能概述](#功能概述)
- [本地测试步骤](#本地测试步骤)
- [验证清单](#验证清单)
- [常见问题排查](#常见问题排查)

---

## 功能概述

### 1. Spring Boot Actuator 监控功能
已为 **cms-service** 和 **mall-service** 集成以下监控能力：

#### 核心监控端点
- **health**: 服务健康状态检查（数据库连接、磁盘空间等）
- **info**: 应用基本信息
- **metrics**: 关键指标监控（JVM内存、CPU、HTTP请求等）
- **httptrace**: HTTP请求追踪（最近100个请求）
- **loggers**: 动态调整日志级别
- **beans**: Spring Bean列表
- **env**: 环境变量和配置属性
- **prometheus**: Prometheus监控数据导出
- **threaddump**: 线程快照（仅mall-service）
- **heapdump**: 堆内存快照（仅mall-service）

### 2. 日志系统规范化配置

#### 日志格式
```
2026-04-26 10:30:45.123 [http-nio-8085-exec-1] INFO  com.cheng.cms.controller.ArticleController - 文章列表查询成功
```

包含要素：
- ✅ 时间戳（精确到毫秒）
- ✅ 线程名称
- ✅ 日志级别（ERROR/WARN/INFO/DEBUG）
- ✅ 类名（完整路径）
- ✅ 日志消息

#### 日志分级策略
- **ERROR**: 系统错误、异常堆栈
- **WARN**: 警告信息、潜在问题
- **INFO**: 业务操作、关键流程
- **DEBUG**: 调试信息、详细参数

#### 日志文件自动切割
- **按大小切割**: 单个文件最大 50MB
- **按时间切割**: 每天生成新文件
- **保留策略**: 最多保留30天，总大小不超过1GB
- **文件分类**: 
  - `cms-service_error.log` / `mall-service_error.log`
  - `cms-service_warn.log` / `mall-service_warn.log`
  - `cms-service_info.log` / `mall-service_info.log`
  - `cms-service_debug.log` / `mall-service_debug.log`

---

## 本地测试步骤

### 第一步：启动 cms-service

#### 方法1：使用 IDEA 启动
1. 打开 IDEA，找到 `cms-service/src/main/java/com/cheng/cms/CmsApplication.java`
2. 右键点击文件 → Run 'CmsApplication'
3. 等待启动完成，控制台显示 "Started CmsApplication in X seconds"

#### 方法2：使用 Maven 命令启动
```powershell
# 进入 cms-service 目录
cd E:\CFDemo\gacha-system\cms-service

# 编译并启动
mvn spring-boot:run
```

### 第二步：验证 cms-service Actuator 端点

打开浏览器或 PowerShell，依次访问以下地址：

#### 1. 健康检查端点
```
http://localhost:8085/actuator/health
```

**预期结果**：
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 500000000000,
        "free": 300000000000,
        "threshold": 10485760
      }
    }
  }
}
```

#### 2. 应用信息端点
```
http://localhost:8085/actuator/info
```

**预期结果**：
```json
{}
```
（空对象表示正常，可以后续添加自定义信息）

#### 3. 指标监控端点
```
http://localhost:8085/actuator/metrics
```

**预期结果**：返回所有可用指标列表，包括：
- `jvm.memory.used`
- `jvm.cpu.recent`
- `http.server.requests`
- `tomcat.sessions.active`

#### 4. 查看具体指标（JVM内存使用）
```
http://localhost:8085/actuator/metrics/jvm.memory.used
```

**预期结果**：
```json
{
  "name": "jvm.memory.used",
  "measurements": [
    {
      "statistic": "VALUE",
      "value": 157286400
    }
  ],
  "availableTags": [...]
}
```

#### 5. HTTP请求追踪
```
http://localhost:8085/actuator/httptrace
```

**预期结果**：返回最近100个HTTP请求的详细信息（请求方法、URI、响应状态、耗时等）

#### 6. 日志级别动态调整
```powershell
# 查看当前日志级别
Invoke-RestMethod -Uri "http://localhost:8085/actuator/loggers/com.cheng.cms"

# 动态修改日志级别为 DEBUG
Invoke-RestMethod -Uri "http://localhost:8085/actuator/loggers/com.cheng.cms" -Method POST -Headers @{"Content-Type"="application/json"} -Body '{"configuredLevel":"DEBUG"}'
```

### 第三步：验证 cms-service 日志配置

#### 1. 检查日志文件是否生成
```powershell
# 进入 cms-service 目录
cd E:\CFDemo\gacha-system\cms-service

# 查看 logs 目录
ls logs\
```

**预期结果**：应该看到以下文件：
- `cms-service_info.log`
- `cms-service_warn.log`
- `cms-service_error.log`
- `cms-service_debug.log`（如果启用了DEBUG级别）

#### 2. 查看日志内容格式
```powershell
# 查看最新10行日志
Get-Content logs\cms-service_info.log -Tail 10
```

**预期格式**：
```
2026-04-26 10:30:45.123 [http-nio-8085-exec-1] INFO  com.cheng.cms.controller.ArticleController - 文章列表查询成功
2026-04-26 10:30:46.456 [http-nio-8085-exec-2] DEBUG com.cheng.cms.service.ArticleService - 查询参数: page=1, size=10
```

#### 3. 触发不同级别的日志
访问 cms-service 的业务接口，观察日志文件：
- 正常业务操作 → `cms-service_info.log`
- 警告情况 → `cms-service_warn.log`
- 异常情况 → `cms-service_error.log`

### 第四步：启动 mall-service（如果尚未运行）

```powershell
cd E:\CFDemo\gacha-system\mall-service
mvn spring-boot:run
```

### 第五步：验证 mall-service Actuator 端点

重复第二步的操作，将端口改为 `8081`：

```
http://localhost:8081/api/actuator/health
http://localhost:8081/api/actuator/metrics
http://localhost:8081/api/actuator/threaddump
http://localhost:8081/api/actuator/heapdump
```

**注意**：mall-service 配置了 `context-path: /api`，所以所有端点都需要加 `/api` 前缀。

#### 特殊端点测试

##### 1. 线程快照
```
http://localhost:8081/api/actuator/threaddump
```

**预期结果**：返回所有活动线程的堆栈信息（JSON格式）

##### 2. 堆内存快照（下载文件）
```
http://localhost:8081/api/actuator/heapdump
```

**预期结果**：下载一个 `.hprof` 文件，可以用 VisualVM 或 MAT 工具分析

### 第六步：验证 mall-service 日志配置

```powershell
cd E:\CFDemo\gacha-system\mall-service
ls logs\
Get-Content logs\mall-service_info.log -Tail 10
```

---

## 验证清单

### ✅ Actuator 监控功能验证

| 检查项 | cms-service (8085) | mall-service (8081) | 状态 |
|--------|-------------------|---------------------|------|
| health 端点可访问 | ☐ | ☐ | 待验证 |
| 返回状态为 UP | ☐ | ☐ | 待验证 |
| metrics 端点返回指标 | ☐ | ☐ | 待验证 |
| httptrace 记录请求 | ☐ | ☐ | 待验证 |
| loggers 可动态调整 | ☐ | ☐ | 待验证 |
| prometheus 端点可用 | ☐ | ☐ | 待验证 |
| threaddump 端点（仅mall） | N/A | ☐ | 待验证 |
| heapdump 端点（仅mall） | N/A | ☐ | 待验证 |

### ✅ 日志系统验证

| 检查项 | cms-service | mall-service | 状态 |
|--------|-------------|--------------|------|
| 日志文件自动生成 | ☐ | ☐ | 待验证 |
| 日志格式包含时间戳 | ☐ | ☐ | 待验证 |
| 日志格式包含线程名 | ☐ | ☐ | 待验证 |
| 日志格式包含类名 | ☐ | ☐ | 待验证 |
| ERROR 日志独立文件 | ☐ | ☐ | 待验证 |
| WARN 日志独立文件 | ☐ | ☐ | 待验证 |
| INFO 日志独立文件 | ☐ | ☐ | 待验证 |
| DEBUG 日志独立文件 | ☐ | ☐ | 待验证 |
| 日志文件按天切割 | ☐ | ☐ | 待验证 |
| 日志文件按大小切割 | ☐ | ☐ | 待验证 |

---

## 常见问题排查

### 问题1：Actuator 端点返回 404

**可能原因**：
- 服务未启动
- 端口错误
- 端点未暴露

**解决方法**：
```powershell
# 检查服务是否运行
netstat -ano | findstr :8085  # cms-service
netstat -ano | findstr :8081  # mall-service

# 检查 application.yml 配置
# 确保 management.endpoints.web.exposure.include 包含所需端点
```

### 问题2：health 端点显示 DOWN

**可能原因**：
- 数据库连接失败
- 磁盘空间不足

**解决方法**：
```powershell
# 查看详细错误信息
Invoke-RestMethod -Uri "http://localhost:8085/actuator/health" | ConvertTo-Json -Depth 10

# 检查数据库连接
# 确认 MySQL 服务正在运行
Get-Service -Name MySQL*

# 检查 application.yml 中的数据库配置
```

### 问题3：日志文件没有生成

**可能原因**：
- logs 目录权限问题
- logback-spring.xml 配置错误
- 没有日志输出

**解决方法**：
```powershell
# 手动创建 logs 目录
mkdir logs

# 检查 logback-spring.xml 是否存在
ls src\main\resources\logback-spring.xml

# 重启服务后观察控制台是否有日志输出
# 如果有控制台输出但没有文件，检查文件路径配置
```

### 问题4：日志级别不生效

**可能原因**：
- springProfile 配置与环境不匹配
- logger 配置被覆盖

**解决方法**：
```powershell
# 检查当前激活的 profile
Invoke-RestMethod -Uri "http://localhost:8085/actuator/env" | Select-String "spring.profiles.active"

# 动态调整日志级别测试
Invoke-RestMethod -Uri "http://localhost:8085/actuator/loggers/root" -Method POST -Headers @{"Content-Type"="application/json"} -Body '{"configuredLevel":"DEBUG"}'
```

### 问题5：mall-service 的 actuator 端点需要 /api 前缀

**说明**：这是正常的，因为 mall-service 配置了 `server.servlet.context-path: /api`

**正确访问方式**：
```
http://localhost:8081/api/actuator/health  ✅
http://localhost:8081/actuator/health      ❌
```

---

## 生产环境部署建议

### 1. 安全配置
在生产环境中，建议限制 Actuator 端点的访问：

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus  # 只暴露必要端点
  endpoint:
    health:
      show-details: never  # 不显示详细信息
  server:
    port: 8086  # 使用独立的管理端口
    address: 127.0.0.1  # 仅允许本地访问
```

### 2. 日志优化
```yaml
logging:
  level:
    root: WARN  # 生产环境降低日志级别
    com.cheng.mall: INFO
  file:
    max-size: 100MB  # 增大单个文件大小
    max-history: 90  # 延长保留时间
```

### 3. 监控集成
- **Prometheus + Grafana**: 通过 `/actuator/prometheus` 端点采集指标
- **ELK Stack**: 收集和分析日志文件
- **AlertManager**: 设置健康检查告警

---

## 简历技能描述建议

### 服务监控体系搭建
- 集成 Spring Boot Actuator 实现服务健康检查、指标监控、HTTP请求追踪
- 配置 Prometheus 端点支持分布式监控系统数据采集
- 实现线程快照和堆内存快照功能，支持线上故障快速诊断

### 健康检查机制实现
- 定制化健康检查端点，实时监控数据库连接、磁盘空间等关键组件状态
- 配置动态日志级别调整，支持运行时问题排查
- 实现详细的健康状态报告，包含各组件详细信息

### 线上运维能力强化
- 暴露关键管理端点（metrics、httptrace、loggers），提升运维效率
- 配置独立的监控端口和访问控制，保障生产环境安全
- 提供完整的监控数据接口，支持自动化运维脚本开发

### 分布式日志体系构建
- 基于 Logback 实现多级日志分离（ERROR/WARN/INFO/DEBUG）
- 配置日志文件按时间和大小自动切割，单文件最大50MB，保留30天
- 统一日志输出格式，包含时间戳、线程、类名等关键信息

### 线上问题快速定位
- 实现日志分级存储，便于快速筛选和定位问题
- 配置开发/生产环境差异化日志策略，平衡性能与可观测性
- 支持动态日志级别调整，无需重启服务即可增强日志输出

### 日志排查方法论
- 建立日志文件命名规范和时间索引机制
- 实现日志总量控制（单类型不超过1GB），避免磁盘占满
- 提供完整的日志配置文件模板，支持快速部署和复制

---

## 下一步行动

1. ✅ 本地测试验证所有功能
2. ⏳ 提交代码到 Git 版本控制系统
3. ⏳ 在服务器上部署并验证
4. ⏳ 配置 Prometheus + Grafana 监控面板（可选）
5. ⏳ 集成 ELK 日志分析系统（可选）

---

**最后更新时间**: 2026-04-26  
**维护人员**: 开发团队
