# CMS 前端空白页面诊断脚本

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  CMS Admin 空白页面诊断工具" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: 检查前端服务是否运行
Write-Host "[1/5] 检查前端服务状态..." -ForegroundColor Yellow
$frontendPort = netstat -ano | findstr ":5174.*LISTENING"
if ($frontendPort) {
    Write-Host "[OK] 前端服务正在运行 (端口 5174)" -ForegroundColor Green
} else {
    Write-Host "[ERROR] 前端服务未运行！" -ForegroundColor Red
    Write-Host "  请执行: cd cms-admin && npm run dev" -ForegroundColor Gray
    exit 1
}

# Step 2: 检查后端服务是否运行
Write-Host ""
Write-Host "[2/5] 检查后端服务状态..." -ForegroundColor Yellow
$backendPort = netstat -ano | findstr ":8085.*LISTENING"
if ($backendPort) {
    Write-Host "[OK] 后端服务正在运行 (端口 8085)" -ForegroundColor Green
} else {
    Write-Host "[WARN] 后端服务未运行！" -ForegroundColor Yellow
    Write-Host "  请执行: cd cms-service && mvn spring-boot:run" -ForegroundColor Gray
}

# Step 3: 检查 .env 配置
Write-Host ""
Write-Host "[3/5] 检查 .env 配置..." -ForegroundColor Yellow
$envContent = Get-Content "cms-admin\.env" -ErrorAction SilentlyContinue
if ($envContent) {
    $apiUrl = $envContent | Select-String "VITE_API_BASE_URL"
    if ($apiUrl) {
        Write-Host "[OK] API地址配置: $apiUrl" -ForegroundColor Green
        
        # 验证是否是正确的地址
        if ($apiUrl -match "8085") {
            Write-Host "[OK] API地址正确 (使用8085端口)" -ForegroundColor Green
        } else {
            Write-Host "[ERROR] API地址错误！应该是 http://localhost:8085/api/cms" -ForegroundColor Red
        }
    } else {
        Write-Host "[ERROR] 未找到 VITE_API_BASE_URL 配置" -ForegroundColor Red
    }
} else {
    Write-Host "[ERROR] .env 文件不存在" -ForegroundColor Red
}

# Step 4: 检查 request.js 默认配置
Write-Host ""
Write-Host "[4/5] 检查 request.js 默认配置..." -ForegroundColor Yellow
$requestFile = "cms-admin\src\utils\request.js"
if (Test-Path $requestFile) {
    $requestContent = Get-Content $requestFile -Raw
    if ($requestContent -match "localhost:8085") {
        Write-Host "[OK] request.js 默认地址正确 (8085)" -ForegroundColor Green
    } elseif ($requestContent -match "localhost:8084") {
        Write-Host "[ERROR] request.js 默认地址错误 (使用了8084)" -ForegroundColor Red
        Write-Host "  需要修改为: http://localhost:8085/api/cms" -ForegroundColor Gray
    } else {
        Write-Host "[WARN] 无法确定默认地址配置" -ForegroundColor Yellow
    }
} else {
    Write-Host "[ERROR] request.js 文件不存在" -ForegroundColor Red
}

# Step 5: 测试后端API连接
Write-Host ""
Write-Host "[5/5] 测试后端API连接..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8085/api/cms/auth/login" `
                                  -Method POST `
                                  -Body '{"username":"admin","password":"admin123"}' `
                                  -ContentType "application/json" `
                                  -TimeoutSec 5 `
                                  -ErrorAction Stop
    
    if ($response.code -eq 200) {
        Write-Host "[OK] 后端API连接正常" -ForegroundColor Green
        Write-Host "  登录成功，Token已获取" -ForegroundColor Gray
    } else {
        Write-Host "[WARN] API返回异常: $($response.message)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "[ERROR] 无法连接到后端API" -ForegroundColor Red
    Write-Host "  错误信息: $($_.Exception.Message)" -ForegroundColor Gray
    Write-Host ""
    Write-Host "  可能原因:" -ForegroundColor Yellow
    Write-Host "  1. 后端服务未启动" -ForegroundColor Gray
    Write-Host "  2. 端口号不正确" -ForegroundColor Gray
    Write-Host "  3. 防火墙阻止连接" -ForegroundColor Gray
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  诊断完成" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 提供解决方案
Write-Host "如果页面仍然空白，请尝试以下步骤：" -ForegroundColor Yellow
Write-Host ""
Write-Host "1. 清除浏览器缓存：" -ForegroundColor White
Write-Host "   - 按 Ctrl+Shift+Delete" -ForegroundColor Gray
Write-Host "   - 选择"缓存的图片和文件"" -ForegroundColor Gray
Write-Host "   - 点击"清除数据"" -ForegroundColor Gray
Write-Host ""
Write-Host "2. 使用无痕模式访问：" -ForegroundColor White
Write-Host "   - 按 Ctrl+Shift+N (Chrome)" -ForegroundColor Gray
Write-Host "   - 访问 http://localhost:5174" -ForegroundColor Gray
Write-Host ""
Write-Host "3. 检查浏览器控制台错误：" -ForegroundColor White
Write-Host "   - 按 F12 打开开发者工具" -ForegroundColor Gray
Write-Host "   - 查看 Console 标签页的错误信息" -ForegroundColor Gray
Write-Host "   - 查看 Network 标签页的请求状态" -ForegroundColor Gray
Write-Host ""
Write-Host "4. 重启前端服务：" -ForegroundColor White
Write-Host "   - 在终端按 Ctrl+C 停止服务" -ForegroundColor Gray
Write-Host "   - 重新执行: npm run dev" -ForegroundColor Gray
Write-Host ""
