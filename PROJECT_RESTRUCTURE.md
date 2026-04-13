# 项目架构调整说明

## 📋 调整概览

本次调整将项目从单一环境改造为支持多环境（开发/生产/演示）的标准化项目结构。

---

## ✅ 已完成的调整

### 1. 数据库方案

#### 文件结构调整
```
database/
├── schema-production.sql    # ✨ 新增：生产版本表结构（无数据）
├── init-dev.sql             # ✨ 新增：开发环境初始化（含测试数据）
├── 新建文本文档.SQL         # 原文件（可保留作为参考）
└── ...其他文件
```

#### 数据库命名规范
- **开发环境**: `gacha_system_dev` - 包含测试数据
- **生产环境**: `gacha_system_prod` - 仅表结构，手动添加必要数据
- **演示环境**: `gacha_system_demo` - 包含示例数据

---

### 2. 环境配置方案

#### 新增 .env 配置文件
每个服务都添加了 `.env.example` 模板文件：

```
auth-service/.env.example
game-service/.env.example
gacha-service/.env.example
mall-service/.env.example
```

#### 配置项说明
- `DB_NAME`: 数据库名称（dev/prod/demo）
- `JPA_DDL_AUTO`: 自动建表策略（update/validate）
- `JPA_SHOW_SQL`: 是否显示 SQL 日志
- `LOG_LEVEL_*`: 日志级别控制

---

### 3. Git 分支策略

#### 分支结构
```
main (演示版/生产版)
├── develop (开发版主分支)
│   ├── feature/* (功能分支)
│   └── fix/* (修复分支)
```

#### 新增文档
- ✨ `GIT_WORKFLOW.md` - Git 分支管理工作流详细说明
- ✨ `.gitignore` 更新 - 忽略 `.env` 文件和日志文件

---

### 4. 自动化工具

#### 数据库切换脚本
- ✨ `switch-database.ps1` - PowerShell 一键切换数据库环境

**使用方法：**
```powershell
# 切换到开发环境
.\switch-database.ps1 -Environment dev

# 切换到生产环境
.\switch-database.ps1 -Environment prod

# 切换到演示环境
.\switch-database.ps1 -Environment demo
```

**脚本功能：**
1. 创建对应环境的数据库
2. 执行初始化 SQL 脚本
3. 自动更新所有服务的 `.env` 配置
4. 显示测试账号信息（开发/演示环境）

---

### 5. 文档完善

#### 新增文档
- ✨ `ENVIRONMENT_SETUP.md` - 完整的环境配置指南
- ✨ `GIT_WORKFLOW.md` - Git 分支管理策略
- ✨ `PROJECT_RESTRUCTURE.md` - 本文件

---

## 🚀 快速开始

### 方式一：使用自动化脚本（推荐）

```powershell
# 1. 切换到开发环境（包含测试数据）
.\switch-database.ps1 -Environment dev

# 2. 启动各个服务
cd auth-service
mvn spring-boot:run

# 新开终端
cd game-service
mvn spring-boot:run

# 新开终端
cd gacha-service
mvn spring-boot:run

# 新开终端
cd mall-service
mvn spring-boot:run

# 3. 启动前端
cd game-mall
npm install
npm run dev
```

### 方式二：手动配置

#### 1. 初始化数据库
```bash
# 开发环境
mysql -u root -p < database/init-dev.sql

# 生产环境
mysql -u root -p -e "CREATE DATABASE gacha_system_prod DEFAULT CHARACTER SET utf8mb4;"
mysql -u root -p gacha_system_prod < database/schema-production.sql
```

#### 2. 配置环境变量
```bash
# 为每个服务复制并修改 .env 文件
cd auth-service
copy .env.example .env
# 编辑 .env 文件，修改 DB_NAME 等配置
```

#### 3. 启动服务
```bash
mvn spring-boot:run
```

---

## 📊 环境对比

| 特性 | 开发环境 (dev) | 生产环境 (prod) | 演示环境 (demo) |
|------|---------------|----------------|----------------|
| 数据库名 | gacha_system_dev | gacha_system_prod | gacha_system_demo |
| 测试数据 | ✅ 包含 | ❌ 不包含 | ✅ 包含示例数据 |
| JPA DDL | update | validate | update |
| SQL 日志 | ✅ 开启 | ❌ 关闭 | ✅ 开启 |
| 日志级别 | DEBUG | WARN | INFO |
| 用途 | 日常开发 | 线上部署 | 客户演示 |

