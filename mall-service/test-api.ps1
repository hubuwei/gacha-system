# 游戏商城后端 API 测试脚本
# 使用前请确保 mall-service 已启动在 http://localhost:8081

$API_BASE = "http://localhost:8081/api"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   游戏商城后端 API 测试" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 测试1: 获取所有游戏（含标签）
Write-Host "[测试1] 获取游戏列表..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$API_BASE/games/all-with-tags" -Method Get
    Write-Host "✓ 成功! 获取到 $($response.data.Count) 个游戏" -ForegroundColor Green
} catch {
    Write-Host "✗ 失败: $_" -ForegroundColor Red
}
Write-Host ""

# 测试2: 获取轮播图
Write-Host "[测试2] 获取轮播图..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$API_BASE/banners/active" -Method Get
    Write-Host "✓ 成功! 获取到 $($response.data.Count) 个轮播图" -ForegroundColor Green
} catch {
    Write-Host "✗ 失败: $_" -ForegroundColor Red
}
Write-Host ""

# 测试3: 获取游戏分类
Write-Host "[测试3] 获取游戏分类..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$API_BASE/games/categories" -Method Get
    Write-Host "✓ 成功! 获取到 $($response.data.Count) 个分类" -ForegroundColor Green
} catch {
    Write-Host "✗ 失败: $_" -ForegroundColor Red
}
Write-Host ""

# 测试4: 获取游戏标签
Write-Host "[测试4] 获取游戏标签..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$API_BASE/games/tags" -Method Get
    Write-Host "✓ 成功! 获取到 $($response.data.Count) 个标签" -ForegroundColor Green
} catch {
    Write-Host "✗ 失败: $_" -ForegroundColor Red
}
Write-Host ""

# 测试5: 获取用户钱包信息
Write-Host "[测试5] 获取用户钱包 (userId=1)..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$API_BASE/wallet/balance?userId=1" -Method Get
    if ($response.code -eq 200) {
        Write-Host "✓ 成功!" -ForegroundColor Green
        Write-Host "  余额: $($response.data.balance)" -ForegroundColor White
        Write-Host "  累计充值: $($response.data.totalRecharge)" -ForegroundColor White
        Write-Host "  累计消费: $($response.data.totalConsumed)" -ForegroundColor White
    } else {
        Write-Host "✗ 失败: $($response.message)" -ForegroundColor Red
    }
} catch {
    Write-Host "✗ 失败: $_" -ForegroundColor Red
}
Write-Host ""

# 测试6: 用户充值
Write-Host "[测试6] 用户充值 (userId=1, amount=100)..." -ForegroundColor Yellow
try {
    $body = @{
        userId = 1
        amount = 100.00
        paymentMethod = "alipay"
    } | ConvertTo-Json
    
    $response = Invoke-RestMethod -Uri "$API_BASE/wallet/recharge" -Method Post -Body $body -ContentType "application/json; charset=utf-8"
    if ($response.code -eq 200) {
        Write-Host "✓ 充值成功!" -ForegroundColor Green
        Write-Host "  充值金额: $($response.data.amount)" -ForegroundColor White
        Write-Host "  充值前余额: $($response.data.balanceBefore)" -ForegroundColor White
        Write-Host "  充值后余额: $($response.data.balanceAfter)" -ForegroundColor White
    } else {
        Write-Host "✗ 失败: $($response.message)" -ForegroundColor Red
    }
} catch {
    Write-Host "✗ 失败: $_" -ForegroundColor Red
}
Write-Host ""

# 测试7: 获取交易记录
Write-Host "[测试7] 获取交易记录 (userId=1)..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$API_BASE/wallet/transactions?userId=1" -Method Get
    Write-Host "✓ 成功! 获取到 $($response.data.Count) 条交易记录" -ForegroundColor Green
} catch {
    Write-Host "✗ 失败: $_" -ForegroundColor Red
}
Write-Host ""

# 测试8: 获取购物车
Write-Host "[测试8] 获取购物车 (userId=1)..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$API_BASE/cart?userId=1" -Method Get
    Write-Host "✓ 成功! 购物车中有 $($response.data.Count) 个商品" -ForegroundColor Green
} catch {
    Write-Host "✗ 失败: $_" -ForegroundColor Red
}
Write-Host ""

# 测试9: 获取订单列表
Write-Host "[测试9] 获取订单列表 (userId=1)..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$API_BASE/orders?userId=1" -Method Get
    Write-Host "✓ 成功! 获取到 $($response.data.Count) 个订单" -ForegroundColor Green
} catch {
    Write-Host "✗ 失败: $_" -ForegroundColor Red
}
Write-Host ""

# 测试10: 获取精选游戏
Write-Host "[测试10] 获取精选游戏..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$API_BASE/games/featured" -Method Get
    Write-Host "✓ 成功! 获取到 $($response.data.Count) 个精选游戏" -ForegroundColor Green
} catch {
    Write-Host "✗ 失败: $_" -ForegroundColor Red
}
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   测试完成!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
