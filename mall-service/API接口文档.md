# 游戏商城后端 API 接口文档

## 基础信息

- **服务地址**: `http://localhost:8081/api`
- **数据格式**: JSON
- **字符编码**: UTF-8

## 通用响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

---

## 1. 游戏相关接口

### 1.1 获取游戏列表（分页）
**GET** `/games`

**请求参数**:
- `page`: 页码，默认 0
- `size`: 每页数量，默认 20
- `sortBy`: 排序字段，默认 id
- `order`: 排序方式 asc/desc，默认 asc

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [...],
    "totalElements": 100,
    "totalPages": 5,
    "currentPage": 0,
    "pageSize": 20
  }
}
```

### 1.2 获取所有游戏（含标签）
**GET** `/games/all-with-tags`

**响应**: 返回所有游戏及其分类、标签信息

### 1.3 获取精选游戏
**GET** `/games/featured`

### 1.4 获取游戏详情
**GET** `/games/{id}`

**路径参数**:
- `id`: 游戏ID

### 1.5 搜索游戏
**GET** `/games/search?keyword=xxx`

**请求参数**:
- `keyword`: 搜索关键词

### 1.6 根据分类获取游戏
**GET** `/games/category/{categoryId}`

### 1.7 根据标签获取游戏
**GET** `/games/tag/{tagId}`

### 1.8 获取所有分类
**GET** `/games/categories`

### 1.9 获取所有标签
**GET** `/games/tags`

---

## 2. 购物车接口

### 2.1 获取用户购物车
**GET** `/cart?userId={userId}`

### 2.2 添加到购物车
**POST** `/cart?userId={userId}&gameId={gameId}`

### 2.3 从购物车移除
**DELETE** `/cart/{gameId}?userId={userId}`

### 2.4 更新购物车项选中状态
**PUT** `/cart/{gameId}/check?userId={userId}&checked={true/false}`

### 2.5 获取购物车商品数量
**GET** `/cart/count?userId={userId}`

---

## 3. 愿望单接口

### 3.1 获取用户愿望单
**GET** `/wishlist?userId={userId}`

### 3.2 添加到愿望单
**POST** `/wishlist?userId={userId}&gameId={gameId}`

### 3.3 从愿望单移除
**DELETE** `/wishlist/{gameId}?userId={userId}`

### 3.4 检查游戏是否在愿望单
**GET** `/wishlist/check?userId={userId}&gameId={gameId}`

---

## 4. 订单接口

### 4.1 创建订单
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

**支付方式**:
- `balance`: 余额支付
- `alipay`: 支付宝（待实现）
- `wechat`: 微信（待实现）

### 4.2 获取用户订单列表
**GET** `/orders?userId={userId}&status={status}`

**请求参数**:
- `userId`: 用户ID
- `status`: 订单状态（可选），all/pending/completed/cancelled

### 4.3 获取订单详情
**GET** `/orders/{orderId}?userId={userId}`

### 4.4 取消订单
**POST** `/orders/{orderId}/cancel?userId={userId}`

### 4.5 获取已购游戏列表
**GET** `/orders/purchased-games?userId={userId}`

---

## 5. 钱包接口

### 5.1 获取用户钱包信息
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

### 5.2 用户充值
**POST** `/wallet/recharge`

**请求体**:
```json
{
  "userId": 1,
  "amount": 100.00,
  "paymentMethod": "alipay"
}
```

### 5.3 获取交易记录
**GET** `/wallet/transactions?userId={userId}&type={type}`

**请求参数**:
- `userId`: 用户ID
- `type`: 交易类型（可选），all/recharge/purchase/refund

---

## 6. 轮播图接口

### 6.1 获取启用的轮播图
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
      "imageUrl": "https://...",
      "targetType": "game",
      "targetId": 1,
      "targetUrl": null,
      "sortOrder": 1
    }
  ]
}
```

---

## 7. 游戏评论接口

### 7.1 获取游戏评论列表
**GET** `/reviews/game/{gameId}?page=0&size=10&sortBy=createdAt&order=desc`

### 7.2 发表评论
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

### 7.3 删除评论
**DELETE** `/reviews/{reviewId}?userId={userId}`

### 7.4 点赞/点踩评论
**POST** `/reviews/{reviewId}/helpful?userId={userId}&isHelpful={true/false}`

---

## 8. 通知接口

### 8.1 获取用户通知列表
**GET** `/notifications?userId={userId}&isRead={true/false}`

### 8.2 标记通知为已读
**PUT** `/notifications/{notificationId}/read?userId={userId}`

### 8.3 标记所有通知为已读
**PUT** `/notifications/read-all?userId={userId}`

### 8.4 获取未读通知数量
**GET** `/notifications/unread-count?userId={userId}`

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权（需要登录） |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 注意事项

1. **用户认证**: 目前所有接口都允许匿名访问，后续会添加JWT认证
2. **余额支付**: 创建订单时如果选择余额支付，会立即扣款并更新订单状态
3. **跨域配置**: 已配置CORS允许所有域名访问
4. **数据库**: 使用MySQL 8.0，表结构见 `database/新建文本文档.SQL`
5. **中间件**: Redis、Elasticsearch、RabbitMQ 已配置但部分功能暂未启用

---

## 快速测试

### 1. 获取游戏列表
```bash
curl http://localhost:8081/api/games/all-with-tags
```

### 2. 获取用户钱包
```bash
curl http://localhost:8081/api/wallet/balance?userId=1
```

### 3. 用户充值
```bash
curl -X POST http://localhost:8081/api/wallet/recharge \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"amount":100.00,"paymentMethod":"alipay"}'
```

### 4. 创建订单
```bash
curl -X POST http://localhost:8081/api/orders/create \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "paymentMethod": "balance",
    "items": [{"gameId": 1, "quantity": 1}]
  }'
```

### 5. 获取轮播图
```bash
curl http://localhost:8081/api/banners/active
```
