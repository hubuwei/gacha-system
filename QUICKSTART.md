# 🚀 快速开始指南

## ✅ 优化完成

路径配置已优化，实现**本地开发零配置、生产环境简单配置**。

---

## 💻 本地开发（开箱即用）

### 1️⃣ 克隆项目
```powershell
git clone https://github.com/hubuwei/gacha-system.git
cd gacha-system
```

### 2️⃣ 启动后端
```powershell
cd mall-service
mvn spring-boot:run
```

### 3️⃣ 启动前端（新终端）
```powershell
cd game-mall
npm install  # 首次运行
npm run dev
```

### 4️⃣ 访问应用
- 前端：http://localhost:5173
- API测试：http://localhost:8081/api/games

**✅ 无需任何配置，自动工作！**

---

## 🖥️ 生产环境部署

### 方式A：一键部署脚本（推荐）

```bash
# 1. 上传到服务器
cd /opt
git clone https://github.com/hubuwei/gacha-system.git

# 2. 配置环境变量
cd gacha-system
cp .env.prod.example .env.prod
nano .env.prod  # 修改密码等配置

# 3. 运行部署脚本
chmod +x deploy-prod.sh
sudo ./deploy-prod.sh
```

### 方式B：手动部署

详细步骤请查看 [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)

---

## 📚 文档导航

| 文档 | 说明 |
|------|------|
| [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md) | 完整部署指南 |
| [PATH_CONFIG_OPTIMIZATION.md](PATH_CONFIG_OPTIMIZATION.md) | 路径配置详解 |
| README.md | 项目介绍 |

---

## 🔧 核心改进

### ✨ 自动路径检测

**本地开发**：
```java
// 自动检测为 E:\CFDemo\gacha-system\GamePapers
// 无需配置！
```

**生产环境**：
```yaml
# application-prod.yml
static:
  resources:
    game-papers-path: /opt/gacha-system/GamePapers
```

### 🌐 统一路径规范

- 数据库存储：`/GamePapers/xxx.jpg`
- 访问URL：`/api/GamePapers/xxx.jpg`
- Nginx直接返回静态文件（性能更优）

---

## ⚙️ 配置清单

### 本地开发
- ✅ 无配置要求
- ✅ 自动检测路径
- ✅ Vite代理API请求

### 生产环境
需要配置：
1. `.env.prod` - 数据库、Redis、RabbitMQ密码
2. `GAME_PAPERS_PATH=/opt/gacha-system/GamePapers`
3. Nginx配置文件

---

## 🆘 常见问题

### Q: 图片显示404？
```bash
# 检查目录是否存在
ls GamePapers/

# 查看日志
tail -f logs/mall.log | grep WebMvcConfig
```

### Q: 如何修改配置？
编辑 `.env.prod` 文件后重启服务：
```bash
sudo systemctl restart mall-service
```

### Q: 如何查看日志？
```bash
# 实时日志
tail -f logs/mall.log

# systemd日志
journalctl -u mall-service -f
```

---

## 📞 获取帮助

- 📖 查看详细文档：[DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)
- 🐛 提交Issue：https://github.com/hubuwei/gacha-system/issues
- 💬 联系开发者

---

**祝使用愉快！** 🎉
