# 游戏商城 API 测试脚本

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   游戏商城功能测试" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$MALL_API = "http://localhost:8081/api"
$AUTH_API = "http://localhost:8084/api/auth"

# 测试1: 检查服务
Write-Host "【测试1】检查后端服务..." -ForegroundColor Yellow
try {
    $gamesResponse = Invoke-RestMethod -Uri "$MALL_API/games/all-with-tags" -Method Get -TimeoutSec 5
    Write-Host "✅ 商城后端正常 (8081)" -ForegroundColor Green
    Write-Host "   游戏数量: $($gamesResponse.data.Count)" -ForegroundColor Gray
} catch {
    Write-Host "❌ 商城后端异常" -ForegroundColor Red
}

# 测试2: 用户登录
Write-Host ""
Write-Host "【测试2】用户登录..." -ForegroundColor Yellow
$loginBody = @{username="admin";password="admin123"} | ConvertTo-Json
try {
    $loginResponse = Invoke-RestMethod -Uri "$AUTH_API/login" -Method Post -Body $loginBody -ContentType "application/json"
    if ($loginResponse.code -eq 200) {
        $token = $loginResponse.data.token
        $userId = $loginResponse.data.id
        Write-Host "✅ 登录成功" -ForegroundColor Green
        Write-Host "   用户ID: $userId" -ForegroundColor Gray
        Write-Host "   余额: ¥$($loginResponse.data.balance)" -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ 登录失败: $_" -ForegroundColor Red
}

# 测试3: 浏览游戏
Write-Host ""
Write-Host "【测试3】浏览游戏列表..." -ForegroundColor Yellow
try {
    $games = (Invoke-RestMethod -Uri "$MALL_API/games/all-with-tags").data
    Write-Host "✅ 获取到 $($games.Count) 个游戏" -ForegroundColor Green
    if ($games.Count -gt 0) {
        $game = $games[0]
        Write-Host "   示例: $($game.title) - ¥$($game.currentPrice)" -ForegroundColor Gray
        $testGameId = $game.id
    }
} catch {
    Write-Host "❌ 获取游戏失败" -ForegroundColor Red
}

# 测试4: 添加到购物车
Write-Host ""
Write-Host "【测试4】添加到购物车..." -ForegroundColor Yellow
if ($testGameId -and $userId) {
    try {
        $url = "$MALL_API/cart?userId=$userId&gameId=$testGameId"
        Invoke-RestMethod -Uri $url -Method Post | Out-Null
        Write-Host "✅ 添加成功" -ForegroundColor Green
    } catch {
        Write-Host "⚠️  添加失败" -ForegroundColor Yellow
    }
}

# 测试5: 查看购物车
Write-Host ""
Write-Host "【测试5】查看购物车..." -ForegroundColor Yellow
if ($userId) {
    try {
        $cart = (Invoke-RestMethod -Uri "$MALL_API/cart?userId=$userId").data
        Write-Host "✅ 购物车有 $($cart.Count) 件商品" -ForegroundColor Green
    } catch {
        Write-Host "❌ 获取购物车失败" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   测试完成！" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "🌐 前端地址: http://localhost:5174" -ForegroundColor Green
Write-Host "💡 请在浏览器中访问并体验完整功能" -ForegroundColor Yellow
Write-Host ""
