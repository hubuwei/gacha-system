# 路径配置优化说明

## 🎯 优化目标

实现**本地开发和生产环境自动适配**，代码上传后开箱即用，无需手动修改路径配置。

---

## ✨ 主要改进

### 1. 后端静态资源路径自动检测

**改进前：**
- 硬编码路径，本地和线上需要分别修改
- 容易出错，部署时经常忘记改路径

**改进后：**
```java
// WebMvcConfig.java
@Value("${static.resources.game-papers-path:}")
private String gamePapersPath;

// 如果配置了绝对路径，使用配置的（生产环境）
// 否则自动检测为项目根目录/GamePapers（本地开发）
```

### 2. 统一的路径规范

| 资源类型 | 数据库存储路径 | 访问URL | 说明 |
|---------|--------------|---------|------|
| 游戏图片 | `/GamePapers/xxx.jpg` | `/api/GamePapers/xxx.jpg` | 统一使用相对路径 |
| 用户头像 | `/uploads/avatars/xxx.jpg` | `http://localhost:8084/uploads/avatars/xxx.jpg` | auth-service提供 |

### 3. 配置文件分离

- **本地开发**：`application.yml` + 环境变量（可选）
- **生产环境**：`application-prod.yml` + `.env.prod`

---

## 📖 使用指南

### 本地开发（零配置）

#### 1. 项目结构
```
gacha-system/           # 克隆到此目录
├── GamePapers/         # 游戏图片（必须在此位置）
├── mall-service/       # 商城服务
├── auth-service/       # 认证服务
└── game-mall/          # 前端
```

#### 2. 启动服务
```powershell
# 后端
cd mall-service
mvn spring-boot:run

# 前端（新终端）
cd game-mall
npm run dev
```

#### 3. 访问
- 前端：http://localhost:5173
- 图片：http://localhost:8081/api/GamePapers/黑神话悟空.jpg

**✅ 无需任何配置，自动工作！**

---

### 生产环境（简单配置）

#### 1. 上传代码到服务器
```bash
cd /opt
git clone https://github.com/hubuwei/gacha-system.git
cd gacha-system
```

#### 2. 配置环境变量
```bash
# 复制模板
cp .env.prod.example .env.prod

# 编辑配置（主要是密码）
nano .env.prod
```

需要修改的配置：
```bash
DB_PASSWORD=你的数据库密码
REDIS_PASSWORD=你的Redis密码
RABBITMQ_PASSWORD=你的RabbitMQ密码
GAME_PAPERS_PATH=/opt/gacha-system/GamePapers  # 已配置好
```

#### 3. 编译打包
```bash
mvn clean package -DskipTests
```

#### 4. 配置Nginx
```bash
# 复制配置
cp nginx-gacha-system.conf /etc/nginx/conf.d/gacha-system.conf

# 检查并重载
nginx -t
nginx -s reload
```

#### 5. 启动服务

**方式A：使用systemd（推荐）**
```bash
# 复制service文件
sudo cp mall-service.service /etc/systemd/system/

# 重载并启动
sudo systemctl daemon-reload
sudo systemctl enable mall-service
sudo systemctl start mall-service

# 查看状态
sudo systemctl status mall-service
```

**方式B：使用部署脚本**
```bash
chmod +x deploy-prod.sh
sudo ./deploy-prod.sh
```

**方式C：手动启动**
```bash
cd mall-service
nohup java -jar \
    --spring.profiles.active=prod \
    target/mall-service-1.0.0-SNAPSHOT.jar \
    > ../logs/mall-startup.log 2>&1 &
```

---

## 🔧 配置项详解

### 静态资源配置

#### 本地开发
```yaml
# application.yml
static:
  resources:
    game-papers-path:  # 留空，自动检测
```

#### 生产环境
```yaml
# application-prod.yml
static:
  resources:
    game-papers-path: /opt/gacha-system/GamePapers  # 绝对路径
```

或通过环境变量：
```bash
export GAME_PAPERS_PATH=/opt/gacha-system/GamePapers
```

### Nginx路径映射

```nginx
# 直接访问（兼容旧代码）
location /GamePapers/ {
    alias /opt/gacha-system/GamePapers/;
}

# API访问（新标准）
location /api/GamePapers/ {
    alias /opt/gacha-system/GamePapers/;
}
```

---

## 🎨 前端调用示例

### React组件中使用

