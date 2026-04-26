# Gacha System - 游戏商城与抽奖系统技术文档

## 📑 目录

1. [项目概述与功能特性](#1-项目概述与功能特性)
2. [技术架构设计](#2-技术架构设计)
3. [环境要求与安装部署流程](#3-环境要求与安装部署流程)
4. [详细使用指南](#4-详细使用指南)
5. [完整API接口文档](#5-完整api接口文档)
6. [常见问题解答](#6-常见问题解答)
7. [贡献规范与代码提交指南](#7-贡献规范与代码提交指南)

---

## 1. 项目概述与功能特性

### 1.1 项目简介

Gacha System 是一个基于微服务架构的游戏商城与抽奖系统，提供了完整的游戏购买、社交互动、签到奖励、积分兑换和抽奖功能。系统采用前后端分离架构，后端基于 Spring Boot 微服务构建，前端使用 React 构建现代化的用户界面。

### 1.2 项目组成

```
gacha-system/
├── game-mall/          # 游戏商城前端 (React + Vite) - 端口 5173
├── cms-admin/          # CMS管理后台前端 (React + Ant Design) - 端口 5174
├── mall-service/       # 商城服务后端 (Spring Boot) - 端口 8081
├── cms-service/        # CMS服务后端 (Spring Boot) - 端口 8085
├── game-service/       # 游戏服务后端 (签到、积分、兑换) - 端口 8082
├── gacha-service/      # 抽卡服务后端 - 端口 8083
├── auth-service/       # 认证服务后端 - 端口 8084
└── common/            # 公共模块 (实体类、DTO、工具类)
```

### 1.3 核心功能特性

#### 1.3.1 游戏商城模块 (game-mall)

| 功能模块 | 功能描述 |
|---------|---------|
| **游戏浏览** | 游戏列表展示、分页、排序、分类筛选、标签过滤 |
| **游戏搜索** | Elasticsearch 全文搜索，支持拼音、同义词、多字段加权 |
| **购物车** | 添加/移除商品、数量修改、价格实时计算 |
| **订单管理** | 订单创建、微信支付、订单超时自动取消 (15分钟) |
| **愿望单** | 添加/移除游戏、降价提醒通知 |
| **用户钱包** | 余额查询、充值功能 (模拟支付)、交易流水记录 |
| **社交功能** | 好友系统 (添加/删除/黑名单)、好友邀请、好友活动动态 |
| **通知系统** | WebSocket 实时推送、通知中心、邮件通知 |
| **评论系统** | 游戏评论、评分、点赞/点踩 |

#### 1.3.2 CMS 后台管理模块 (cms-admin)

| 功能模块 | 功能描述 |
|---------|---------|
| **仪表盘** | 统计数据展示、本周营收图表、热门游戏排行 |
| **用户管理** | 用户列表查询、详情查看、禁用/启用 |
| **游戏管理** | 游戏列表/搜索、价格编辑、上下架管理 |
| **订单管理** | 订单列表查询、状态筛选、详情查看 |
| **公告管理** | 公告列表、创建/编辑、置顶/发布 |
| **轮播图管理** | 轮播图列表、上传/编辑、链接配置 |
| **评论管理** | 评论列表、审核/删除 |
| **通知管理** | 广播通知、折扣通知邮件 |

#### 1.3.3 抽奖模块 (gacha-service)

| 功能 | 描述 |
|-----|------|
| **单次抽奖** | 随机抽取游戏物品 |
| **十连抽** | 批量抽取， garant 机制保底 |
| **保底系统** | 90抽必出 SSR |
| **概率公示** | SSR (2%)、SR (18%)、R (80%) |

#### 1.3.4 游戏服务模块 (game-service)

| 功能 | 描述 |
|-----|------|
| **签到系统** | 每日签到、连续签到奖励 (7天/30天 bonus) |
| **积分管理** | 积分查询、积分记录 |
| **充值系统** | 模拟充值、余额管理 |
| **物品兑换** | 积分兑换游戏物品、库存管理 |

### 1.4 技术亮点

- **微服务架构**: 按业务域拆分服务，独立部署与扩展
- **多级缓存**: Redis + Elasticsearch 提升检索和访问效率
- **异步消息**: RabbitMQ 解耦服务间通信，支持削峰填谷
- **实时通知**: WebSocket + STOMP 协议实现即时推送
- **自动化部署**: GitHub Actions 实现 CI/CD 持续部署

---

## 2. 技术架构设计

### 2.1 系统架构图

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              用户浏览器                                   │
└─────────────────────────────────────────────────────────────────────────┘
                                      │
                    ┌─────────────────┴─────────────────┐
                    ▼                                   ▼
           ┌──────────────────┐               ┌──────────────────┐
           │   Game Mall      │               │   CMS Admin      │
           │   前端 :5173     │               │   前端 :5174     │
           └────────┬─────────┘               └────────┬─────────┘
                    │                                   │
                    │         HTTPS (Nginx 反向代理)     │
                    │         端口 80/443 路由分发        │
                    └─────────────────┬─────────────────┘
                                      │
┌─────────────────────────────────────┼─────────────────────────────────┐
│                              Nginx 负载均衡层                            │
└─────────────────────────────────────┼─────────────────────────────────┘
                                      │
           ┌──────────────────────────┼──────────────────────────┐
           │                          │                          │
           ▼                          ▼                          ▼
┌──────────────────┐      ┌──────────────────┐      ┌──────────────────┐
│  mall-service   │      │  auth-service    │      │  cms-service     │
│      :8081       │      │      :8084       │      │      :8085       │
└────────┬─────────┘      └────────┬─────────┘      └────────┬─────────┘
         │                         │                         │
         │    ┌─────────────────────┴─────────────────────┐    │
         │    │              MySQL :3306                 │    │
         │    │         (gacha_system_prod)               │    │
         │    └────────────────────────────────────────────┘    │
         │                                                        │
         ├────────────────────┬────────────────────┬──────────────┤
         ▼                    ▼                    ▼              ▼
┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────┐
│      Redis      │  │    RabbitMQ      │  │  Elasticsearch   │  │game-service  │
│      :6379      │  │      :5672       │  │      :9200       │  │    :8082     │
└──────────────────┘  └────────┬─────────┘  └──────────────────┘  └──────────────┘
                                │
                       ┌────────┴────────┐
                       ▼                ▼
              ┌──────────────┐  ┌──────────────┐
              │gacha-service │  │    微信支付   │
              │    :8083     │  │     API      │
              └──────────────┘  └──────────────┘
```

### 2.2 技术栈选型

#### 2.2.1 前端技术栈

| 技术组件 | 版本 | 应用场景 |
|---------|------|---------|
| **React** | 19.2.4 / 19.2.5 | 核心 UI 框架，函数组件 + Hooks |
| **React Router** | 7.14.0 / 7.14.2 | 客户端路由管理 |
| **Vite** | 8.0.1 / 8.0.10 | 构建工具，HMR 热更新 |
| **Ant Design** | 6.3.6 | 企业级 UI 组件库 (CMS) |
| **@ant-design/charts** | 2.6.7 | 数据可视化图表 |
| **@stomp/stompjs** | 7.3.0 | WebSocket STOMP 协议客户端 |
| **sockjs-client** | 1.6.1 | WebSocket 模拟库 |
| **Axios** | 1.15.2 | HTTP 客户端 |
| **Day.js** | 1.11.20 | 日期处理 |

#### 2.2.2 后端技术栈

| 技术组件 | 版本 | 应用场景 |
|---------|------|---------|
| **Spring Boot** | 2.7.18 | 核心框架 |
| **Spring Data JPA** | - | ORM 数据访问 |
| **Spring Data Redis** | - | Redis 缓存集成 |
| **Spring Data Elasticsearch** | - | 搜索引擎集成 |
| **Spring AMQP** | - | RabbitMQ 消息队列 |
| **Spring Security** | - | 安全认证框架 |
| **Spring WebSocket** | - | 实时通信 |
| **MySQL** | 8.0 | 主数据库 |
| **Redis** | 6.x | 缓存/会话/消息队列 |
| **Elasticsearch** | - | 全文搜索引擎 |
| **RabbitMQ** | 3-management | 消息中间件 |
| **JWT (jjwt)** | 0.11.5 | 无状态认证 |
| **Lombok** | 1.18.30 | 注解处理器 |
| **Hutool** | 5.8.23 | Java 工具库 |
| **Aliyun OSS SDK** | 3.17.1 | 对象存储 |
| **WechatPay SDK** | 0.2.17 | 微信支付 API v3 |

### 2.3 模块间交互关系

#### 2.3.1 服务通信架构

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           前端 (Browser)                                 │
└─────────────────────────────────────────────────────────────────────────┘
                    │                                    ▲
                    │ HTTP/REST                          │
                    ▼                                    │
┌─────────────────────────────────────────────────────────────────────────┐
│                      Nginx 反向代理层                                     │
│   /api/*    → mall-service:8081   /cms/* → cms-service:8085              │
│   /auth/*   → auth-service:8084   /game/* → game-service:8082            │
└─────────────────────────────────────────────────────────────────────────┘
```

#### 2.3.2 数据流转路径

##### 用户登录流程

```
用户 → Frontend → Auth Service → MySQL (验证)
                          ↓
                     生成 JWT Token
                          ↓
用户 ← Frontend ← Auth Service (返回 Token，存储到 localStorage)
```

##### 游戏购买流程

```
1. 用户选择游戏 → 加入购物车
2. 用户结算 → 创建订单 (pending)
3. 设置订单超时 (Redis 15分钟)
4. 发起微信支付
5. 支付回调 → 更新订单 (paid)
6. MQ 通知 → 异步发送邮件
7. MQ 通知 → 更新 ES 销量
8. 返回结果给用户
```

##### 签到奖励流程

```
1. 用户签到 → Redis 检查今日是否已签到
2. 计算连续签到天数
3. 计算奖励 (基础 + 连续签到 bonus)
4. 扣除/增加积分和余额
5. 记录签到明细 → MySQL
6. 更新 Redis 连续签到缓存
```

### 2.4 CI/CD 自动化部署流程

#### 2.4.1 GitHub Actions 工作流

```yaml
name: 部署Steam项目

on:
  push:
    branches:
      - master

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 11
        uses: actions/setup-java@v4
        with:
          java-version: '11'
          distribution: 'temurin'
          cache: maven

      - name: Build with Maven
        run: mvn clean package -Pprod -Dmaven.test.skip=true

      - name: Deploy to Server
        uses: appleboy/ssh-action@master
        with:
          host: ${{ secrets.SERVER_HOST }}
          username: ${{ secrets.SERVER_USER }}
          key: ${{ secrets.SERVER_PRIVATE_KEY }}
          script: |
            cd /opt/gacha-system
            git pull
            docker compose up -d --build
            sleep 15
            curl -X POST http://localhost:8081/api/sync/games
```

#### 2.4.2 CI/CD 实现机制

| 阶段 | 操作 | 说明 |
|-----|------|------|
| **触发** | `push to master` | 代码推送自动触发 |
| **构建** | `mvn clean package` | Maven 编译打包，跳过测试 |
| **部署** | `ssh to server` | 远程连接部署服务器 |
| **服务更新** | `docker compose up -d` | 构建并启动所有容器 |
| **数据同步** | `ES sync API` | 同步游戏数据到 Elasticsearch |

#### 2.4.3 CI/CD 范畴说明

> **说明**: 本项目采用 **持续集成 (CI)** + **持续部署 (CD)** 混合模式：
> - **持续集成 (CI)**: 每次 push 自动执行 Maven 构建和测试
> - **持续部署 (CD)**: master 分支通过构建后自动部署到生产服务器

---

## 3. 环境要求与安装部署流程

### 3.1 环境要求

#### 3.1.1 开发环境

| 组件 | 版本要求 | 说明 |
|-----|---------|------|
| **JDK** | 11+ | 推荐 Temurin JDK 11 |
| **Node.js** | 16+ | 前端构建工具 |
| **npm** | 8+ | 前端依赖管理 |
| **MySQL** | 8.0+ | 主数据库 |
| **Redis** | 6.x | 缓存 (可选，本地开发可关闭) |
| **Elasticsearch** | 8.x | 搜索引擎 (可选) |
| **RabbitMQ** | 3.x | 消息队列 (可选) |
| **Maven** | 3.6+ | 后端构建工具 |

#### 3.1.2 生产环境

| 组件 | 最低配置 | 推荐配置 |
|-----|---------|---------|
| **CPU** | 2 核 | 4 核+ |
| **内存** | 4 GB | 8 GB+ |
| **磁盘** | 50 GB SSD | 100 GB SSD+ |
| **操作系统** | Ubuntu 20.04 / CentOS 7+ | Ubuntu 22.04 LTS |

### 3.2 开发环境配置

#### 3.2.1 后端服务启动

```bash
# 1. 进入 mall-service 目录
cd mall-service

# 2. 修改 application.yml 配置数据库连接
# 编辑 src/main/resources/application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/gacha_system_dev
    username: root
    password: your_password

# 3. 启动后端服务
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

#### 3.2.2 前端服务启动

```bash
# 1. 进入 game-mall 目录
cd game-mall

# 2. 安装依赖
npm install

# 3. 启动开发服务器
npm run dev

# 4. 访问 http://localhost:5173
```

#### 3.2.3 CMS 后台启动

```bash
# 1. 进入 cms-admin 目录
cd cms-admin

# 2. 安装依赖
npm install

# 3. 启动开发服务器
npm run dev

# 4. 访问 http://localhost:5174
```

#### 3.2.4 数据库初始化

```bash
# 1. 登录 MySQL
mysql -u root -p

# 2. 创建数据库
CREATE DATABASE gacha_system_dev DEFAULT CHARACTER SET utf8mb4;

# 3. 执行初始化脚本
USE gacha_system_dev;
SOURCE database/init.sql;
```

### 3.3 生产环境部署

#### 3.3.1 服务器准备

```bash
# 1. 更新系统包
sudo apt update && sudo apt upgrade -y

# 2. 安装 Docker 和 Docker Compose
curl -fsSL https://get.docker.com | sh
sudo apt install docker-compose -y

# 3. 创建项目目录
sudo mkdir -p /opt/gacha-system
cd /opt/gacha-system

# 4. 上传项目文件
git clone https://github.com/your-repo/gacha-system.git .
```

#### 3.3.2 环境变量配置

```bash
# 创建生产环境配置文件
cat > .env.prod << EOF
# 数据库配置
DB_HOST=mysql
DB_PORT=3306
DB_NAME=gacha_system_prod
DB_USERNAME=root
DB_PASSWORD=your_secure_password

# Redis 配置
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password

# RabbitMQ 配置
RABBITMQ_HOST=rabbitmq
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=admin
RABBITMQ_PASSWORD=your_rabbitmq_password

# JWT 配置
JWT_SECRET=your-production-jwt-secret-key

# 游戏图片路径
GAME_PAPERS_PATH=/opt/gacha-system/GamePapers
EOF
```

#### 3.3.3 Docker Compose 部署

```bash
# 1. 启动所有服务
docker-compose up -d

# 2. 查看服务状态
docker-compose ps

# 3. 查看日志
docker-compose logs -f mall-service

# 4. 同步游戏数据到 ES
curl -X POST http://localhost:8081/api/sync/games
```

#### 3.3.4 Nginx 配置

```nginx
# /etc/nginx/conf.d/gacha-system.conf

upstream mall_backend {
    server 127.0.0.1:8081;
}

upstream cms_backend {
    server 127.0.0.1:8085;
}

server {
    listen 80;
    server_name your-domain.com;

    # 前端静态资源
    location / {
        root /opt/gacha-system/game-mall/dist;
        try_files $uri $uri/ /index.html;
    }

    # Mall API 代理
    location /api/ {
        proxy_pass http://mall_backend/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # CMS API 代理
    location /cms/ {
        proxy_pass http://cms_backend/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # 游戏图片静态资源
    location /GamePapers/ {
        alias /opt/gacha-system/GamePapers/;
        expires 30d;
    }
}
```

---

## 4. 详细使用指南

### 4.1 用户操作流程

#### 4.1.1 用户注册与登录

```
步骤 1: 访问前端页面 http://localhost:5173
步骤 2: 点击右上角「登录/注册」按钮
步骤 3: 选择注册模式，填写用户名、密码、邮箱
步骤 4: 获取邮箱验证码，填写后提交
步骤 5: 注册成功，自动登录
```

**测试账号**:
```
用户名: working2026
密码: 123456
```

#### 4.1.2 游戏购买流程

```
步骤 1: 浏览游戏列表，使用分类/标签筛选
步骤 2: 点击游戏卡片进入详情页
步骤 3: 查看游戏介绍、截图、系统要求、评论
步骤 4: 点击「加入购物车」或「立即购买」
步骤 5: 打开购物车侧边栏，确认商品
步骤 6: 点击「结算」发起支付
步骤 7: 选择支付方式 (余额支付)
步骤 8: 支付成功，游戏加入游戏库
```

#### 4.1.3 签到奖励流程

```
步骤 1: 登录后进入个人中心
步骤 2: 点击「每日签到」按钮
步骤 3: 系统计算连续签到天数和奖励
步骤 4: 领取积分和余额奖励
步骤 5: 查看签到日历和累计奖励
```

### 4.2 CMS 后台操作

#### 4.2.1 管理员登录

```
步骤 1: 访问 CMS 后台 http://localhost:5174
步骤 2: 输入管理员账号密码
步骤 3: 进入仪表盘查看数据概览
```

#### 4.2.2 游戏管理

```
步骤 1: 进入「游戏管理」菜单
步骤 2: 查看游戏列表，支持关键词搜索
步骤 3: 点击「编辑」修改游戏信息
步骤 4: 修改价格、上下架状态
步骤 5: 保存修改
```

#### 4.2.3 公告发布

```
步骤 1: 进入「公告管理」菜单
步骤 2: 点击「新建公告」
步骤 3: 填写公告标题、内容、类型
步骤 4: 设置发布时间和优先级
步骤 5: 点击「发布」
步骤 6: 用户端实时收到通知
```

### 4.3 功能模块说明

#### 4.3.1 购物车模块

```javascript
// 前端 API 调用示例
const MALL_API_BASE = '/api'

// 获取购物车
const response = await fetch(`${MALL_API_BASE}/cart?userId=${userId}`)
const result = await response.json()

// 添加到购物车
await fetch(`${MALL_API_BASE}/cart?userId=${userId}&gameId=${gameId}`, {
  method: 'POST'
})

// 移除购物车商品
await fetch(`${MALL_API_BASE}/cart/${gameId}?userId=${userId}`, {
  method: 'DELETE'
})
```

#### 4.3.2 订单模块

| 订单状态 | 说明 |
|---------|------|
| `pending` | 待支付，创建后 15 分钟自动取消 |
| `paid` | 已支付，等待发货 |
| `completed` | 已完成，交易成功 |
| `cancelled` | 已取消 |

#### 4.3.3 签到奖励规则

| 连续天数 | 积分奖励 | 余额奖励 | 特殊奖励 |
|---------|---------|---------|---------|
| 每日基础 | 10 | 1 元 | - |
| 第 7 天 | +88 | +10 元 | - |
| 第 30 天 | +888 | +100 元 | 手机兑换券 |

---

## 5. 完整API接口文档

### 5.1 基础信息

- **服务地址**: `http://localhost:8081/api`
- **CMS 服务地址**: `http://localhost:8085`
- **数据格式**: JSON
- **字符编码**: UTF-8

### 5.2 通用响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
|-----|------|------|
| `code` | int | 状态码，200=成功 |
| `message` | string | 响应消息 |
| `data` | object | 数据对象 |

### 5.3 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

### 5.4 游戏相关接口

#### 5.4.1 获取游戏列表（分页）

**GET** `/games`

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| `page` | int | 否 | 页码，默认 0 |
| `size` | int | 否 | 每页数量，默认 20 |
| `sortBy` | string | 否 | 排序字段，默认 id |
| `order` | string | 否 | asc/desc，默认 asc |

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "title": "巫师3：狂猎",
        "coverImage": "/GamePapers/witcher3.jpg",
        "currentPrice": 99.00,
        "basePrice": 149.00,
        "discountRate": 0.66,
        "rating": 9.5,
        "totalSales": 1000
      }
    ],
    "totalElements": 100,
    "totalPages": 5,
    "currentPage": 0,
    "pageSize": 20
  }
}
```

#### 5.4.2 获取游戏详情

**GET** `/games/{id}`

**路径参数**:

| 参数 | 类型 | 说明 |
|-----|------|------|
| `id` | long | 游戏 ID |

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "title": "巫师3：狂猎",
    "shortDescription": "一款开放世界 RPG 游戏",
    "coverImage": "/GamePapers/witcher3.jpg",
    "screenshots": ["/GamePapers/witcher3_1.jpg"],
    "basePrice": 149.00,
    "currentPrice": 99.00,
    "discountRate": 0.66,
    "rating": 9.5,
    "totalReviews": 500,
    "totalSales": 1000,
    "isOnSale": true,
    "categories": ["RPG", "开放世界"],
    "tags": ["奇幻", "剧情丰富"],
    "systemRequirements": {
      "os": "Windows 7/8/10",
      "processor": "Intel Core i5-2500K",
      "memory": "6 GB RAM",
      "graphics": "NVIDIA GeForce GTX 660"
    }
  }
}
```

#### 5.4.3 搜索游戏

**GET** `/games/search`

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| `keyword` | string | 是 | 搜索关键词 |

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "title": "巫师3：狂猎",
      "shortDescription": "一款开放世界 RPG 游戏",
      "currentPrice": 99.00,
      "coverImage": "/GamePapers/witcher3.jpg"
    }
  ]
}
```

#### 5.4.4 获取所有游戏（含标签）

**GET** `/games/all-with-tags`

#### 5.4.5 获取精选游戏

**GET** `/games/featured`

#### 5.4.6 获取所有分类

**GET** `/games/categories`

#### 5.4.7 获取所有标签

**GET** `/games/tags`

---

### 5.5 购物车接口

#### 5.5.1 获取用户购物车

**GET** `/cart?userId={userId}`

#### 5.5.2 添加到购物车

**POST** `/cart?userId={userId}&gameId={gameId}`

#### 5.5.3 从购物车移除

**DELETE** `/cart/{gameId}?userId={userId}`

#### 5.5.4 更新选中状态

**PUT** `/cart/{gameId}/check?userId={userId}&checked={true/false}`

#### 5.5.5 获取购物车数量

**GET** `/cart/count?userId={userId}`

---

### 5.6 愿望单接口

#### 5.6.1 获取用户愿望单

**GET** `/wishlist?userId={userId}`

#### 5.6.2 添加到愿望单

**POST** `/wishlist?userId={userId}&gameId={gameId}`

#### 5.6.3 从愿望单移除

**DELETE** `/wishlist/{gameId}?userId={userId}`

#### 5.6.4 检查游戏是否在愿望单

**GET** `/wishlist/check?userId={userId}&gameId={gameId}`

---

### 5.7 订单接口

#### 5.7.1 创建订单

**POST** `/orders/create`

**请求体**:

```json
{
  "userId": 1,
  "paymentMethod": "balance",
  "items": [
    {
      "gameId": 1,
      "quantity": 1
    }
  ]
}
```

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| `userId` | long | 是 | 用户 ID |
| `paymentMethod` | string | 是 | 支付方式：balance/wechat/alipay |
| `items` | array | 是 | 商品列表 |
| `items[].gameId` | long | 是 | 游戏 ID |
| `items[].quantity` | int | 是 | 数量 |

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "orderId": 12345,
    "orderNo": "ORD20260426123456789",
    "totalAmount": 99.00,
    "status": "pending",
    "expireTime": "2026-04-26 12:30:00"
  }
}
```

#### 5.7.2 获取用户订单列表

**GET** `/orders?userId={userId}&status={status}`

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| `userId` | long | 是 | 用户 ID |
| `status` | string | 否 | 订单状态：all/pending/completed/cancelled |

#### 5.7.3 获取订单详情

**GET** `/orders/{orderId}?userId={userId}`

#### 5.7.4 取消订单

**POST** `/orders/{orderId}/cancel?userId={userId}`

#### 5.7.5 获取已购游戏列表

**GET** `/orders/purchased-games?userId={userId}`

---

### 5.8 钱包接口

#### 5.8.1 获取用户钱包信息

**GET** `/wallet/balance?userId={userId}`

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 1,
    "balance": 100.00,
    "frozenBalance": 0.00,
    "totalRecharge": 500.00,
    "totalConsumed": 400.00
  }
}
```

#### 5.8.2 用户充值

**POST** `/wallet/recharge`

**请求体**:

```json
{
  "userId": 1,
  "amount": 100.00,
  "paymentMethod": "alipay"
}
```

#### 5.8.3 获取交易记录

**GET** `/wallet/transactions?userId={userId}&type={type}`

| 参数 | 类型 | 说明 |
|-----|------|------|
| `type` | string | 交易类型：all/recharge/purchase/refund |

---

### 5.9 轮播图接口

#### 5.9.1 获取启用的轮播图

**GET** `/banners/active`

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "title": "GTA V 特惠",
      "imageUrl": "https://example.com/banner1.jpg",
      "targetType": "game",
      "targetId": 1,
      "sortOrder": 1
    }
  ]
}
```

