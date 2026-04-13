# 折扣通知邮件功能测试脚本
# 使用方法：.\test-discount-notification.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  折扣通知邮件功能测试" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$API_BASE = "http://localhost:8081/api"

# 测试配置
$GAME_ID = 1  # 要测试的游戏ID
$OLD_DISCOUNT = 20  # 原折扣率
$NEW_DISCOUNT = 50  # 新折扣率
$OLD_PRICE = 238.40  # 原价格
$NEW_PRICE = 149.00  # 新价格

Write-Host "📋 测试配置:" -ForegroundColor Yellow
Write-Host "  游戏ID: $GAME_ID" -ForegroundColor White
Write-Host "  原折扣: ${OLD_DISCOUNT}%" -ForegroundColor White
Write-Host "  新折扣: ${NEW_DISCOUNT}%" -ForegroundColor White
Write-Host "  原价格: ¥${OLD_PRICE}" -ForegroundColor White
Write-Host "  新价格: ¥${NEW_PRICE}" -ForegroundColor White
Write-Host ""

# 步骤1：检查游戏当前状态
Write-Host "🔍 步骤1: 查询游戏当前信息..." -ForegroundColor Green
try {
    $response = Invoke-RestMethod -Uri "$API_BASE/games/$GAME_ID" -Method Get
    Write-Host "  ✅ 游戏名称: $($response.data.title)" -ForegroundColor Green
    Write-Host "  ✅ 当前折扣: $($response.data.discountRate)%" -ForegroundColor Green
    Write-Host "  ✅ 当前价格: ¥$($response.data.currentPrice)" -ForegroundColor Green
} catch {
    Write-Host "  ❌ 查询失败: $_" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 步骤2：更新折扣（触发邮件通知）
Write-Host "📧 步骤2: 更新折扣并触发邮件通知..." -ForegroundColor Green
$body = @{
    discountRate = $NEW_DISCOUNT
    currentPrice = $NEW_PRICE
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod `
        -Uri "$API_BASE/admin/games/$GAME_ID/discount" `
        -Method Put `
        -Body $body `
        -ContentType "application/json"
    
    if ($response.code -eq 200) {
        Write-Host "  ✅ 折扣更新成功!" -ForegroundColor Green
        Write-Host "  ✅ 响应消息: $($response.message)" -ForegroundColor Green
    } else {
        Write-Host "  ❌ 更新失败: $($response.message)" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "  ❌ 请求失败: $_" -ForegroundColor Red
    Write-Host "  详细信息: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 步骤3：等待邮件发送（异步）
Write-Host "⏳ 步骤3: 等待邮件发送（5秒）..." -ForegroundColor Yellow
Start-Sleep -Seconds 5
Write-Host "  ✅ 等待完成" -ForegroundColor Green
Write-Host ""

# 步骤4：提示检查邮件
Write-Host "📬 步骤4: 请检查以下事项:" -ForegroundColor Cyan
Write-Host "  1️⃣  查看愿望单中该游戏的用户邮箱" -ForegroundColor White
Write-Host "  2️⃣  检查收件箱和垃圾邮件文件夹" -ForegroundColor White
Write-Host "  3️⃣  查看应用日志确认发送状态" -ForegroundColor White
Write-Host ""

# 步骤5：查看日志提示
Write-Host "📝 查看日志命令:" -ForegroundColor Cyan
Write-Host "  Get-Content mall-service\logs\mall-startup.log -Tail 50" -ForegroundColor Gray
Write-Host ""

# 步骤6：恢复原折扣（可选）
Write-Host "🔄 是否恢复原折扣？" -ForegroundColor Yellow
$restore = Read-Host "输入 Y 恢复，其他键跳过"
if ($restore -eq "Y" -or $restore -eq "y") {
    Write-Host "  正在恢复原折扣..." -ForegroundColor Yellow
    $restoreBody = @{
        discountRate = $OLD_DISCOUNT
        currentPrice = $OLD_PRICE
    } | ConvertTo-Json
    
    try {
        $response = Invoke-RestMethod `
            -Uri "$API_BASE/admin/games/$GAME_ID/discount" `
            -Method Put `
            -Body $restoreBody `
            -ContentType "application/json"
        
        if ($response.code -eq 200) {
            Write-Host "  ✅ 已恢复原折扣" -ForegroundColor Green
        } else {
            Write-Host "  ⚠️  恢复失败: $($response.message)" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "  ⚠️  恢复请求失败: $_" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  测试完成！" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
