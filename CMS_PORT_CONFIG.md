# CMS 系统端口配置说明

## 📌 端口分配

| 服务 | 端口 | 说明 |
|------|------|------|
| auth-service | 8084 | 用户认证服务（已存在） |
| **cms-service** | **8085** | **CMS后端服务** |
| **cms-admin** | **5174** | **CMS前端应用** |

## 🔧 配置文件位置

### 1. CMS后端端口配置

**文件：** `cms-service/src/main/resources/application.yml`

```yaml
server:
  port: ${CMS_PORT:8085}  # 默认8085，可通过环境变量覆盖
```

### 2. CMS前端API地址配置

**文件：** `cms-admin/.env`

```env
VITE_API_BASE_URL=http://localhost:8085/api/cms
```

### 3. CMS前端开发服务器端口

**文件：** `cms-admin/vite.config.js`

```javascript
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5174,  // 前端开发服务器端口
    open: false,
  },
})
```

## 🚀 访问地址

启动成功后，访问以下地址：

- **CMS管理后台：** http://localhost:5174
- **CMS后端API：** http://localhost:8085/api/cms
- **用户认证服务：** http://localhost:8084/api/auth

## ⚠️ 端口冲突处理

### 如果8085端口被占用

**检查占用进程：**
```powershell
netstat -ano | findstr ":8085"
```

**终止进程：**
```powershell
taskkill /F /PID <进程ID>
```

**或者修改端口：**

1. 修改 `cms-service/src/main/resources/application.yml`：
   ```yaml
   server:
     port: 8086  # 改为其他可用端口
   ```

2. 修改 `cms-admin/.env`：
   ```env
   VITE_API_BASE_URL=http://localhost:8086/api/cms
   ```

3. 更新启动脚本中的端口号

### 如果5174端口被占用

Vite会自动尝试下一个可用端口（5175, 5176...），或在 `vite.config.js` 中指定其他端口。

## 🛠️ 快速停止服务

使用提供的停止脚本：

```powershell
.\stop-cms.ps1
```

该脚本会自动停止8085和5174端口的服务。

## 📝 注意事项

1. **不要修改auth-service的8084端口**，除非您确定没有其他服务依赖它
2. **CMS服务必须使用不同的端口**（8085），避免与auth-service冲突
3. **前端和后端端口都要保持一致**，修改后端端口后记得更新前端的 `.env` 文件
4. **防火墙设置**：确保8085和5174端口未被防火墙阻止

## 🔍 验证端口是否正常

**检查后端是否启动：**
```powershell
curl http://localhost:8085/api/cms/auth/login
```

**检查前端是否启动：**
浏览器访问 http://localhost:5174

---

**提示：** 如果遇到端口问题，请优先检查是否有旧的服务进程未关闭。
