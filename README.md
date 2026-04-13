# Gacha System - 微服务抽奖系统

## 项目简介

一个基于 Spring Boot 的微服务抽奖系统，包含用户认证、充值、积分管理和抽奖功能。

## 技术栈

- **后端框架**: Spring Boot 3.2.0
- **数据库**: MySQL
- **ORM**: Spring Data JPA
- **认证**: JWT
- **构建工具**: Maven
- **Java 版本**: 11

## 项目结构

```
gacha-system/
├── common/              # 公共模块（实体类、DTO、工具类）
├── auth-service/        # 认证服务（端口：8081）
├── game-service/        # 游戏服务（端口：8082）
└── gacha-service/       # 抽奖服务（端口：8083）
```

## 核心功能

### 1. 用户认证 (auth-service:8081)
- 用户注册
- 用户登录（JWT Token）
- 大区服务器列表
- 选择大区

### 2. 游戏服务 (game-service:8082)
- 充值（模拟支付）
- 积分查询
- 余额管理

### 3. 抽奖服务 (gacha-service:8083)
- 单次抽奖
- 十连抽
- 保底机制（90 抽必出 SSR）
- 概率系统：SSR(2%)、SR(18%)、R(80%)

## 快速开始

### 1. 环境要求
- JDK 11+
- MySQL 5.7+
- Maven 3.6+

### 2. 数据库初始化

```bash
# 登录 MySQL
mysql -u root -p

# 执行初始化脚本
source database/init.sql
```

### 3. 修改配置

根据实际情况修改各服务的 `application.yml` 中的数据库连接信息：
- 数据库地址
- 用户名
- 密码

### 4. 编译项目

```bash
mvn clean install -DskipTests
```

### 5. 启动服务

分别启动三个服务：

**认证服务:**
```bash
cd auth-service
mvn spring-boot:run
# 或直接运行 target 包
java -jar target/auth-service-1.0.0-SNAPSHOT.jar
```

**游戏服务:**
```bash
cd game-service
mvn spring-boot:run
```

**抽奖服务:**
```bash
cd gacha-service
mvn spring-boot:run
```

## API 接口文档

### 认证服务 (http://localhost:8081)

#### 1. 用户注册
```http
POST /api/auth/register
Content-Type: application/x-www-form-urlencoded

username=testuser&password=123456
```

#### 2. 用户登录
```http
POST /api/auth/login
Content-Type: application/json

{
    "username": "testuser",
    "password": "123456"
}

响应:
{
    "code": 200,
    "message": "success",
    "data": {
        "userId": 1,
        "username": "testuser",
        "token": "eyJhbGciOiJIUzI1NiJ9...",
        "points": 1000,
        "balance": 0.0,
        "currentServer": null
    }
}
```

#### 3. 获取大区列表
```http
GET /api/servers

响应:
[
    {
        "id": 1,
        "serverCode": "east-china-1",
        "serverName": "华东一区",
        "status": 1
    },
    ...
]
```

#### 4. 选择大区
```http
POST /api/users/{userId}/select-server?serverCode=east-china-1
```

### 游戏服务 (http://localhost:8082)

#### 1. 充值
```http
POST /api/recharge
Content-Type: application/json

{
    "userId": 1,
    "amount": 100.0
}

响应:
{
    "code": 200,
    "message": "success",
    "data": {
        "id": 1,
        "userId": 1,
        "amount": 100.0,
        "points": 1000,
        "createTime": "2024-03-27T12:00:00",
        "remark": "充值"
    }
}
```

#### 2. 查询积分
```http
GET /api/points/{userId}

响应:
{
    "code": 200,
    "message": "success",
    "data": {
        "id": 1,
        "username": "testuser",
        "points": 2000,
        "balance": 100.0,
        ...
    }
}
```

### 抽奖服务 (http://localhost:8083)

#### 1. 单次抽奖
```http
POST /api/gacha/draw
Content-Type: application/json

{
    "userId": 1,
    "poolCode": "standard",
    "drawCount": 1
}

响应:
{
    "code": 200,
    "message": "success",
    "data": {
        "results": [
            {
                "prizeName": "传说英雄",
                "prizeLevel": 1,
                "consumePoints": 100,
                "isGuaranteed": false
            }
        ],
        "totalConsumePoints": 100,
        "newPointsBalance": 1900
    }
}
```

