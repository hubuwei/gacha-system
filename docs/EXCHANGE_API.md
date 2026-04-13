# 积分兑换系统 - API 接口文档

## 📋 目录
- [功能概述](#功能概述)
- [数据库表结构](#数据库表结构)
- [API 接口列表](#api-接口列表)
- [使用示例](#使用示例)

---

## 功能概述

积分兑换系统提供以下核心功能：
- ✅ 用户可以使用积分兑换礼品（显卡、手机等）
- ✅ 每日 0 点自动重置库存
- ✅ 兑换成功后填写收货地址（姓名、电话、地址）
- ✅ 兑换记录管理
- ✅ 库存管理和限制

---

## 数据库表结构

### 1. exchange_item (兑换物品表)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 物品 ID（主键） |
| name | VARCHAR(100) | 物品名称 |
| description | VARCHAR(500) | 物品描述 |
| icon_url | VARCHAR(500) | 物品图标 URL |
| required_points | INT | 所需积分 |
| total_stock | INT | 每日库存总量 |
| current_stock | INT | 当前剩余库存 |
| enabled | TINYINT(1) | 是否上架（0-下架，1-上架） |
| sort_weight | INT | 排序权重（数字越大越靠前） |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### 2. exchange_record (兑换记录表)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 兑换记录 ID（主键） |
| user_id | BIGINT | 用户 ID |
| item_id | BIGINT | 物品 ID |
| used_points | INT | 消耗积分 |
| status | TINYINT(4) | 兑换状态（0-待填写地址，1-已填写地址，2-已发货，3-已完成，-1-已取消） |
| exchange_date | DATE | 兑换日期 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### 3. delivery_address (收货地址表)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 地址 ID（主键） |
| exchange_record_id | BIGINT | 兑换记录 ID（唯一） |
| user_id | BIGINT | 用户 ID |
| recipient_name | VARCHAR(50) | 收货人姓名 |
| phone_number | VARCHAR(20) | 联系电话 |
| province | VARCHAR(50) | 省份 |
| city | VARCHAR(50) | 城市 |
| district | VARCHAR(50) | 区县 |
| detail_address | VARCHAR(500) | 详细地址 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

---

## API 接口列表

### 基础信息
- **服务地址**: `http://localhost:8082`
- **请求头**: `Authorization: Bearer {token}`

---

### 1. 获取可兑换物品列表
**GET** `/api/exchange/items`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "机械键盘",
      "description": "Cherry 轴体，RGB 背光，游戏办公两用",
      "iconUrl": "https://example.com/images/keyboard.jpg",
      "requiredPoints": 5000,
      "totalStock": 10,
      "currentStock": 10,
      "enabled": true,
      "sortWeight": 100
    }
  ]
}
```

---

### 2. 获取物品详情
**GET** `/api/exchange/items/{itemId}`

**路径参数**:
- `itemId`: 物品 ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "机械键盘",
    "requiredPoints": 5000,
    "currentStock": 10
  }
}
```

---

### 3. 执行积分兑换
**POST** `/api/exchange`

**请求头**:
```
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体**:
```json
{
  "itemId": 1
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "userId": 123,
    "itemId": 1,
    "usedPoints": 5000,
    "status": 0,
    "exchangeDate": "2026-04-01"
  }
}
```

**错误响应**:
```json
{
  "code": 400,
  "message": "积分不足"
}
```

---

### 4. 查询用户兑换记录
**GET** `/api/exchange/records`

**请求头**:
```
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "userId": 123,
      "itemId": 1,
      "usedPoints": 5000,
      "status": 0,
      "exchangeDate": "2026-04-01",
      "createdAt": "2026-04-01T10:30:00"
    }
  ]
}
```

---

### 5. 填写/更新收货地址
**POST** `/api/exchange/address`

**请求头**:
```
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体**:
```json
{
  "exchangeRecordId": 1,
  "recipientName": "张三",
  "phoneNumber": "13800138000",
  "province": "广东省",
  "city": "深圳市",
  "district": "南山区",
  "detailAddress": "xx 街道 xx 小区 xx 栋 xx 单元 xxx 室"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "exchangeRecordId": 1,
    "userId": 123,
    "recipientName": "张三",
    "phoneNumber": "13800138000",
    "province": "广东省",
    "city": "深圳市",
    "district": "南山区",
    "detailAddress": "xx 街道 xx 小区 xx 栋 xx 单元 xxx 室"
  }
}
```

