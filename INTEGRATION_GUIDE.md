# 前后端联调说明文档

## 📋 服务列表与端口

| 服务名称 | 端口号 | 功能描述 | API 前缀 |
|---------|--------|---------|---------|
| auth-service | 8081 | 用户认证（登录、注册） | /api/auth |
| game-service | 8082 | 游戏功能（充值、签到、积分兑换） | /api/* |
| gacha-service | 8083 | 抽奖服务 | /api/gacha |

## 🚀 启动顺序

1. **启动 Redis**（如果 game-service 需要）
   ```powershell
   redis-server
   ```

2. **启动认证服务**
   ```powershell
   cd E:\CFDemo\gacha-system\auth-service
   java -jar target/auth-service-1.0.0-SNAPSHOT.jar
   ```

3. **启动游戏服务**
   ```powershell
   cd E:\CFDemo\gacha-system\game-service
   java -jar target/game-service-1.0.0-SNAPSHOT.jar
   ```

4. **启动抽奖服务**
   ```powershell
   cd E:\CFDemo\gacha-system\gacha-service
   java -jar target/gacha-service-1.0.0-SNAPSHOT.jar
   ```

## 🔗 API 接口列表

### 1. 认证服务 (8081)

#### 用户登录
- **URL**: `POST /api/auth/login`
- **请求体**:
  ```json
  {
    "username": "working2026",
    "password": "123456"
  }
  ```
- **响应**:
  ```json
  {
    "success": true,
    "data": {
      "user": {
        "id": 15,
        "username": "working2026"
      },
      "token": "eyJhbGciOiJIUzUxMiJ9..."
    }
  }
  ```

#### 用户注册
- **URL**: `POST /api/auth/register/body`
- **请求体**:
  ```json
  {
    "username": "newuser",
    "password": "123456",
    "confirmPassword": "123456"
  }
  ```

#### 获取用户信息
- **URL**: `GET /api/auth/info`
- **Header**: `Authorization: Bearer {token}`

#### 用户登出
- **URL**: `POST /api/auth/logout`
- **Header**: `Authorization: Bearer {token}`

### 2. 游戏服务 (8082)

#### 账户充值
- **URL**: `POST /api/recharge`
- **Header**: `Authorization: Bearer {token}`
- **请求体**:
  ```json
  {
    "userId": 15,
    "amount": 100,
    "rechargeMethod": "ADMIN_DIRECT"
  }
  ```

#### 每日签到
- **URL**: `POST /api/check-in`
- **Header**: `Authorization: Bearer {token}`
- **请求体**:
  ```json
  {
    "userId": 15
  }
  ```

#### 查询签到状态
- **URL**: `GET /api/check-in/status?userId={userId}`
- **Header**: `Authorization: Bearer {token}`

### 3. 抽奖服务 (8083)

#### 单次/多次抽奖
- **URL**: `POST /api/gacha/draw`
- **Header**: `Authorization: Bearer {token}`
- **请求体**:
  ```json
  {
    "userId": 15,
    "count": 1
  }
  ```

## 🧪 测试步骤

### 方法一：使用集成测试页面

1. 打开测试页面：`E:\CFDemo\gacha-system\integration-test.html`
2. 页面会自动检测所有服务的状态
3. 按顺序测试以下功能：
   - 用户注册
   - 用户登录
   - 账户充值
   - 每日签到
   - 幸运抽奖

### 方法二：使用 PowerShell 命令测试

#### 1. 测试登录
```powershell
$loginBody = @{ username = "working2026"; password = "123456" } | ConvertTo-Json
$loginResponse = Invoke-WebRequest -Uri "http://localhost:8081/api/auth/login" -Method POST -Body $loginBody -ContentType "application/json" -UseBasicParsing
$token = ($loginResponse.Content | ConvertFrom-Json).data.token
Write-Host "Token: $token"
```

#### 2. 测试充值
```powershell
$headers = @{ Authorization = "Bearer $token" }
$rechargeBody = @{ userId = 15; amount = 100; rechargeMethod = "ADMIN_DIRECT" } | ConvertTo-Json
Invoke-WebRequest -Uri "http://localhost:8082/api/recharge" -Method POST -Body $rechargeBody -ContentType "application/json" -Headers $headers -UseBasicParsing
```

#### 3. 测试签到
```powershell
$checkInBody = @{ userId = 15 } | ConvertTo-Json
Invoke-WebRequest -Uri "http://localhost:8082/api/check-in" -Method POST -Body $checkInBody -ContentType "application/json" -Headers $headers -UseBasicParsing
```

#### 4. 测试抽奖
```powershell
$gachaBody = @{ userId = 15; count = 1 } | ConvertTo-Json
Invoke-WebRequest -Uri "http://localhost:8083/api/gacha/draw" -Method POST -Body $gachaBody -ContentType "application/json" -Headers $headers -UseBasicParsing
```

## ⚠️ 常见问题

### 1. CORS 错误
如果遇到跨域问题，检查各服务的 CORS 配置：
- auth-service: `@CrossOrigin(origins = "*")`
- game-service: WebConfig 配置类
- gacha-service: `@CrossOrigin(origins = "*")`

### 2. Token 无效
确保使用最新的 token，并且 token 格式正确：
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

### 3. 服务无法连接
- 确认所有服务已启动
- 检查端口是否被占用
- 查看各服务的日志输出

### 4. JWT Secret 问题
如果登录时报 "signing key size" 错误，检查 application.yml 中的 jwt.secret 配置：
```yaml
jwt:
  secret: ThisIsAVeryLongAndSecureSecretKeyForJWTTokenGenerationWithHS512AlgorithmInGachaSystem2026
```

## 📊 数据流程

1. **用户注册流程**
   ```
   前端 → POST /api/auth/register/body → auth-service → 数据库
   ```

2. **用户登录流程**
   ```
   前端 → POST /api/auth/login → auth-service → 验证密码 → 生成 Token → 返回 Token
   ```

3. **充值流程**
   ```
   前端 → POST /api/recharge + Token → game-service → 验证 Token → 更新余额 → 返回结果
   ```

4. **签到流程**
   ```
   前端 → POST /api/check-in + Token → game-service → 验证签到状态 → 发放奖励 → 返回结果
   ```

5. **抽奖流程**
   ```
   前端 → POST /api/gacha/draw + Token → gacha-service → 验证积分 → 随机抽取 → 返回结果
   ```

## 🎯 联调成功标志

✅ 所有服务状态显示为绿色（在线）
✅ 能够成功注册新用户
✅ 能够使用注册的账号登录并获取 Token
✅ 能够使用 Token 访问需要认证的接口
✅ 充值功能正常，余额正确增加
✅ 签到功能正常，能够领取奖励
✅ 抽奖功能正常，能够消耗积分并获取奖品

## 📝 测试账号

可以使用以下测试账号：

- 用户名：`working2026`
- 密码：`123456`

或者在测试页面中注册新账号。

## 🔧 开发工具推荐

1. **浏览器开发者工具** - 查看网络请求和响应
2. **PowerShell** - 快速测试 API 接口
3. **集成测试页面** - 可视化测试所有功能
4. **后端日志** - 查看详细的请求处理过程

## 📖 下一步

完成基础功能联调后，可以进一步测试：
- 前端 gacha.html 页面的完整功能
- exchange.html 积分兑换功能
- checkin.html 签到页面功能
- 更多游戏特性和业务逻辑
