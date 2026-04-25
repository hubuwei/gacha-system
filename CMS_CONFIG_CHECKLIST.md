# CMS 前后端配置一致性检查清单

## ✅ 当前配置状态

### 后端配置 (cms-service)

**文件：** `cms-service/src/main/resources/application.yml`

```yaml
server:
  port: 8085  # ✅ 后端端口

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/gacha_system_dev
    username: root
    password: 123456
    
jwt:
  secret: your-secret-key-change-in-production-for-security
  expiration: 86400000
```

**API 基础路径：** `/api/cms`  
（由 Controller 的 `@RequestMapping("/api/cms")` 定义）

**完整 API 地址：** `http://localhost:8085/api/cms`

---

### 前端配置 (cms-admin)

**文件：** `cms-admin/.env`

```env
VITE_API_BASE_URL=http://localhost:8085/api/cms  # ✅ 前端API地址
```

**文件：** `cms-admin/vite.config.js`

```javascript
export default defineConfig({
  server: {
    port: 5174,  # ✅ 前端开发服务器端口
  },
})
```

**前端访问地址：** `http://localhost:5174`

---

## 🔍 配置一致性验证

### ✅ 已验证的配置项

| 配置项 | 后端值 | 前端值 | 状态 |
|--------|--------|--------|------|
| 后端端口 | 8085 | 8085 (在 .env 中) | ✅ 一致 |
| API 路径 | /api/cms | /api/cms (在 .env 中) | ✅ 一致 |
| 数据库 | gacha_system_dev | - | ✅ 正确 |
| JWT Secret | your-secret-key... | - | ✅ 默认值 |

### 📋 完整 API 端点映射

前端调用示例：
```javascript
// 登录 API
POST http://localhost:8085/api/cms/auth/login

// 获取管理员信息
GET http://localhost:8085/api/cms/auth/info
```

前端代码中的使用：
```javascript
// src/api/auth.js
import request from '../utils/request';

export const login = (data) => {
  return request({
    url: '/auth/login',  // 自动拼接为 /api/cms/auth/login
    method: 'post',
    data,
  });
};
```

Axios 配置（`src/utils/request.js`）：
```javascript
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8085/api/cms',
  timeout: 30000,
});
```

---

## 🚀 启动验证步骤

### Step 1: 验证后端是否正常运行

```powershell
# 检查端口 8085 是否监听
netstat -ano | findstr ":8085"
```

应该看到类似输出：
```
TCP    0.0.0.0:8085           0.0.0.0:0              LISTENING       43064
```

### Step 2: 测试后端 API

```powershell
# 测试登录接口
curl -X POST http://localhost:8085/api/cms/auth/login `
  -H "Content-Type: application/json" `
  -d '{"username":"admin","password":"admin123"}'
```

应该返回：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "adminId": 1,
    "username": "admin",
    ...
  }
}
```

### Step 3: 验证前端是否能连接后端

1. 启动前端：
   ```bash
   cd cms-admin
   npm run dev
   ```

2. 浏览器打开：http://localhost:5174

3. 打开浏览器开发者工具（F12）→ Network 标签

4. 尝试登录，观察网络请求：
   - Request URL: `http://localhost:8085/api/cms/auth/login`
   - Status: 200
   - Response: 包含 token

---

## ⚠️ 常见配置问题

### 问题1：前端无法连接后端

**症状：**
- 浏览器控制台显示 `Network Error` 或 `CORS error`
- Network 标签中请求状态为 `(failed)`

**检查清单：**
1. ✅ 后端是否在运行？（检查端口 8085）
2. ✅ `.env` 文件中的 API 地址是否正确？
3. ✅ Controller 是否有 `@CrossOrigin(origins = "*")` 注解？

**解决方案：**
确保 `AdminAuthController.java` 有跨域配置：
```java
@RestController
@RequestMapping("/api/cms/auth")
@CrossOrigin(origins = "*")  // ← 必须有这个
public class AdminAuthController {
    // ...
}
```

### 问题2：404 Not Found

**症状：**
- 请求返回 404 错误

**可能原因：**
1. API 路径拼写错误
2. 后端未启动
3. 端口号不匹配

**检查：**
```powershell
# 确认后端监听的端口
netstat -ano | findstr ":8085"

# 确认前端配置的 API 地址
Get-Content cms-admin\.env
```

### 问题3：JWT Token 无效

**症状：**
- 登录后访问其他接口返回 401 Unauthorized

**检查清单：**
1. ✅ Token 是否正确保存到 localStorage？
2. ✅ 请求头是否包含 `Authorization: Bearer <token>`？
3. ✅ 后端 JWT Secret 是否与生成时一致？

**前端代码检查（`src/utils/request.js`）：**
```javascript
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('admin_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;  // ← 必须有
    }
    return config;
  },
  // ...
);
```

---

## 📝 配置文件位置汇总

| 文件 | 作用 | 关键配置 |
|------|------|----------|
| `cms-service/src/main/resources/application.yml` | 后端配置 | server.port=8085 |
| `cms-admin/.env` | 前端环境变量 | VITE_API_BASE_URL |
| `cms-admin/vite.config.js` | 前端构建配置 | server.port=5174 |
| `cms-admin/src/utils/request.js` | Axios 封装 | baseURL, interceptors |
| `cms-service/pom.xml` | Maven 依赖 | spring-boot.version |

---

## 🔧 修改配置的步骤

### 如果需要修改后端端口

**Step 1:** 修改 `application.yml`
```yaml
server:
  port: 8086  # 改为新端口
```

**Step 2:** 修改前端 `.env`
```env
VITE_API_BASE_URL=http://localhost:8086/api/cms
```

**Step 3:** 重启后端和前端

### 如果需要修改前端端口

**Step 1:** 修改 `vite.config.js`
```javascript
export default defineConfig({
  server: {
    port: 5175,  # 改为新端口
  },
})
```

**Step 2:** 重启前端

---

## ✅ 最终验证清单

在开始开发前，请确认以下所有项都为 ✅：

- [ ] 后端服务在端口 8085 上运行
- [ ] 前端服务在端口 5174 上运行
- [ ] 数据库 `gacha_system_dev` 可连接
- [ ] 管理员账号存在（admin/admin123）
- [ ] 前端 `.env` 中的 API 地址正确
- [ ] 浏览器可以访问 http://localhost:5174
- [ ] 登录功能正常工作
- [ ] Network 标签中无 CORS 错误
- [ ] Token 正确保存和使用

---

## 🎯 快速诊断命令

```powershell
# 1. 检查后端端口
netstat -ano | findstr ":8085"

# 2. 检查前端端口
netstat -ano | findstr ":5174"

# 3. 测试后端 API
curl http://localhost:8085/api/cms/auth/login -Method POST -Body '{"username":"admin","password":"admin123"}' -ContentType "application/json"

# 4. 查看前端配置
Get-Content cms-admin\.env

# 5. 查看后端日志
Get-Content cms-service\logs\cms-service.log -Tail 50
```

---

**提示：** 如果修改了任何配置，记得重启对应的服务使配置生效！