---

### 6. 查询收货地址
**GET** `/api/exchange/address/{exchangeRecordId}`

**路径参数**:
- `exchangeRecordId`: 兑换记录 ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "recipientName": "张三",
    "phoneNumber": "13800138000",
    "province": "广东省",
    "city": "深圳市",
    "detailAddress": "xx 街道 xx 小区 xx 栋 xx 单元 xxx 室"
  }
}
```

---

## 使用示例

### 完整的兑换流程

#### 步骤 1: 获取可兑换物品列表
```javascript
const response = await fetch('http://localhost:8082/api/exchange/items', {
  method: 'GET',
  headers: {
    'Authorization': 'Bearer ' + token
  }
});
const items = await response.json();
console.log(items);
```

#### 步骤 2: 执行兑换
```javascript
const response = await fetch('http://localhost:8082/api/exchange', {
  method: 'POST',
  headers: {
    'Authorization': 'Bearer ' + token,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    itemId: 1  // 兑换机械键盘
  })
});
const result = await response.json();
console.log(result);
// 返回兑换记录 ID
const exchangeRecordId = result.data.id;
```

#### 步骤 3: 填写收货地址
```javascript
const response = await fetch('http://localhost:8082/api/exchange/address', {
  method: 'POST',
  headers: {
    'Authorization': 'Bearer ' + token,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    exchangeRecordId: exchangeRecordId,
    recipientName: '张三',
    phoneNumber: '13800138000',
    province: '广东省',
    city: '深圳市',
    district: '南山区',
    detailAddress: 'xx 街道 xx 小区 xx 栋 xx 单元 xxx 室'
  })
});
const addressResult = await response.json();
console.log(addressResult);
```

#### 步骤 4: 查看兑换记录
```javascript
const response = await fetch('http://localhost:8082/api/exchange/records', {
  method: 'GET',
  headers: {
    'Authorization': 'Bearer ' + token
  }
});
const records = await response.json();
console.log(records);
```

---

## 业务规则

### 1. 库存管理
- 每日 0 点自动重置所有物品库存
- 使用定时任务 `@Scheduled(cron = "0 0 0 * * ?")` 执行
- 库存扣减和积分扣除在同一事务中完成

### 2. 兑换限制
- 每个用户每日最多兑换 3 次（可在代码中调整）
- 必须有足够的积分
- 物品必须在上架状态
- 库存必须充足

### 3. 状态流转
```
0 (待填写地址) → 1 (已填写地址) → 2 (已发货) → 3 (已完成)
                              ↓
                          -1 (已取消)
```

### 4. 数据一致性
- 使用 `@Transactional` 保证事务一致性
- 库存扣减和积分扣除要么同时成功，要么同时失败
- 兑换记录创建成功后才允许填写地址

---

## 测试数据

系统预置了以下测试物品：

| ID | 物品名称 | 所需积分 | 每日库存 |
|----|---------|---------|---------|
| 1 | 机械键盘 | 5000 | 10 |
| 2 | 无线鼠标 | 3000 | 20 |
| 3 | 显卡 RTX 4060 | 50000 | 2 |
| 4 | 智能手机 | 80000 | 1 |
| 5 | 耳机 | 2000 | 30 |
| 6 | 鼠标垫 | 500 | 50 |

---

## 注意事项

1. **Token 认证**: 所有接口都需要在 Header 中携带 JWT Token
2. **跨域配置**: 已配置 CORS，支持前端跨域访问
3. **并发控制**: 库存扣减使用了数据库行锁，防止超卖
4. **定时任务**: 需要确保 Spring Boot 应用持续运行以执行每日 0 点的库存重置
5. **索引优化**: 关键查询字段已添加索引（user_id, exchange_date, status）
