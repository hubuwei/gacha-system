# 测试钱包余额接口

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   测试钱包余额接口" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$userId = 1
$url = "http://localhost:8081/api/wallet/balance?userId=$userId"

Write-Host "测试参数:" -ForegroundColor Yellow
Write-Host "  URL: $url" -ForegroundColor Gray
Write-Host ""

try {
    Write-Host "发送请求..." -ForegroundColor Yellow
    $response = Invoke-RestMethod -Uri $url -Method Get
    
    Write-Host ""
    Write-Host "响应结果:" -ForegroundColor Green
    Write-Host ($response | ConvertTo-Json -Depth 10) -ForegroundColor White
    
    if ($response.code -eq 200) {
        Write-Host ""
        Write-Host "✓ 获取成功！" -ForegroundColor Green
        Write-Host "  用户ID: $($response.data.userId)" -ForegroundColor Gray
        Write-Host "  余额: ￥$($response.data.balance)" -ForegroundColor Gray
        Write-Host "  冻结余额: ￥$($response.data.frozenBalance)" -ForegroundColor Gray
        Write-Host "  累计充值: ￥$($response.data.totalRecharge)" -ForegroundColor Gray
        Write-Host "  累计消费: ￥$($response.data.totalConsumed)" -ForegroundColor Gray
    } else {
        Write-Host ""
        Write-Host "✗ 获取失败" -ForegroundColor Red
        Write-Host "  错误信息: $($response.message)" -ForegroundColor Gray
    }
} catch {
    Write-Host ""
    Write-Host "✗ 请求失败" -ForegroundColor Red
    Write-Host "  错误: $($_.Exception.Message)" -ForegroundColor Gray
    
    if ($_.ErrorDetails) {
        Write-Host "  响应内容: $($_.ErrorDetails.Message)" -ForegroundColor Gray
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