---

## 🔄 环境切换流程

### 开发 → 生产

```powershell
# 1. 切换到生产环境
.\switch-database.ps1 -Environment prod

# 2. 手动添加生产环境必需的基础数据
# - 创建管理员账号
# - 添加游戏分类和标签
# - 配置轮播图等

# 3. 重新打包
mvn clean package -DskipTests

# 4. 启动生产服务
java -jar auth-service.jar --spring.profiles.active=prod
```

### 生产 → 开发

```powershell
# 1. 切换回开发环境
.\switch-database.ps1 -Environment dev

# 2. 重启开发服务
mvn spring-boot:run
```

---

## ⚠️ 注意事项

### 1. .env 文件安全
- ❌ **不要**将 `.env` 文件提交到 Git
- ✅ 只提交 `.env.example` 作为模板
- ✅ 已添加到 `.gitignore`

### 2. 数据库选择
- 开发时使用 `gacha_system_dev`，可以自由修改表结构
- 生产时使用 `gacha_system_prod`，禁止自动修改表结构
- 演示时使用 `gacha_system_demo`，定期重置数据

### 3. 分支管理
- `main` 分支始终保持稳定，可随时部署
- `develop` 分支进行日常开发
- 功能开发在 `feature/*` 分支进行
- 完成后通过 PR 合并到 `develop`

### 4. Spring Cloud 决策
- ❌ **暂不引入** Spring Cloud
- ✅ 当前使用简单的 REST API 通信
- 💡 未来如需服务治理、负载均衡等再考虑引入

**理由：**
- 项目规模适中，5个服务通过 HTTP 调用即可
- Spring Cloud 会增加复杂度和学习成本
- 可以用 Nginx 做反向代理和负载均衡
- 后续可引入 OpenFeign 做声明式 HTTP 客户端（轻量级）

---

## 📝 下一步建议

### 短期（1-2周）
1. ✅ 团队熟悉新的分支管理流程
2. ✅ 统一各服务的 `.env` 配置
3. ✅ 建立代码审查机制

### 中期（1-2月）
1. 🔧 引入 CI/CD 自动化部署
2. 🔧 添加单元测试和集成测试
3. 🔧 完善 API 文档（Swagger/OpenAPI）

### 长期（3-6月）
1. 🚀 考虑引入 Docker 容器化部署
2. 🚀 评估是否需要引入 Spring Cloud
3. 🚀 建立监控和日志系统（Prometheus + Grafana）

---

## 🆘 常见问题

### Q1: 如何回滚到之前的版本？
```bash
# 查看提交历史
git log --oneline

# 回退到指定版本
git reset --hard <commit-hash>

# 或创建新分支保存当前状态
git checkout -b backup-before-change
```

### Q2: .env 文件丢失怎么办？
```bash
# 从示例文件复制
cp .env.example .env

# 根据环境修改配置
```

### Q3: 数据库切换后服务启动失败？
1. 检查 `.env` 文件中的 `DB_NAME` 是否正确
2. 确认数据库已成功创建并初始化
3. 检查 MySQL 服务是否正常运行
4. 查看服务日志定位具体错误

### Q4: 如何在不同环境间同步表结构变更？
```sql
-- 1. 在开发环境修改表结构
ALTER TABLE users ADD COLUMN new_field VARCHAR(100);

-- 2. 生成迁移脚本
-- 手动编写或使用工具生成

-- 3. 在生产环境执行迁移脚本
mysql -u root -p gacha_system_prod < migration_script.sql
```

---

## 📚 相关文档

- [环境配置详细指南](ENVIRONMENT_SETUP.md)
- [Git 分支管理工作流](GIT_WORKFLOW.md)
- [数据库设计说明](database/数据库设计说明.md)
- [快速启动指南](mall-service/快速启动指南.md)

---

## 👥 团队协作建议

### 开发人员
1. 从 `develop` 分支创建 `feature/*` 分支
2. 开发完成后提交 PR 到 `develop`
3. 等待代码审查通过后合并
4. 定期从 `develop` 拉取最新代码

### 测试人员
1. 在 `develop` 分支进行测试
2. 发现问题提交到 issue 系统
3. 验证修复后通知开发人员

### 运维人员
1. 从 `main` 分支部署生产环境
2. 使用 `schema-production.sql` 初始化数据库
3. 监控服务运行状态和日志

---

**最后更新**: 2026-04-09  
**版本**: v1.0.0
