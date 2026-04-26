# Spring Boot Actuator 和日志系统自动化测试脚本
# 使用方法: .\test-actuator-and-logging.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Spring Boot Actuator & Logging 测试脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 定义服务配置
$services = @(
    @{Name="cms-service"; Port=8085; ContextPath=""},
    @{Name="mall-service"; Port=8081; ContextPath="/api"}
)

# 测试计数器
$totalTests = 0
$passedTests = 0
$failedTests = 0

# 辅助函数：测试 HTTP 端点
function Test-Endpoint {
    param(
        [string]$ServiceName,
        [string]$Url,
        [string]$Description
    )
    
    $script:totalTests++
    Write-Host "[$ServiceName] 测试: $Description" -NoNewline
    
    try {
        $response = Invoke-RestMethod -Uri $Url -Method Get -TimeoutSec 5 -ErrorAction Stop
        
        if ($response) {
            Write-Host " ✅ 通过" -ForegroundColor Green
            $script:passedTests++
            return $true
        } else {
            Write-Host " ❌ 失败 (空响应)" -ForegroundColor Red
            $script:failedTests++
            return $false
        }
    } catch {
        Write-Host " ❌ 失败 ($($_.Exception.Message))" -ForegroundColor Red
        $script:failedTests++
        return $false
    }
}

# 辅助函数：检查文件是否存在
function Test-LogFile {
    param(
        [string]$ServiceName,
        [string]$FilePath,
        [string]$Description
    )
    
    $script:totalTests++
    Write-Host "[$ServiceName] 测试: $Description" -NoNewline
    
    if (Test-Path $FilePath) {
        $fileSize = (Get-Item $FilePath).Length
        Write-Host " ✅ 通过 (大小: $([math]::Round($fileSize/1KB, 2)) KB)" -ForegroundColor Green
        $script:passedTests++
        return $true
    } else {
        Write-Host " ❌ 失败 (文件不存在)" -ForegroundColor Red
        $script:failedTests++
        return $false
    }
}

# 辅助函数：检查日志格式
function Test-LogFormat {
    param(
        [string]$ServiceName,
        [string]$FilePath
    )
    
    $script:totalTests++
    Write-Host "[$ServiceName] 测试: 日志格式验证" -NoNewline
    
    if (-not (Test-Path $FilePath)) {
        Write-Host " ⚠️  跳过 (文件不存在)" -ForegroundColor Yellow
        return $null
    }
    
    try {
        $lastLine = Get-Content $FilePath -Tail 1 -ErrorAction Stop
        
        # 检查是否包含时间戳、线程、级别、类名
        if ($lastLine -match '\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3} \[.+\] (INFO|DEBUG|WARN|ERROR) .+ - .+') {
            Write-Host " ✅ 通过" -ForegroundColor Green
            $script:passedTests++
            return $true
        } else {
            Write-Host " ❌ 失败 (格式不正确)" -ForegroundColor Red
            Write-Host "       实际内容: $lastLine" -ForegroundColor Gray
            $script:failedTests++
            return $false
        }
    } catch {
        Write-Host " ⚠️  跳过 (读取失败)" -ForegroundColor Yellow
        return $null
    }
}

Write-Host "第一步: 检查服务是否运行..." -ForegroundColor Yellow
Write-Host ""