---

### 5.10 游戏评论接口

#### 5.10.1 获取游戏评论列表

**GET** `/reviews/game/{gameId}?page=0&size=10&sortBy=createdAt&order=desc`

#### 5.10.2 发表评论

**POST** `/reviews`

**请求体**:

```json
{
  "userId": 1,
  "username": "用户名",
  "userAvatar": "头像URL",
  "gameId": 1,
  "rating": 9,
  "title": "评论标题",
  "content": "评论内容",
  "pros": "优点",
  "cons": "缺点",
  "playHours": 50.5
}
```

#### 5.10.3 删除评论

**DELETE** `/reviews/{reviewId}?userId={userId}`

#### 5.10.4 点赞/点踩评论

**POST** `/reviews/{reviewId}/helpful?userId={userId}&isHelpful={true/false}`

---

### 5.11 通知接口

#### 5.11.1 获取用户通知列表

**GET** `/notifications?userId={userId}&isRead={true/false}`

#### 5.11.2 标记通知为已读

**PUT** `/notifications/{notificationId}/read?userId={userId}`

#### 5.11.3 标记所有通知为已读

**PUT** `/notifications/read-all?userId={userId}`

#### 5.11.4 获取未读通知数量

**GET** `/notifications/unread-count?userId={userId}`

---

