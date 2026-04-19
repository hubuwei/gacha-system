# ============================================
# 中间件连接测试脚本
# 用于验证本地是否能连接到服务器中间件
# ============================================

$serverIP = "111.228.12.167"
$redisPort = 6379
$rabbitmqPort = 5672
$rabbitmqMgmtPort = 15672
$esPort = 9200

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  服务器中间件连接测试" -ForegroundColor Cyan
Write-Host "  服务器: $serverIP" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 测试 Redis
Write-Host "[1/3] 测试 Redis 连接..." -ForegroundColor Yellow
try {
    $tcpClient = New-Object System.Net.Sockets.TcpClient
    $connectTask = $tcpClient.ConnectAsync($serverIP, $redisPort)
    if ($connectTask.Wait(3000)) {
        Write-Host "  ✓ Redis 端口 $redisPort 可访问" -ForegroundColor Green
        $tcpClient.Close()
    } else {
        Write-Host "  ✗ Redis 连接超时" -ForegroundColor Red
        Write-Host "  提示: 检查服务器防火墙是否开放端口 $redisPort" -ForegroundColor Yellow
    }
} catch {
    Write-Host "  ✗ Redis 连接失败: $_" -ForegroundColor Red
}
Write-Host ""

# 测试 RabbitMQ
Write-Host "[2/3] 测试 RabbitMQ 连接..." -ForegroundColor Yellow
try {
    # 测试 AMQP 端口
    $tcpClient = New-Object System.Net.Sockets.TcpClient
    $connectTask = $tcpClient.ConnectAsync($serverIP, $rabbitmqPort)
    if ($connectTask.Wait(3000)) {
        Write-Host "  ✓ RabbitMQ AMQP 端口 $rabbitmqPort 可访问" -ForegroundColor Green
        $tcpClient.Close()
    } else {
        Write-Host "  ✗ RabbitMQ AMQP 连接超时" -ForegroundColor Red
    }
    
    # 测试管理界面端口
    $tcpClient2 = New-Object System.Net.Sockets.TcpClient
    $connectTask2 = $tcpClient2.ConnectAsync($serverIP, $rabbitmqMgmtPort)
    if ($connectTask2.Wait(3000)) {
        Write-Host "  ✓ RabbitMQ 管理界面端口 $rabbitmqMgmtPort 可访问" -ForegroundColor Green
        Write-Host "  管理界面: http://$serverIP`:$rabbitmqMgmtPort" -ForegroundColor Cyan
        $tcpClient2.Close()
    } else {
        Write-Host "  ⚠ RabbitMQ 管理界面端口 $rabbitmqMgmtPort 不可访问（可选）" -ForegroundColor Yellow
    }
} catch {
    Write-Host "  ✗ RabbitMQ 连接失败: $_" -ForegroundColor Red
}
Write-Host ""

# 测试 Elasticsearch
Write-Host "[3/3] 测试 Elasticsearch 连接..." -ForegroundColor Yellow
try {
    $tcpClient = New-Object System.Net.Sockets.TcpClient
    $connectTask = $tcpClient.ConnectAsync($serverIP, $esPort)
    if ($connectTask.Wait(3000)) {
        Write-Host "  ✓ Elasticsearch 端口 $esPort 可访问" -ForegroundColor Green
        $tcpClient.Close()
        
        # 尝试获取 ES 版本信息
        try {
            $response = Invoke-WebRequest -Uri "http://$serverIP`:$esPort" -TimeoutSec 5 -UseBasicParsing
            Write-Host "  ✓ Elasticsearch 服务正常响应" -ForegroundColor Green
        } catch {
            Write-Host "  ⚠ Elasticsearch 端口开放但 HTTP 响应异常" -ForegroundColor Yellow
        }
    } else {
        Write-Host "  ✗ Elasticsearch 连接超时" -ForegroundColor Red
        Write-Host "  提示: 检查服务器防火墙是否开放端口 $esPort" -ForegroundColor Yellow
    }
} catch {
    Write-Host "  ✗ Elasticsearch 连接失败: $_" -ForegroundColor Red
}
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  测试完成" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "如果所有测试通过，可以启动本地服务：" -ForegroundColor Green
Write-Host "  cd auth-service && mvn spring-boot:run" -ForegroundColor White
Write-Host "  cd game-service && mvn spring-boot:run" -ForegroundColor White
Write-Host "  cd gacha-service && mvn spring-boot:run" -ForegroundColor White
Write-Host "  cd mall-service && mvn spring-boot:run" -ForegroundColor White
Write-Host ""
