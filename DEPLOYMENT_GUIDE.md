# 环境配置说明

## 📋 目录

- [本地开发环境](#本地开发环境)
- [生产环境](#生产环境)
- [常见问题](#常见问题)

---

## 🖥️ 本地开发环境

### 自动适配（开箱即用）

本地开发时**无需任何配置**，系统会自动检测路径。

#### 项目结构要求
```
gacha-system/           # 项目根目录
├── GamePapers/         # 游戏图片目录（必须在此位置）
├── mall-service/       # 商城服务
├── auth-service/       # 认证服务
├── game-service/       # 游戏服务
└── game-mall/          # 前端项目
```

#### 启动步骤

1. **启动后端服务**
   ```powershell
   # 进入mall-service目录
   cd mall-service
   
   # 使用Maven启动
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

2. **启动前端服务**
   ```powershell
   # 进入game-mall目录
   cd game-mall
   
   # 安装依赖（首次运行）
   npm install
   
   # 启动开发服务器
   npm run dev
   ```

3. **访问应用**
   - 前端地址：http://localhost:5173
   - 后端API：http://localhost:8081/api

#### 工作原理

- **静态资源路径**：自动检测为 `项目根目录/GamePapers`
- **API代理**：Vite开发服务器自动代理 `/api` 请求到后端
- **图片访问**：`/api/GamePapers/xxx.jpg` → 后端静态资源映射

---

## 🚀 生产环境

### 服务器部署

#### 1. 上传代码到服务器

```bash
# 在服务器上创建目录
mkdir -p /opt/gacha-system

# 上传项目文件（使用scp或git）
cd /opt/gacha-system
git clone https://github.com/hubuwei/gacha-system.git
```

#### 2. 配置环境变量

编辑 `.env.prod` 文件或创建环境变量：

```bash
# 方式1：编辑 .env.prod 文件
cd /opt/gacha-system/mall-service
nano .env.prod

# 添加以下内容：
GAME_PAPERS_PATH=/opt/gacha-system/GamePapers
DB_HOST=localhost
DB_PORT=3306
DB_NAME=gacha_system_prod
DB_USERNAME=root
DB_PASSWORD=你的数据库密码
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=你的Redis密码
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=admin
RABBITMQ_PASSWORD=你的RabbitMQ密码
```

#### 3. 编译打包

```bash
# 在项目根目录执行
cd /opt/gacha-system
mvn clean package -DskipTests
```

#### 4. 配置Nginx

```bash
# 复制Nginx配置文件
cp nginx-gacha-system.conf /etc/nginx/conf.d/gacha-system.conf

# 检查配置
nginx -t

# 重载Nginx
nginx -s reload
```

#### 5. 启动服务

```bash
# 使用提供的启动脚本
cd /opt/gacha-system
chmod +x start-all-services.sh
./start-all-services.sh
```

或使用systemd管理（推荐）：

```bash
# 创建service文件
sudo nano /etc/systemd/system/mall-service.service
```

内容：
```ini
[Unit]
Description=Mall Service
After=network.target mysql.service redis.service rabbitmq-server.service

[Service]
Type=simple
User=www-data
WorkingDirectory=/opt/gacha-system/mall-service
Environment="SPRING_PROFILES_ACTIVE=prod"
Environment="GAME_PAPERS_PATH=/opt/gacha-system/GamePapers"
ExecStart=/usr/bin/java -jar target/mall-service-1.0.0-SNAPSHOT.jar
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

启动服务：
```bash
sudo systemctl daemon-reload
sudo systemctl enable mall-service
sudo systemctl start mall-service
```

---

## ❓ 常见问题

### Q1: 图片显示404

**原因**：GamePapers路径配置错误

**解决方案**：
1. 检查日志中WebMvcConfig的输出
2. 确认GamePapers目录存在且包含图片
3. 生产环境检查 `GAME_PAPERS_PATH` 环境变量

```bash
# 查看日志
tail -f /opt/gacha-system/logs/mall.log | grep WebMvcConfig

# 检查目录
ls -la /opt/gacha-system/GamePapers/
```

### Q2: 本地开发图片不显示

**原因**：项目结构不符合要求

**解决方案**：
确保GamePapers在项目根目录：
```
gacha-system/          ← 必须在项目根目录
├── GamePapers/        ← 图片目录
└── mall-service/
```

### Q3: 如何修改图片路径

**本地开发**：无需修改，自动检测

**生产环境**：
```bash
# 方式1：修改 .env.prod
GAME_PAPERS_PATH=/your/custom/path

# 方式2：修改 application-prod.yml
static:
  resources:
    game-papers-path: /your/custom/path

# 方式3：启动时指定
java -jar mall-service.jar --static.resources.game-papers-path=/your/custom/path
```

### Q4: Nginx配置后仍然404

**检查清单**：
1. Nginx配置是否正确：`nginx -t`
2. Nginx是否重载：`nginx -s reload`
3. 目录权限是否正确：`chmod -R 755 /opt/gacha-system/GamePapers`
4. SELinux是否阻止访问（CentOS）：`setenforce 0` 测试

### Q5: 数据库中的图片路径

数据库中存储的路径应该是：`/GamePapers/xxx.jpg`

示例SQL：
```sql
UPDATE games SET image_url = '/GamePapers/黑神话悟空.jpg' WHERE id = 1;
```

---

## 🔧 配置项说明

### mall-service 配置

| 配置项 | 本地开发 | 生产环境 | 说明 |
|--------|---------|---------|------|
| `server.port` | 8081 | 8081 | 服务端口 |
| `server.servlet.context-path` | /api | /api | API前缀 |
| `static.resources.game-papers-path` | (空) | /opt/gacha-system/GamePapers | 图片目录路径 |
| `spring.datasource.url` | localhost:3306/gacha_system_dev | localhost:3306/gacha_system_prod | 数据库连接 |
| `spring.redis.host` | localhost | localhost | Redis地址 |

### 前端配置

| 配置项 | 值 | 说明 |
|--------|---|------|
| API_BASE_URL | '' (空字符串) | 使用相对路径 |
| MALL_API_BASE | '/api' | API基础路径 |

### Nginx配置

| 路径 | 目标 | 说明 |
|------|------|------|
| `/` | /opt/gacha-system/frontend | 前端静态文件 |
| `/GamePapers/` | /opt/gacha-system/GamePapers/ | 图片直接访问 |
| `/api/GamePapers/` | /opt/gacha-system/GamePapers/ | 图片API访问 |
| `/api/auth/` | http://localhost:8084 | 认证服务 |
| `/api/game/` | http://localhost:8082 | 游戏服务 |
| `/api/gacha/` | http://localhost:8083 | 抽卡服务 |
| `/api/` | http://localhost:8081 | 商城服务（默认） |

---

## 📝 快速检查清单

部署前检查：

- [ ] GamePapers目录已上传到服务器
- [ ] 数据库已初始化（gacha_system_prod）
- [ ] Redis、RabbitMQ、MySQL已启动
- [ ] .env.prod配置文件已创建
- [ ] Nginx配置已更新并重载
- [ ] 防火墙已开放80端口
- [ ] 服务启动日志无ERROR

---

## 🆘 获取帮助

如遇到问题：
1. 查看服务日志：`tail -f logs/mall.log`
2. 检查Nginx日志：`tail -f /var/log/nginx/error.log`
3. 查看浏览器控制台Network标签
4. 确认所有中间件服务正常运行