### 5.12 CMS 管理接口

#### 5.12.1 仪表盘统计

**GET** `/cms/dashboard/stats`

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalUsers": 1000,
    "todayNewUsers": 50,
    "totalOrders": 5000,
    "todayOrders": 100,
    "totalRevenue": 500000.00,
    "todayRevenue": 5000.00,
    "totalGames": 200,
    "pendingOrders": 20,
    "pendingReviews": 10
  }
}
```

#### 5.12.2 获取用户列表

**GET** `/cms/users?keyword={keyword}&page={page}&size={size}`

#### 5.12.3 获取游戏列表

**GET** `/cms/games?keyword={keyword}&page={page}&size={size}`

#### 5.12.4 更新游戏

**PUT** `/cms/games/{id}`

#### 5.12.5 获取订单列表

**GET** `/cms/orders?status={status}&page={page}&size={size}`

#### 5.12.6 获取公告列表

**GET** `/cms/announcements?page={page}&size={size}`

#### 5.12.7 创建公告

**POST** `/cms/announcements`

#### 5.12.8 更新公告

**PUT** `/cms/announcements/{id}`

#### 5.12.9 删除公告

**DELETE** `/cms/announcements/{id}`

---

### 5.13 快速测试命令

```bash
# 1. 获取游戏列表
curl http://localhost:8081/api/games/all-with-tags