```jsx
// ✅ 正确：使用相对路径
<img src="/api/GamePapers/黑神话悟空.jpg" alt="游戏封面" />

// ❌ 错误：不要使用绝对路径或localhost
<img src="http://localhost:8081/api/GamePapers/xxx.jpg" />
<img src="http://your-server-ip/GamePapers/xxx.jpg" />
```

### 从API获取图片路径

```javascript
// 后端返回的图片路径已经是正确的格式
const game = await fetch('/api/games/1').then(r => r.json())
console.log(game.imageUrl) // "/GamePapers/黑神话悟空.jpg"

// 直接使用即可
<img src={game.imageUrl} />
```

---

## 📊 路径转换流程

### 本地开发
```
浏览器请求: /api/GamePapers/xxx.jpg
    ↓
Vite代理: http://localhost:8081/api/GamePapers/xxx.jpg
    ↓
Spring Boot: WebMvcConfig映射
    ↓
文件系统: E:\CFDemo\gacha-system\GamePapers\xxx.jpg
```

### 生产环境
```
浏览器请求: /api/GamePapers/xxx.jpg
    ↓
Nginx: 直接返回静态文件
    ↓
文件系统: /opt/gacha-system/GamePapers/xxx.jpg
```

**优势**：Nginx直接返回静态文件，性能更好！

---

## ⚠️ 注意事项

### 1. 数据库中的路径

确保数据库中存储的是相对路径：
```sql
-- ✅ 正确
UPDATE games SET image_url = '/GamePapers/xxx.jpg';

-- ❌ 错误
UPDATE games SET image_url = 'http://localhost:8081/GamePapers/xxx.jpg';
UPDATE games SET image_url = '/opt/gacha-system/GamePapers/xxx.jpg';
```

### 2. 文件权限（Linux）

```bash
# 确保Nginx可以读取
chmod -R 755 /opt/gacha-system/GamePapers
chown -R www-data:www-data /opt/gacha-system/GamePapers
```

### 3. Git忽略规则

已在`.gitignore`中配置：
```
deploy-packages/*.jar
.env
```

**不要上传**：
- ❌ 编译后的JAR文件
- ❌ .env文件（包含密码）
- ✅ 只上传源代码和配置文件模板

---

## 🐛 故障排查

### 问题1：图片404

**检查步骤**：
```bash
# 1. 检查文件是否存在
ls -la /opt/gacha-system/GamePapers/

# 2. 检查日志
tail -f logs/mall.log | grep WebMvcConfig

# 3. 测试直接访问
curl http://localhost:8081/api/GamePapers/test.jpg

# 4. 检查Nginx配置
nginx -t
```

### 问题2：本地开发图片不显示

**解决方案**：
1. 确认GamePapers在项目根目录
2. 检查Vite代理配置（vite.config.js）
3. 清除浏览器缓存

### 问题3：生产环境路径错误

**检查清单**：
- [ ] `.env.prod` 文件中 `GAME_PAPERS_PATH` 已配置
- [ ] 使用的是 `--spring.profiles.active=prod`
- [ ] Nginx配置已更新并重载
- [ ] 目录权限正确

---

## 📝 迁移指南

如果你之前使用了硬编码路径，按以下步骤迁移：

### 1. 更新数据库
```sql
-- 将所有硬编码路径改为相对路径
UPDATE games 
SET image_url = CONCAT('/GamePapers/', SUBSTRING_INDEX(image_url, '/', -1))
WHERE image_url LIKE '%GamePapers%';
```

### 2. 更新前端代码
```javascript
// 查找所有硬编码的localhost
// 替换前
const imageUrl = `http://localhost:8081${game.imageUrl}`

// 替换后
const imageUrl = game.imageUrl  // 已经是完整路径
```

### 3. 重新部署
```bash
git add .
git commit -m "优化路径配置，支持环境自动适配"
git push
```

---

## 🎉 总结

### 优势

✅ **开箱即用**：本地开发零配置  
✅ **自动适配**：根据环境自动选择路径  
✅ **易于维护**：配置集中管理  
✅ **性能优化**：生产环境Nginx直接返回静态文件  
✅ **安全可靠**：敏感信息通过环境变量管理  

### 核心原则

1. **相对路径优先**：数据库存储相对路径
2. **环境变量管理**：敏感信息不写入代码
3. **环境分离**：开发和生产配置独立
4. **自动化检测**：减少人工配置

---

## 📞 技术支持

遇到问题？
1. 查看 `DEPLOYMENT_GUIDE.md` 详细文档
2. 检查日志：`tail -f logs/mall.log`
3. 提交Issue到GitHub
