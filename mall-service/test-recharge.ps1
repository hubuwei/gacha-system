# 充值功能测试脚本

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   充值功能测试" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 测试参数
$userId = 1
$amount = 50.00
$paymentMethod = "balance"
$url = "http://localhost:8081/api/wallet/recharge"

Write-Host "测试参数:" -ForegroundColor Yellow
Write-Host "  URL: $url" -ForegroundColor Gray
Write-Host "  用户ID: $userId" -ForegroundColor Gray
Write-Host "  金额: ￥$amount" -ForegroundColor Gray
Write-Host "  支付方式: $paymentMethod" -ForegroundColor Gray
Write-Host ""

# 构建请求体
$body = @{
    userId = $userId
    amount = $amount
    paymentMethod = $paymentMethod
} | ConvertTo-Json

Write-Host "请求体:" -ForegroundColor Yellow
Write-Host $body -ForegroundColor Gray
Write-Host ""

Write-Host "发送请求..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri $url -Method Post -Body $body -ContentType "application/json; charset=utf-8"
    
    Write-Host ""
    Write-Host "响应结果:" -ForegroundColor Green
    Write-Host ($response | ConvertTo-Json -Depth 10) -ForegroundColor White
    
    if ($response.code -eq 200) {
        Write-Host ""
        Write-Host "✓ 充值成功！" -ForegroundColor Green
        Write-Host "  交易ID: $($response.data.transactionId)" -ForegroundColor Gray
        Write-Host "  充值金额: ￥$($response.data.amount)" -ForegroundColor Gray
        Write-Host "  充值前余额: ￥$($response.data.balanceBefore)" -ForegroundColor Gray
        Write-Host "  充值后余额: ￥$($response.data.balanceAfter)" -ForegroundColor Gray
    } else {
        Write-Host ""
        Write-Host "✗ 充值失败" -ForegroundColor Red
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
Write-Host "测试完成" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