# 2. 获取用户钱包
curl http://localhost:8081/api/wallet/balance?userId=1

# 3. 用户充值
curl -X POST http://localhost:8081/api/wallet/recharge \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"amount":100.00,"paymentMethod":"alipay"}'

# 4. 创建订单
curl -X POST http://localhost:8081/api/orders/create \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"paymentMethod":"balance","items":[{"gameId":1,"quantity":1}]}'

# 5. 获取轮播图
curl http://localhost:8081/api/banners/active

# 6. 获取 CMS 统计数据
curl http://localhost:8085/cms/dashboard/stats
```

---

## 6. 常见问题解答

### 6.1 环境配置问题

#### Q1: 启动后端服务时报错 "Connection refused"

**A**: 请检查以下配置：

1. MySQL 服务是否启动：
   ```bash
   mysql -u root -p
   ```

2. 确认 `application.yml` 中的数据库连接信息：
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/gacha_system_dev
       username: root
       password: your_password
   ```

#### Q2: Redis/RabbitMQ 连接失败

**A**: 本地开发环境可以禁用 Redis 和 RabbitMQ，系统会自动回退到数据库模式。确保 `application.yml` 中配置：

```yaml
spring:
  redis:
    enabled: false
  rabbitmq:
    enabled: false
```

#### Q3: 前端无法访问后端 API

