# 🚀 快速参考卡片

## 数据库切换（一行命令）

```powershell
.\switch-database.ps1 -Environment dev   # 开发环境
.\switch-database.ps1 -Environment prod  # 生产环境
.\switch-database.ps1 -Environment demo  # 演示环境
```

---

## Git 分支策略

```
main (演示/生产) ← develop (开发) ← feature/* (功能)
                                    ← fix/* (修复)
```

**常用命令：**
```bash
git checkout develop && git pull origin develop
git checkout -b feature/my-feature
# 开发完成后...
git add . && git commit -m "feat: 描述"
git push origin feature/my-feature
# 创建 PR 合并到 develop
```

---

## 服务端口

| 服务 | 端口 | 地址 |
|------|------|------|
| Mall Service | 8081 | http://localhost:8081/api |
| Game Service | 8082 | http://localhost:8082 |
| Gacha Service | 8083 | http://localhost:8083 |
| Auth Service | 8084 | http://localhost:8084 |
| Frontend | 5173 | http://localhost:5173 |

---

## 测试账号（开发环境）

- **管理员**: `admin / admin123`
- **用户1**: `testuser1 / user123`
- **用户2**: `testuser2 / user123`

---

## 启动服务

```powershell
# 终端 1
cd auth-service && mvn spring-boot:run

# 终端 2
cd game-service && mvn spring-boot:run

# 终端 3
cd gacha-service && mvn spring-boot:run

# 终端 4
cd mall-service && mvn spring-boot:run

# 终端 5 - 前端
cd game-mall && npm run dev
```

---

## Commit 规范

```
feat: 新功能
fix: 修复bug
docs: 文档
style: 格式
refactor: 重构
test: 测试
chore: 构建
```

**示例：**
```bash
git commit -m "feat: 添加用户签到功能"
git commit -m "fix: 修复登录验证失败"
```

---

## 环境配置

**开发环境 (.env):**
```env
DB_NAME=gacha_system_dev
JPA_DDL_AUTO=update
JPA_SHOW_SQL=true
LOG_LEVEL=DEBUG
```

**生产环境 (.env):**
```env
DB_NAME=gacha_system_prod
JPA_DDL_AUTO=validate
JPA_SHOW_SQL=false
LOG_LEVEL=WARN
```

---

## 数据库文件

- `schema-production.sql` - 生产表结构（无数据）
- `init-dev.sql` - 开发初始化（含测试数据）

---

## 重要提醒

❌ **不要提交** `.env` 文件  
✅ **只提交** `.env.example`  
✅ **使用** `switch-database.ps1` 切换环境  
✅ **从** `develop` **创建** `feature/*` **分支**

---

## 故障排查

**服务启动失败？**
1. 检查 MySQL 是否运行
2. 确认 `.env` 配置正确
3. 查看日志: `*.log` 文件

**数据库连接失败？**
```powershell
# 重新初始化
.\switch-database.ps1 -Environment dev
```

**Git 冲突？**
```bash
git fetch origin
git rebase origin/develop
# 解决冲突后继续
git rebase --continue
```

---

**详细文档**: 查看 `ENVIRONMENT_SETUP.md` 和 `GIT_WORKFLOW.md`