foreach ($service in $services) {
    $port = $service.Port
    $name = $service.Name
    
    Write-Host "检查 $name (端口 $port)..." -NoNewline
    $isRunning = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
    
    if ($isRunning) {
        Write-Host " ✅ 运行中" -ForegroundColor Green
    } else {
        Write-Host " ❌ 未运行" -ForegroundColor Red
        Write-Host "   请先启动服务: cd $name && mvn spring-boot:run" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "第二步: 测试 Actuator 端点..." -ForegroundColor Yellow
Write-Host ""

foreach ($service in $services) {
    $port = $service.Port
    $name = $service.Name
    $contextPath = $service.ContextPath
    $baseUrl = "http://localhost:$port$contextPath"
    
    Write-Host "----------------------------------------" -ForegroundColor Cyan
    Write-Host "测试服务: $name ($baseUrl)" -ForegroundColor Cyan
    Write-Host "----------------------------------------" -ForegroundColor Cyan
    
    # 测试健康检查
    Test-Endpoint -ServiceName $name -Url "$baseUrl/actuator/health" -Description "健康检查端点"
    
    # 测试应用信息
    Test-Endpoint -ServiceName $name -Url "$baseUrl/actuator/info" -Description "应用信息端点"
    
    # 测试指标监控
    Test-Endpoint -ServiceName $name -Url "$baseUrl/actuator/metrics" -Description "指标监控端点"
    
    # 测试具体指标（JVM内存）
    Test-Endpoint -ServiceName $name -Url "$baseUrl/actuator/metrics/jvm.memory.used" -Description "JVM内存指标"
    
    # 测试HTTP追踪
    Test-Endpoint -ServiceName $name -Url "$baseUrl/actuator/httptrace" -Description "HTTP请求追踪"
    
    # 测试日志器
    Test-Endpoint -ServiceName $name -Url "$baseUrl/actuator/loggers" -Description "日志器端点"
    
    # 测试Beans
    Test-Endpoint -ServiceName $name -Url "$baseUrl/actuator/beans" -Description "Spring Beans列表"
    
    # 测试环境变量
    Test-Endpoint -ServiceName $name -Url "$baseUrl/actuator/env" -Description "环境变量端点"
    
    # 测试Prometheus
    Test-Endpoint -ServiceName $name -Url "$baseUrl/actuator/prometheus" -Description "Prometheus指标"
    
    # mall-service 特有端点
    if ($name -eq "mall-service") {
        Test-Endpoint -ServiceName $name -Url "$baseUrl/actuator/threaddump" -Description "线程快照端点"
        # heapdump 返回二进制文件，单独处理
        $script:totalTests++
        Write-Host "[$name] 测试: 堆内存快照端点" -NoNewline
        try {
            $response = Invoke-WebRequest -Uri "$baseUrl/actuator/heapdump" -Method Get -TimeoutSec 10 -ErrorAction Stop
            if ($response.StatusCode -eq 200) {
                Write-Host " ✅ 通过 (文件大小: $([math]::Round($response.Content.Length/1MB, 2)) MB)" -ForegroundColor Green
                $script:passedTests++
            } else {
                Write-Host " ❌ 失败 (状态码: $($response.StatusCode))" -ForegroundColor Red
                $script:failedTests++
            }
        } catch {
            Write-Host " ❌ 失败 ($($_.Exception.Message))" -ForegroundColor Red
            $script:failedTests++
        }
    }
    
    Write-Host ""
}

Write-Host "第三步: 检查日志文件..." -ForegroundColor Yellow
Write-Host ""

$logFiles = @(
    @{Service="cms-service"; Path="E:\CFDemo\gacha-system\cms-service\logs\cms-service_info.log"},
    @{Service="cms-service"; Path="E:\CFDemo\gacha-system\cms-service\logs\cms-service_warn.log"},
    @{Service="cms-service"; Path="E:\CFDemo\gacha-system\cms-service\logs\cms-service_error.log"},
    @{Service="mall-service"; Path="E:\CFDemo\gacha-system\mall-service\logs\mall-service_info.log"},
    @{Service="mall-service"; Path="E:\CFDemo\gacha-system\mall-service\logs\mall-service_warn.log"},
    @{Service="mall-service"; Path="E:\CFDemo\gacha-system\mall-service\logs\mall-service_error.log"}
)

foreach ($logFile in $logFiles) {
    Test-LogFile -ServiceName $logFile.Service -FilePath $logFile.Path -Description "日志文件存在性 ($($logFile.Path | Split-Path -Leaf))"
}

Write-Host ""
Write-Host "第四步: 验证日志格式..." -ForegroundColor Yellow
Write-Host ""

Test-LogFormat -ServiceName "cms-service" -FilePath "E:\CFDemo\gacha-system\cms-service\logs\cms-service_info.log"
Test-LogFormat -ServiceName "mall-service" -FilePath "E:\CFDemo\gacha-system\mall-service\logs\mall-service_info.log"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "测试总结" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "总测试数: $totalTests" -ForegroundColor White
Write-Host "通过数量: $passedTests" -ForegroundColor Green
Write-Host "失败数量: $failedTests" -ForegroundColor Red
Write-Host "通过率: $([math]::Round($passedTests/$totalTests*100, 2))%" -ForegroundColor $(if ($passedTests/$totalTests -ge 0.8) { "Green" } else { "Yellow" })
Write-Host ""

if ($failedTests -eq 0) {
    Write-Host "🎉 所有测试通过！Actuator 监控和日志系统配置成功！" -ForegroundColor Green
} else {
    Write-Host "⚠️  有 $failedTests 个测试失败，请检查上述错误信息。" -ForegroundColor Yellow
    Write-Host "   参考文档: ACTUATOR_AND_LOGGING_VERIFICATION.md" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "下一步操作:" -ForegroundColor Cyan
Write-Host "1. 查看详细文档: .\ACTUATOR_AND_LOGGING_VERIFICATION.md" -ForegroundColor White
Write-Host "2. 提交代码到 Git: git add . && git commit -m '集成Actuator监控和规范化日志系统'" -ForegroundColor White
Write-Host "3. 部署到服务器并验证生产环境配置" -ForegroundColor White
Write-Host ""