#### 2. 十连抽
```http
POST /api/gacha/draw
Content-Type: application/json

{
    "userId": 1,
    "poolCode": "standard",
    "drawCount": 10
}
```

## 游戏规则

### 抽奖概率
- **SSR (传说)**: 2%
- **SR (稀有)**: 18%
- **R (普通)**: 80%

### 保底机制
- 每 90 次抽奖必出 SSR
- 保底计数器独立计算

### 积分系统
- 充值比例：1 元 = 10 积分
- 单抽消耗：100 积分
- 十连抽消耗：1000 积分
- 注册赠送：1000 积分

## 测试账号

数据库初始化后会创建两个测试账号：

| 用户名 | 密码 | 初始积分 | 初始余额 |
|--------|------|----------|----------|
| test001 | 123456 | 1000 | 0.00 |
| test002 | 123456 | 5000 | 100.00 |

## 注意事项

1. **密码加密**: 当前使用 SHA-256 简单加密，生产环境建议使用 BCrypt
2. **JWT 密钥**: 请修改 `JwtUtil` 中的密钥为更安全的随机字符串
3. **数据库配置**: 确保 MySQL 服务已启动且配置正确
4. **端口占用**: 确保 8081、8082、8083 端口未被占用

## 常见问题

### Q: 如何重置用户数据？
A: 重新执行 `database/init.sql` 脚本

### Q: 抽奖记录在哪里查看？
A: 查询数据库表 `gacha_records`

### Q: 如何修改抽奖概率？
A: 修改 `GachaService` 中的 `SSR_RATE`、`SR_RATE`、`R_RATE` 常量

## 扩展建议

1. 添加奖池配置表，支持动态配置概率
2. 实现积分商城功能
3. 添加用户等级系统
4. 实现排行榜功能
5. 添加每日签到功能
6. 实现好友系统

我来分析 SQL 脚本和 React 前端代码,对比已实现和未实现的功能。




**已查看文件** *App.jsx*

**已检索仓库** *shopping cart wishlist order review notification backend API endpoints* 20 个结果


根据 SQL 脚本和 React 前端代码的对比分析,我为你整理了详细的功能清单:

## 📋 React 游戏商城功能实现清单

### ✅ 已实现功能

| 模块 | 功能 | 状态 | 说明 |
|------|------|------|------|
| **用户认证** | 用户登录 | ✅ | 调用 auth-service (8084) |
| | 用户注册 | ✅ | 支持用户名、密码、手机、邮箱 |
| | 用户登出 | ✅ | 清除本地存储 |
| | Token 管理 | ✅ | localStorage 存储 |
| **游戏展示** | 游戏列表 | ⚠️ | 使用硬编码数据,未连接后端 |
| | 分类筛选 | ✅ | 前端筛选 |
| | 搜索功能 | ✅ | 前端搜索 |
| | 精选推荐 | ✅ | 显示 featured 游戏 |
| | 折扣显示 | ✅ | 显示原价、折扣率、现价 |
| **购物车** | 添加商品 | ⚠️ | 仅前端状态,未持久化 |
| | 删除商品 | ⚠️ | 仅前端状态 |
| | 查看购物车 | ✅ | 模态框展示 |
| | 结算购买 | ❌ | 仅模拟扣款,无订单创建 |
| **用户信息** | 余额显示 | ✅ | 从 auth-service 获取 |
| | 充值入口 | ⚠️ | 链接到 game-service,未实现 |

### ❌ 未实现功能(需要完善)

#### 1. **游戏数据模块** 🔴 高优先级
- [ ] 从后端 API 获取真实游戏数据
- [ ] 游戏详情页
- [ ] 游戏配置要求展示
- [ ] 游戏截图/预告片
- [ ] 游戏评论系统
- [ ] 评分和评价数量

#### 2. **购物车模块** 🔴 高优先级
- [ ] 购物车数据持久化(后端 API)
- [ ] 购物车数量修改
- [ ] 批量选择/取消
- [ ] 库存检查
- [ ] 价格实时更新

#### 3. **订单模块** 🔴 高优先级
- [ ] 创建订单 API 调用
- [ ] 订单列表页面
- [ ] 订单详情页面
- [ ] 订单状态跟踪
- [ ] 支付功能(余额/支付宝/微信)
- [ ] 退款申请

