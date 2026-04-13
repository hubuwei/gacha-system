# Git 分支管理策略

## 📌 分支说明

### 1. main (演示版/生产版)
- **用途**: 稳定的演示版本，可随时部署到生产环境
- **保护**: 禁止直接推送，只能通过 PR/MR 合并
- **来源**: 从 develop 分支合并经过测试的功能

### 2. develop (开发版主分支)
- **用途**: 日常开发集成分支，包含最新功能
- **保护**: 建议开启保护，需要代码审查
- **来源**: 从 feature 分支合并完成的功能

### 3. feature/* (功能分支)
- **命名规范**: `feature/功能名称` 或 `feature/模块-功能`
- **示例**: 
  - `feature/user-auth` - 用户认证功能
  - `feature/game-mall-search` - 游戏商城搜索功能
  - `feature/gacha-probability` - 抽卡概率系统
- **生命周期**: 从 develop 创建，完成后合并回 develop

### 4. fix/* (修复分支)
- **命名规范**: `fix/问题描述` 或 `bugfix/问题ID`
- **示例**:
  - `fix/login-error` - 修复登录错误
  - `bugfix/issue-123` - 修复 issue #123
- **生命周期**: 从 develop 创建，修复后合并回 develop

### 5. hotfix/* (紧急修复分支)
- **命名规范**: `hotfix/问题描述`
- **示例**: `hotfix/security-patch`
- **生命周期**: 从 main 创建，修复后同时合并到 main 和 develop

---

## 🔄 工作流程

### 开发新功能
```bash
# 1. 从 develop 创建功能分支
git checkout develop
git pull origin develop
git checkout -b feature/user-auth

# 2. 开发并提交
git add .
git commit -m "feat: 实现用户注册功能"

# 3. 推送到远程
git push origin feature/user-auth

# 4. 创建 Pull Request 合并到 develop
# 5. 等待代码审查通过后合并
```

### 修复 Bug
```bash
# 1. 从 develop 创建修复分支
git checkout develop
git pull origin develop
git checkout -b fix/login-error

# 2. 修复并提交
git add .
git commit -m "fix: 修复登录时密码验证失败的问题"

# 3. 推送并创建 PR
git push origin fix/login-error
```

### 发布新版本
```bash
# 1. 从 develop 合并到 main
git checkout main
git pull origin main
git merge develop
git push origin main

# 2. 打标签
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

### 紧急修复
```bash
# 1. 从 main 创建 hotfix 分支
git checkout main
git pull origin main
git checkout -b hotfix/security-patch

# 2. 修复并提交
git add .
git commit -m "hotfix: 修复安全漏洞"

# 3. 合并到 main 和 develop
git checkout main
git merge hotfix/security-patch
git push origin main

git checkout develop
git merge hotfix/security-patch
git push origin develop

# 4. 删除 hotfix 分支
git branch -d hotfix/security-patch
```

---

## 📝 Commit 消息规范

### 格式
```
<type>: <description>

[optional body]

[optional footer]
```

### Type 类型
- `feat`: 新功能
- `fix`: 修复 bug
- `docs`: 文档变更
- `style`: 代码格式（不影响功能）
- `refactor`: 重构
- `test`: 测试相关
- `chore`: 构建过程或辅助工具变动
- `perf`: 性能优化
- `ci`: CI 配置相关

### 示例
```bash
# 新功能
git commit -m "feat: 添加用户签到功能"

# 修复 bug
git commit -m "fix: 修复购物车结算金额计算错误"

# 文档更新
git commit -m "docs: 更新 API 接口文档"

# 重构
git commit -m "refactor: 优化数据库查询性能"

# 带详细描述
git commit -m "feat: 实现游戏搜索功能

- 支持按标题搜索
- 支持按分类筛选
- 支持按价格排序

Closes #123"
```

---

## 🔒 分支保护规则

### main 分支
- ✅ 需要至少 1 人代码审查
- ✅ 需要通过 CI 检查
- ✅ 禁止强制推送
- ✅ 禁止删除分支

### develop 分支
- ✅ 建议开启代码审查
- ✅ 禁止强制推送
- ⚠️ 允许直接推送（小团队可放宽）

---

## 📊 分支图示

```
main        ----M-------------------------M-----> (演示版/生产版)
             /                           /
develop     ----D-------D-------D-------D--------> (开发版)
            / \       / \     / \     /
feature    F1   F2   F3  F4  F5  F6  F7          (功能分支)
            
fix        ----X-----------X--------------------> (修复分支)

hotfix     H1-----------------------------------> (紧急修复)
```

**图例**:
- M = Merge from develop
- D = Develop commits
- F = Feature branch
- X = Fix branch
- H = Hotfix branch

---

## 💡 最佳实践

1. **频繁同步**: 经常从 develop 拉取最新代码
   ```bash
   git fetch origin
   git rebase origin/develop
   ```

2. **小步提交**: 每个 commit 只做一件事，保持原子性

3. **及时清理**: 合并后删除已完成的分支
   ```bash
   git branch -d feature/completed-feature
   git push origin --delete feature/completed-feature
   ```

4. **语义化版本**: 使用语义化版本号
   - v1.0.0 (主版本.次版本.修订版本)
   - 主版本: 不兼容的 API 修改
   - 次版本: 向下兼容的功能性新增
   - 修订版本: 向下兼容的问题修正

5. **环境隔离**: 
   - 开发环境: 使用 `gacha_system_dev` 数据库
   - 生产环境: 使用 `gacha_system_prod` 数据库
   - 通过 `.env` 文件切换配置

---

## 🚀 快速开始

```bash
# 克隆仓库
git clone <repository-url>
cd gacha-system

# 查看分支
git branch -a

# 切换到 develop 分支
git checkout develop

# 创建新功能分支
git checkout -b feature/my-new-feature

# 开发完成后推送
git push origin feature/my-new-feature

# 在 GitHub/GitLab 上创建 Pull Request
```
