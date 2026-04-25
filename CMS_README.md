# 游戏商城 CMS 运营后台

一个专业的React CMS运营后台系统，用于管理游戏商城的内容、订单、用户等。

## 📋 功能特性

### ✅ 已实现功能

1. **管理员登录**
   - JWT认证
   - 权限控制
   - 登录状态持久化

2. **数据看板**
   - 总用户数统计
   - 总订单数统计
   - 总营收统计
   - 游戏总数统计
   - 营收趋势图表

3. **游戏管理**
   - 游戏列表展示
   - 新增/编辑/删除游戏
   - 上下架管理
   - 批量上传图片（待完善后端API）

4. **订单管理**
   - 订单列表
   - 订单详情查看
   - 订单状态管理
   - 支付状态跟踪

5. **用户管理**
   - 用户列表
   - 用户详情查看
   - 用户封禁/解封
   - 用户数据统计

6. **活动公告**
   - 公告发布
   - 公告编辑/删除
   - 公告类型管理
   - 轮播图管理（待完善）

## 🛠️ 技术栈

### 前端
- **React 18** - UI框架
- **Vite** - 构建工具
- **Ant Design** - UI组件库
- **React Router** - 路由管理
- **Axios** - HTTP客户端
- **@ant-design/charts** - 图表库

### 后端
- **Spring Boot** - Java后端框架
- **MySQL** - 数据库
- **JWT** - 身份认证
- **JPA/Hibernate** - ORM框架

## 📦 快速开始

### 前置要求

- Node.js >= 16
- JDK 11+
- MySQL 8.0+
- Maven 3.6+

### 1. 数据库初始化

```bash
# 在MySQL中执行SQL脚本
mysql -u root -p123456 gacha_system_dev < database/create_cms_tables.sql
```

### 2. 启动后端服务

```bash
# 进入项目根目录
cd E:\CFDemo\gacha-system

# 编译common模块
cd common
mvn clean install

# 启动CMS服务
cd ../cms-service
mvn spring-boot:run
```

CMS服务将在 `http://localhost:8085` 启动

### 3. 启动前端服务

```bash
# 进入CMS前端目录
cd E:\CFDemo\gacha-system\cms-admin

# 安装依赖（如果还未安装）
npm install

# 启动开发服务器
npm run dev
```

前端服务将在 `http://localhost:5174` 启动

### 4. 访问系统

打开浏览器访问：`http://localhost:5174`

**默认管理员账号：**
- 用户名：`admin`
- 密码：`admin123`

## 📁 项目结构

```
cms-admin/
├── src/
│   ├── api/              # API接口定义
│   │   ├── auth.js       # 认证相关API
│   │   ├── games.js      # 游戏管理API
│   │   ├── orders.js     # 订单管理API
│   │   ├── users.js      # 用户管理API
│   │   ├── announcements.js  # 公告管理API
│   │   └── dashboard.js  # 数据看板API
│   ├── components/       # 公共组件
│   ├── layouts/          # 布局组件
│   │   ├── MainLayout.jsx    # 主布局
│   │   └── MainLayout.css
│   ├── pages/            # 页面组件
│   │   ├── Login.jsx         # 登录页
│   │   ├── Dashboard.jsx     # 数据看板
│   │   ├── Games.jsx         # 游戏管理
│   │   ├── Orders.jsx        # 订单管理
│   │   ├── Users.jsx         # 用户管理
│   │   └── Announcements.jsx # 活动公告
│   ├── utils/            # 工具函数
│   │   └── request.js    # Axios封装
│   ├── App.jsx           # 应用入口
│   └── main.jsx          # React入口
├── .env                  # 环境变量
├── package.json
└── vite.config.js

cms-service/
├── src/main/java/com/cheng/cms/
│   ├── config/           # 配置类
│   ├── controller/       # 控制器
│   ├── entity/           # 实体类
│   ├── repository/       # 数据访问层
│   ├── service/          # 业务逻辑层
│   └── util/             # 工具类
├── src/main/resources/
│   └── application.yml   # 配置文件
└── pom.xml
```

## 🔧 配置说明

### 环境变量

在 `cms-admin/.env` 文件中配置：

```env
VITE_API_BASE_URL=http://localhost:8084/api/cms
```

### 后端配置

在 `cms-service/src/main/resources/application.yml` 中配置：

```yaml
server:
  port: 8085

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/gacha_system_dev
    username: root
    password: 123456

jwt:
  secret: your-secret-key-change-in-production
  expiration: 86400000
```

## 📊 数据库表结构

### 核心表

1. **admins** - 管理员表（复用现有）
2. **system_configs** - 系统配置表
3. **announcements** - 系统公告表
4. **game_images** - 游戏图片库表
5. **daily_statistics** - 每日统计表
6. **admin_login_logs** - 管理员登录日志表

### 复用的业务表

1. **games** - 游戏表
2. **orders** - 订单表
3. **users** - 用户表
4. **banners** - 轮播图表

## 🚀 部署

### 生产环境构建

```bash
# 前端构建
cd cms-admin
npm run build

# 后端打包
cd ../cms-service
mvn clean package
```

### Docker部署（待完善）

```bash
# 构建Docker镜像
docker build -t cms-service ./cms-service

# 运行容器
docker run -d -p 8084:8084 cms-service
```

## 📝 待完善功能

1. **后端API完整实现**
   - 游戏CRUD API
   - 订单管理API
   - 用户管理API
   - 公告管理API
   - 数据统计API
   - 文件上传API

2. **前端功能增强**
   - 表单验证完善
   - 图片上传组件
   - 富文本编辑器
   - 高级搜索和筛选
   - 数据导出功能

3. **权限管理**
   - 角色权限控制
   - 菜单权限动态加载
   - 操作日志记录

4. **性能优化**
   - 路由懒加载
   - 组件代码分割
   - API请求缓存

## 🐛 常见问题

### 1. 登录失败

检查：
- 后端服务是否正常运行
- 数据库连接是否正常
- 管理员账号是否存在于数据库

### 2. API请求失败

检查：
- `.env` 文件中的API地址是否正确
- 后端CORS配置是否允许前端域名
- JWT Token是否有效

### 3. 端口冲突

修改端口：
- 前端：修改 `vite.config.js`
- 后端：修改 `application.yml` 中的 `server.port`

## 📄 许可证

MIT License

## 👥 贡献

欢迎提交Issue和Pull Request！

## 📞 联系方式

如有问题，请联系开发团队。
