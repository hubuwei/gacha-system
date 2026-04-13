# 测试订单自动取消功能

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   订单自动取消功能测试" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "功能说明:" -ForegroundColor Yellow
Write-Host "  - 定时任务每5分钟执行一次" -ForegroundColor Gray
Write-Host "  - 自动取消创建时间超过15分钟的待支付订单" -ForegroundColor Gray
Write-Host "  - 记录审计日志" -ForegroundColor Gray
Write-Host ""

Write-Host "测试步骤:" -ForegroundColor Yellow
Write-Host "  1. 创建一个待支付订单" -ForegroundColor Gray
Write-Host "  2. 等待15分钟以上" -ForegroundColor Gray
Write-Host "  3. 查看订单状态是否变为'已取消'" -ForegroundColor Gray
Write-Host "  4. 检查后端日志是否有自动取消的记录" -ForegroundColor Gray
Write-Host ""

Write-Host "快速测试方法（修改时间为测试）:" -ForegroundColor Yellow
Write-Host "  如果想立即测试，可以临时修改代码：" -ForegroundColor Gray
Write-Host "  OrderService.java 第318行" -ForegroundColor Gray
Write-Host "  将 minusMinutes(15) 改为 minusSeconds(30)" -ForegroundColor Gray
Write-Host "  然后等待30秒即可看到效果" -ForegroundColor Gray
Write-Host ""

Write-Host "查看日志:" -ForegroundColor Yellow
Write-Host "  mall-service/logs/mall-startup.log" -ForegroundColor Gray
Write-Host "  搜索关键字: '超时订单' 或 'ORDER_AUTO_CANCEL'" -ForegroundColor Gray
Write-Host ""

Write-Host "预期日志输出:" -ForegroundColor Green
Write-Host "  [INFO] 开始检查超时未支付订单..." -ForegroundColor White
Write-Host "  [INFO] 发现 X 个超时订单，开始取消..." -ForegroundColor White
Write-Host "  [INFO] 订单已自动取消: orderNo=ORD..., userId=X, createdAt=..." -ForegroundColor White
Write-Host "  [INFO] 超时订单检查完成，共取消 X 个订单" -ForegroundColor White
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "注意事项:" -ForegroundColor Yellow
Write-Host "  ✓ 确保 mall-service 正在运行" -ForegroundColor Gray
Write-Host "  ✓ 确保 @EnableScheduling 已启用" -ForegroundColor Gray
Write-Host "  ✓ 定时任务会在应用启动后自动运行" -ForegroundColor Gray
Write-Host "  ✓ 生产环境建议调整为更长的间隔（如10分钟）" -ForegroundColor Gray
Write-Host "========================================" -ForegroundColor Cyan