**A**: 检查 Vite 代理配置是否正确 (`vite.config.js`)：

```javascript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8081',
      changeOrigin: true
    }
  }
}
```

### 6.2 功能使用问题

#### Q4: 订单超时自动取消未生效

**A**: 需要启用 Redis 键空间通知：

```bash
# 修改 redis.conf
notify-keyspace-events Ex
```

或使用 Docker 部署时添加启动参数：
```bash
redis-server --notify-keyspace-events Ex
```

#### Q5: 微信支付无法调起

**A**: 确保配置了有效的微信支付商户信息：

```yaml
wechat:
  pay:
    app-id: your-app-id
    mch-id: your-mch-id
    api-v3-key: your-api-v3-key
    private-key: your-private-key
    serial-no: your-serial-no
```

本地开发环境可使用模拟支付模式。

#### Q6: 邮件通知未发送

**A**: 检查邮件配置：

```yaml
spring:
  mail:
    host: smtp.example.com
    port: 587
    username: your-email
    password: your-password
```

### 6.3 部署问题

#### Q7: Docker 容器启动失败

**A**: 按以下顺序检查：

1. 查看容器日志：
   ```bash
   docker-compose logs -f
   ```

2. 检查端口占用：
   ```bash
   netstat -tlnp | grep 3306
   ```

