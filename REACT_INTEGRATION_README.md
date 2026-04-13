# React 游戏商城前后端联调指南

## 🎉 已完成的功能集成

### ✅ 后端服务（已启动）
- **认证服务** (端口 8081) - 用户登录、注册、Token 颁发
- **游戏服务** (端口 8082) - 充值、签到、积分兑换
- **抽奖服务** (端口 8083) - 幸运抽奖

### ✅ React 前端功能
1. **用户认证系统**
   - 登录/注册模态框
   - JWT Token 自动管理
   - 用户信息持久化（localStorage）

2. **购物车功能**
   - 添加商品到购物车
   - 购物车侧边栏展示
   - 商品数量统计
   - 总价计算

3. **钱包系统**
   - 实时余额显示
   - 充值按钮快捷入口
   - 购买时余额验证

4. **UI/UX 优化**
   - GTA6 风格主题
   - 毛玻璃效果
   - 流畅动画过渡
   - 响应式设计

## 🚀 快速开始

### 1. 访问前端页面
打开浏览器访问：**http://localhost:5175**

### 2. 测试账号
```
用户名：working2026
密码：123456
```

### 3. 功能测试流程

#### Step 1: 登录
1. 点击右上角「登录/注册」按钮
2. 输入测试账号
3. 点击登录
4. 成功后会显示用户名和余额

#### Step 2: 浏览商品
1. 使用分类筛选查看不同游戏
2. 使用搜索框查找特定游戏
3. 查看精选推荐区域

#### Step 3: 加入购物车
1. 点击任意商品的「购买」按钮
2. 商品添加到购物车
3. 购物车图标显示商品数量

#### Step 4: 查看购物车
1. 点击右上角「购物车」按钮
2. 侧边栏显示所有商品
3. 可以移除不需要的商品
4. 查看总计金额

#### Step 5: 充值（可选）
1. 点击「💳 充值」按钮
2. 跳转到充值页面
3. 输入充值金额
4. 确认充值

#### Step 6: 购买商品
1. 确保余额充足
2. 打开购物车
3. 点击「立即购买」
4. 系统验证余额并完成购买
5. 余额自动扣减

## 🔧 API 集成详情

### 认证相关 API

#### 登录接口
```javascript
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "username": "working2026",
  "password": "123456"
}

Response:
{
  "success": true,
  "data": {
    "user": { "id": 15, "username": "working2026" },
    "token": "eyJhbGciOiJIUzUxMiJ9..."
  }
}
```

#### 获取用户信息
```javascript
GET http://localhost:8081/api/auth/info
Authorization: Bearer {token}

Response:
{
  "success": true,
  "data": {
    "id": 15,
    "username": "working2026",
    "balance": 100.0,
    "points": 0
  }
}
```

#### 注册接口
```javascript
POST http://localhost:8081/api/auth/register/body
Content-Type: application/json

{
  "username": "newuser",
  "password": "123456",
  "confirmPassword": "123456"
}
```

### 游戏服务 API

#### 充值接口
```javascript
POST http://localhost:8082/api/recharge
Content-Type: application/json
Authorization: Bearer {token}

{
  "userId": 15,
  "amount": 100,
  "rechargeMethod": "ADMIN_DIRECT"
}
```

## 📊 数据流转图

```
用户操作 → React 组件 → Fetch API → 后端服务 → 数据库
   ↓                                       ↓
UI 更新 ← State 变化 ← JSON 响应 ← JWT 验证 ← 业务逻辑
```

## ⚠️ 注意事项

### CORS 跨域配置
所有后端服务已配置 CORS 允许前端访问：
- auth-service: `@CrossOrigin(origins = "*")`
- game-service: WebConfig 配置类
- gacha-service: `@CrossOrigin(origins = "*")`

### Token 管理
- Token 存储在 localStorage 中
- 有效期：24 小时
- 过期后需要重新登录
- 每次请求自动携带 Token

### 余额同步
- 登录后自动获取余额
- 充值后手动刷新余额
- 购买后实时更新余额

## 🐛 常见问题

### 1. 无法连接后端
**症状**: 点击登录无反应或报错
**解决**: 
- 检查后端服务是否启动
- 确认端口号正确（8081/8082/8083）
- 查看浏览器控制台错误信息

### 2. Token 失效
**症状**: 提示未授权访问
**解决**:
- 清除 localStorage
- 重新登录获取新 Token

### 3. 余额不足
**症状**: 购买时提示余额不足
**解决**:
- 点击充值按钮进行充值
- 或使用管理员权限直接修改余额

### 4. 购物车无法清空
**症状**: 购买后购物车仍有商品
**解决**:
- 检查购买逻辑是否正常执行
- 确认 setCart([]) 被调用

## 🎯 下一步优化建议

1. **订单系统**
   - 添加订单历史记录
   - 订单状态追踪

2. **支付集成**
   - 接入真实支付接口
   - 支持多种支付方式

3. **商品管理**
   - 从后端动态加载游戏列表
   - 添加商品详情页

4. **用户中心**
   - 完善个人信息编辑
   - 添加头像上传功能

5. **消息通知**
   - 添加 Toast 提示组件
   - 购买成功/失败通知

## 📝 开发日志

**本次更新**:
- ✅ 集成用户登录/注册功能
- ✅ 实现购物车完整逻辑
- ✅ 添加钱包余额显示
- ✅ 完成购买流程
- ✅ 优化 UI 样式和动画
- ✅ 修复已知 Bug

**技术栈**:
- Frontend: React 18 + Vite
- Backend: Spring Boot 2.7.18
- Database: MySQL 8.0
- Auth: JWT (HS512)

---

**最后更新时间**: 2026-04-04  
**当前版本**: v1.0.0