#### 4. **愿望单模块** 🟡 中优先级
- [ ] 添加到愿望单
- [ ] 愿望单列表
- [ ] 从愿望单移除
- [ ] 折扣通知设置
- [ ] 发售通知设置

#### 5. **用户中心** 🟡 中优先级
- [ ] 个人资料编辑
- [ ] 头像上传
- [ ] 密码修改
- [ ] 手机号/邮箱验证
- [ ] 消费记录查询
- [ ] 余额流水

#### 6. **评论系统** 🟢 低优先级
- [ ] 发表评论
- [ ] 查看评论列表
- [ ] 评论点赞/点踩
- [ ] 回复评论
- [ ] 评论图片上传
- [ ] 官方回复标识

#### 7. **通知系统** 🟢 低优先级
- [ ] 系统通知列表
- [ ] 折扣提醒
- [ ] 订单状态通知
- [ ] 消息已读标记
- [ ] WebSocket 实时推送

#### 8. **其他功能** 🟢 低优先级
- [ ] 轮播图展示
- [ ] 游戏标签筛选
- [ ] 排序功能(价格/评分/销量)
- [ ] 分页加载
- [ ] 响应式优化

---

## 🎯 建议实施计划

我建议按以下优先级逐步完善:

### 第一阶段:核心购物流程(必须)
1. ✅ 游戏数据从后端获取
2. ✅ 购物车后端持久化
3. ✅ 订单创建和支付
4. ✅ 订单列表和详情

### 第二阶段:用户体验提升(重要)
5. 愿望单功能
6. 用户资料管理
7. 游戏详情页

### 第三阶段:社交互动(可选)
8. 评论系统
9. 通知系统

---

**你希望我先从哪个阶段开始实现?** 

我建议先从**第一阶段**开始,完成核心的购物流程。我可以帮你:
1. 启动 mall-service (8081端口)
2. 实现游戏数据 API
3. 实现购物车 API
4. 实现订单 API
5. 更新 React 前端对接这些 API

需要我开始吗?
你说得对!让我重新分析一下 React 前端实际需要哪些接口。




**已检索代码** *fetch\(|AUTH_API_BASE|GAME_API_BASE* 7 个结果


你说得对!**8084 (auth-service) 是必须的**,因为 React 前端目前调用的所有认证接口都在 auth-service 上:

## 📡 React 前端当前需要的接口

### 必须启动的服务:

1. **auth-service (8084)** ✅ 必须
   - `POST /api/auth/login` - 登录
   - `POST /api/auth/register/body` - 注册
   - `GET /api/auth/info` - 获取用户信息(含余额)
   - `POST /api/auth/logout` - 登出

2. **game-service (8082)** ⚠️ 仅充值页面用
   - `/api/recharge` - 充值页面(新窗口打开)

### 不需要启动的服务:

- **mall-service (8081)** ❌ 暂时不需要
  - React 前端目前没有调用 mall-service 的任何接口
  - 购物车、订单等功能都是前端模拟的

---

## 🎯 完善 React 项目的方案

既然你只需要完善 React 项目,有两个选择:

### 方案 A: 继续使用 auth-service (推荐) ✅
**保持现状**,auth-service 继续运行在 8084,因为它已经提供了:
- ✅ 登录/注册
- ✅ 用户信息查询
- ✅ Token 管理

**需要做的:**
1. 在 auth-service 中添加游戏商城相关接口
2. 或者创建一个新的 mall-service 专门处理商城业务

### 方案 B: 迁移到 mall-service
把认证功能也迁移到 mall-service (8081),然后:
1. 修改 React 前端的 API 地址为 8081
2. 停止 auth-service
3. 在 mall-service 中实现所有功能

---

## 💡 我的建议

**保持 auth-service (8084) 运行**,因为:
1. ✅ 认证功能已经正常工作
2. ✅ 职责分离清晰(auth 负责认证,mall 负责商城)
3. ✅ 不需要修改现有代码

**接下来只需要:**
1. 启动 game-service (8082) - 用于充值
2. 在 auth-service 或新建服务中添加商城接口:
   - 游戏列表
   - 购物车
   - 订单
   - 等等...

**你希望我:**
- A. 在 auth-service 中添加商城接口(简单快速)
- B. 启动 mall-service (8081) 并实现商城接口(架构更清晰)
- C. 其他想法?

请告诉我你的选择,我会帮你实现!