3. 确认 MySQL 健康检查通过后再启动其他服务

#### Q8: GitHub Actions 部署失败

**A**: 检查以下配置：

1. Secrets 配置是否完整：
   - `SERVER_HOST`
   - `SERVER_USER`
   - `SERVER_PRIVATE_KEY`

2. 服务器 SSH 连接是否正常

3. 目标目录权限是否正确

### 6.4 其他问题

#### Q9: 如何修改 JWT 密钥？

**A**: 在 `application.yml` 中修改：

```yaml
jwt:
  secret: your-new-secret-key-must-be-at-least-256-bits
  expiration: 86400000
```

> 生产环境务必使用强密钥并通过环境变量配置。

#### Q10: 如何重置管理员密码？

**A**: 通过数据库直接修改：

```sql
UPDATE admins SET password = 'new_encrypted_password' WHERE username = 'admin';
```

或使用开发工具生成加密密码后更新。

---

## 7. 贡献规范与代码提交指南

### 7.1 Git 分支管理策略

#### 7.1.1 分支说明

| 分支类型 | 命名规范 | 说明 |
|---------|---------|------|
| **main** | `main` | 生产版本，禁止直接推送 |
| **develop** | `develop` | 开发主分支，集成最新功能 |
| **feature** | `feature/*` | 功能分支，从 develop 创建 |
| **fix** | `fix/*` | 修复分支，从 develop 创建 |
| **hotfix** | `hotfix/*` | 紧急修复，从 main 创建 |

