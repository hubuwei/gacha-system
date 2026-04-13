# 🎮 游戏商城系统 - 架构升级说明

> **重要**: 本项目已完成架构升级，支持多环境部署和标准化 Git 工作流。

---

## 🆕 最新变化 (2026-04-09)

### ✨ 核心改进

1. **多环境支持**
   - ✅ 开发环境 (`gacha_system_dev`) - 含测试数据
   - ✅ 生产环境 (`gacha_system_prod`) - 仅表结构
   - ✅ 演示环境 (`gacha_system_demo`) - 含示例数据

2. **配置管理**
   - ✅ 每个服务独立的 `.env` 配置文件
   - ✅ 一键切换数据库环境脚本
   - ✅ 敏感信息不提交到 Git

3. **Git 工作流**
   - ✅ `main` 分支 = 演示版/生产版
   - ✅ `develop` 分支 = 开发版
   - ✅ `feature/*` 分支 = 功能开发

4. **自动化工具**
   - ✅ PowerShell 数据库切换脚本
   - ✅ 自动更新服务配置
   - ✅ 智能环境检测

---

## 🚀 快速开始（3步启动）

### 步骤 1: 初始化数据库

```powershell
# 切换到开发环境（包含测试数据）
.\switch-database.ps1 -Environment dev
```

**测试账号：**
- 管理员: `admin / admin123`
- 用户1: `testuser1 / user123`
- 用户2: `testuser2 / user123`

### 步骤 2: 启动后端服务

```powershell
# 终端 1 - 认证服务
cd auth-service
mvn spring-boot:run

# 终端 2 - 游戏服务
cd game-service
mvn spring-boot:run

# 终端 3 - 抽卡服务
cd gacha-service
mvn spring-boot:run

# 终端 4 - 商城服务
cd mall-service
mvn spring-boot:run
```

### 步骤 3: 启动前端应用

```powershell
cd game-mall
npm install
npm run dev
```

访问: http://localhost:5173

---

## 📁 项目结构

```
gacha-system/
├── database/                    # 数据库脚本
│   ├── schema-production.sql   # 生产版本表结构
│   ├── init-dev.sql            # 开发环境初始化
│   └── ...
├── auth-service/               # 认证服务 (8084)
│   ├── .env.example            # 配置模板
│   ├── .env                    # 实际配置（不提交）
│   └── src/
├── game-service/               # 游戏服务 (8082)
│   ├── .env.example
│   ├── .env
│   └── src/
├── gacha-service/              # 抽卡服务 (8083)
│   ├── .env.example
│   ├── .env
│   └── src/
├── mall-service/               # 商城服务 (8081)
│   ├── .env.example
│   ├── .env
│   └── src/
├── game-mall/                  # React 前端
│   └── src/
├── switch-database.ps1         # 数据库切换脚本
├── GIT_WORKFLOW.md             # Git 工作流说明
├── ENVIRONMENT_SETUP.md        # 环境配置指南
└── PROJECT_RESTRUCTURE.md      # 架构调整说明
```

---

## 🔧 常用命令

### 数据库管理

```powershell
# 切换环境
.\switch-database.ps1 -Environment dev    # 开发
.\switch-database.ps1 -Environment prod   # 生产
.\switch-database.ps1 -Environment demo   # 演示

# 手动执行 SQL
mysql -u root -p < database/init-dev.sql
```

### Git 操作

```bash
# 创建功能分支
git checkout develop
git pull origin develop
git checkout -b feature/my-feature

# 提交代码
git add .
git commit -m "feat: 添加新功能"
git push origin feature/my-feature

# 合并到主分支（通过 PR）
# 在 GitHub/GitLab 上创建 Pull Request
```

### 构建部署

```bash
# 开发模式
mvn spring-boot:run

# 生产打包
mvn clean package -DskipTests

# 运行 JAR
java -jar target/service.jar --spring.profiles.active=prod
```

---

## 🌍 环境对比

| 特性 | 开发 (dev) | 生产 (prod) | 演示 (demo) |
|------|-----------|------------|------------|
| 数据库 | gacha_system_dev | gacha_system_prod | gacha_system_demo |
| 测试数据 | ✅ | ❌ | ✅ |
| 自动建表 | ✅ update | ❌ validate | ✅ update |
| SQL 日志 | ✅ | ❌ | ✅ |
| 日志级别 | DEBUG | WARN | INFO |

---

## 📚 详细文档

- 📘 [环境配置指南](ENVIRONMENT_SETUP.md) - 详细的配置说明
- 📗 [Git 工作流](GIT_WORKFLOW.md) - 分支管理策略
- 📙 [架构调整说明](PROJECT_RESTRUCTURE.md) - 本次升级详情
- 📕 [数据库设计](database/数据库设计说明.md) - 表结构说明

---

## ⚠️ 重要提示

### 1. 不要提交 .env 文件
`.env` 文件包含敏感信息，已添加到 `.gitignore`。  
只提交 `.env.example` 作为配置模板。

### 2. 分支保护
- `main` 分支禁止直接推送
- 所有功能通过 PR 合并
- 生产部署从 `main` 分支拉取

### 3. 环境隔离
- 开发使用 `gacha_system_dev`
- 生产使用 `gacha_system_prod`
- 不要混用数据库

---

## 🛠️ 技术栈

### 后端
- Spring Boot 2.7.18
- Java 11
- MySQL 8.0
- Redis (可选)
- Elasticsearch (搜索)
- RabbitMQ (消息队列)

### 前端
- React 18
- Vite
- Ant Design

### 工具
- Maven
- Git
- PowerShell

---

## ❓ 常见问题

### Q: 如何重置开发数据库？
```powershell
.\switch-database.ps1 -Environment dev
```

### Q: 服务启动失败怎么办？
1. 检查 MySQL 是否运行
2. 确认 `.env` 配置正确
3. 查看日志文件定位错误

### Q: 如何切换到生产环境？
```powershell
.\switch-database.ps1 -Environment prod
# 修改 .env 中的敏感配置
mvn clean package -DskipTests
java -jar service.jar --spring.profiles.active=prod
```

---

## 🤝 团队协作

### 开发人员
1. 从 `develop` 创建 `feature/*` 分支
2. 开发完成后提交 PR
3. 等待代码审查后合并

### 测试人员
1. 在 `develop` 分支测试
2. 提交 issue 报告问题

### 运维人员
1. 从 `main` 分支部署
2. 监控服务状态

---

## 📞 技术支持

如有问题，请：
1. 查阅相关文档
2. 查看 issue 列表
3. 联系项目负责人

---

**最后更新**: 2026-04-09  
**当前版本**: v1.0.0  
**分支策略**: main (演示) + develop (开发)