#### 7.1.2 分支流程图

```
┌─────────────────────────────────────────────────────────────────┐
│                           main (生产分支)                        │
│  ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■  │
└─────────────────────────┬───────────────────────────────────────┘
                          │
                          │ merge (经过测试)
                          │
┌─────────────────────────▼───────────────────────────────────────┐
│                      develop (开发主分支)                         │
│  ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■  │
└───────┬─────────────────┬─────────────────┬─────────────────────┘
        │                 │                 │
        │ create          │ create          │ create
        ▼                 ▼                 ▼
   ┌─────────┐       ┌─────────┐       ┌─────────┐
   │feature/*│       │feature/*│       │  fix/*  │
   └────┬────┘       └────┬────┘       └────┬────┘
        │                 │                 │
        │ merge           │ merge           │ merge
        └────────┬────────┘                 │
                 └──────────────────────────┘
```

### 7.2 代码提交规范

#### 7.2.1 Commit Message 格式

```
<type>(<scope>): <subject>

<body>

<footer>
```

#### 7.2.2 Type 类型

| 类型 | 说明 |
|-----|------|
| `feat` | 新功能 |
| `fix` | Bug 修复 |
| `docs` | 文档更新 |
| `style` | 代码格式（不影响功能） |
| `refactor` | 重构（不是新功能或修复） |
| `perf` | 性能优化 |
| `test` | 测试相关 |
| `chore` | 构建/工具相关 |

#### 7.2.3 示例

```bash
# 新功能
git commit -m "feat(cart): 添加购物车批量删除功能

- 支持多选删除
- 添加确认对话框
- 优化删除动画"

# Bug 修复
git commit -m "fix(order): 修复订单超时取消失败的问题

问题原因：Redis 键过期事件未正确触发
解决方案：修改键过期监听配置"

# 文档更新
git commit -m "docs: 更新 API 接口文档"
```

### 7.3 代码审查流程

#### 7.3.1 Pull Request 流程

```
1. 从 develop 创建功能分支
   git checkout develop
   git pull origin develop
   git checkout -b feature/user-auth

2. 开发并提交代码
   git add .
   git commit -m "feat: 实现用户认证功能"

3. 推送到远程
   git push origin feature/user-auth

4. 创建 Pull Request
   - 选择目标分支: develop
   - 填写 PR 描述
   - 指定代码审查者

5. 等待审查通过后合并
```

#### 7.3.2 PR 描述模板

```markdown
## 变更描述
简要说明本次变更的内容和目的

## 变更类型
- [ ] 新功能
- [ ] Bug 修复
- [ ] 重构
- [ ] 文档更新

## 测试情况
- [ ] 本地测试通过
- [ ] 单元测试通过
- [ ] 集成测试通过

## 影响范围
说明本次变更影响的模块和功能

## 相关问题
关联的 Issue 编号
```

### 7.4 代码规范

#### 7.4.1 Java 代码规范

- 遵循 Google Java Style Guide
- 类名使用 UpperCamelCase
- 方法名和变量名使用 lowerCamelCase
- 常量使用 UPPER_SNAKE_CASE
- 使用 Lombok 减少样板代码
- 避免长方法，控制在 50 行以内

#### 7.4.2 React 代码规范

- 组件使用 PascalCase
- Hooks 使用 use 前缀
- CSS 类名使用 kebab-case
- 组件文件使用 .jsx 扩展名
- 工具函数使用 .js 扩展名

#### 7.4.3 Git 规范

- 每次提交应该是原子性的（单一目的）
- 提交信息使用中文，简洁明了
- 不要提交敏感信息（密码、密钥等）
- 保持提交历史清晰，避免无效合并

### 7.5 环境配置注意事项

> **安全提醒**:
> - 切勿将包含真实凭据的配置文件提交到仓库
> - 生产环境配置使用环境变量而非硬编码
> - 定期轮换密钥和密码

---

## 附录

### A. 快捷命令参考

```bash
# 后端构建
mvn clean package -Pprod -Dmaven.test.skip=true

# 后端启动
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 前端依赖安装
npm install

# 前端构建生产版本
npm run build

# 前端开发服务器
npm run dev

# Docker 服务启动
docker-compose up -d

# Docker 服务停止
docker-compose down
```

### B. 端口占用参考

| 服务 | 端口 | 说明 |
|-----|------|------|
| game-mall (前端) | 5173 | 开发服务器 |
| cms-admin (前端) | 5174 | 开发服务器 |
| mall-service | 8081 | 商城后端 |
| cms-service | 8085 | CMS 后端 |
| auth-service | 8084 | 认证服务 |
| game-service | 8082 | 游戏服务 |
| gacha-service | 8083 | 抽奖服务 |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |
| RabbitMQ | 5672 | 消息队列 |
| RabbitMQ Management | 15672 | 管理界面 |
| Elasticsearch | 9200 | 搜索引擎 |
| Nginx | 80/443 | Web 服务器 |

### C. 相关文档链接

- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [React 官方文档](https://react.dev)
- [Ant Design 组件库](https://ant.design)
- [Vite 构建工具](https://vitejs.dev)
- [MySQL 8.0 文档](https://dev.mysql.com/doc/refman/8.0/en/)
- [Docker 官方文档](https://docs.docker.com)

---

**文档版本**: v1.0.0
**最后更新**: 2026-04-26
**维护者**: 项目开发团队
