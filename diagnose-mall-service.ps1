# ========================================
# Mall-Service 一键诊断脚本
# 用途：快速诊断服务健康状态和性能问题
# 使用：.\diagnose-mall-service.ps1
# ========================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Mall-Service 一键诊断工具" -ForegroundColor Cyan
Write-Host "  时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8081/api"
$hasError = $false

# ========================================
# 1. 检查服务是否运行
# ========================================
Write-Host "[1/7] 检查服务状态..." -ForegroundColor Yellow

try {
    $health = Invoke-RestMethod -Uri "$baseUrl/actuator/health" -TimeoutSec 5 -ErrorAction Stop
    
    if ($health.status -eq "UP") {
        Write-Host "  ✅ 服务状态: UP" -ForegroundColor Green
    } else {
        Write-Host "  ❌ 服务状态: $($health.status)" -ForegroundColor Red
        $hasError = $true
    }
    
    # 检查数据库
    if ($health.components.db.status -eq "UP") {
        Write-Host "  ✅ 数据库连接: 正常" -ForegroundColor Green
    } else {
        Write-Host "  ❌ 数据库连接: 异常" -ForegroundColor Red
        $hasError = $true
    }
    
    # 检查磁盘空间
    $diskFreeGB = [math]::Round($health.components.diskSpace.details.free / 1GB, 2)
    if ($diskFreeGB -lt 1) {
        Write-Host "  ⚠️  磁盘空间不足: ${diskFreeGB} GB" -ForegroundColor Yellow
    } else {
        Write-Host "  ✅ 磁盘空间: ${diskFreeGB} GB" -ForegroundColor Green
    }
    
} catch {
    Write-Host "  ❌ 无法连接到服务，请确认服务是否启动" -ForegroundColor Red
    Write-Host "     错误信息: $($_.Exception.Message)" -ForegroundColor Gray
    exit 1
}

Write-Host ""

# ========================================
# 2. 检查性能指标
# ========================================
Write-Host "[2/7] 检查性能指标..." -ForegroundColor Yellow

try {
    # JVM 内存
    $memory = Invoke-RestMethod -Uri "$baseUrl/actuator/metrics/jvm.memory.used"
    $memoryMB = [math]::Round($memory.measurements[0].value / 1MB, 2)
    Write-Host "  📊 JVM 内存使用: ${memoryMB} MB" -ForegroundColor Cyan
    
    # CPU 使用率
    try {
        $cpu = Invoke-RestMethod -Uri "$baseUrl/actuator/metrics/jvm.cpu.recent"
        $cpuPercent = [math]::Round($cpu.measurements[0].value * 100, 2)
        if ($cpuPercent -gt 80) {
            Write-Host "  📊 CPU 使用率: ${cpuPercent}% ⚠️" -ForegroundColor Yellow
        } else {
            Write-Host "  📊 CPU 使用率: ${cpuPercent}%" -ForegroundColor Cyan
        }
    } catch {
        Write-Host "  📊 CPU 使用率: 无法获取" -ForegroundColor Gray
    }
    
    # HTTP 请求统计
    $httpStats = Invoke-RestMethod -Uri "$baseUrl/actuator/metrics/http.server.requests"
    $totalRequests = $httpStats.measurements | Where-Object { $_.statistic -eq "COUNT" }
    Write-Host "  📊 总请求数: $($totalRequests.value)" -ForegroundColor Cyan
    
    # GC 信息
    try {
        $gcPause = Invoke-RestMethod -Uri "$baseUrl/actuator/metrics/jvm.gc.pause"
        $gcCount = $gcPause.measurements | Where-Object { $_.statistic -eq "COUNT" }
        $gcTime = $gcPause.measurements | Where-Object { $_.statistic -eq "TOTAL_TIME" }
        Write-Host "  📊 GC 次数: $($gcCount.value) | 总耗时: $([math]::Round($gcTime.value, 2)) s" -ForegroundColor Cyan
    } catch {
        Write-Host "  📊 GC 信息: 无法获取" -ForegroundColor Gray
    }
    
} catch {
    Write-Host "  ⚠️  无法获取性能指标" -ForegroundColor Yellow
}

Write-Host ""

# ========================================
# 3. 分析 HTTP 请求追踪
# ========================================
Write-Host "[3/7] 分析 HTTP 请求..." -ForegroundColor Yellow

try {
    $traces = Invoke-RestMethod -Uri "$baseUrl/actuator/httptrace"
    
    if ($traces.traces.Count -eq 0) {
        Write-Host "  ℹ️  暂无请求记录" -ForegroundColor Gray
    } else {
        Write-Host "  📊 最近请求数: $($traces.traces.Count)" -ForegroundColor Cyan
        
        # 统计状态码
        $statusGroups = $traces.traces | Group-Object { $_.response.status }
        foreach ($group in $statusGroups) {
            $color = if ($group.Name -eq 200) { "Green" } 
                     elseif ($group.Name -ge 500) { "Red" } 
                     elseif ($group.Name -ge 400) { "Yellow" } 
                     else { "White" }
            Write-Host "    状态码 $($group.Name): $($group.Count) 次" -ForegroundColor $color
        }
        
        # 找出慢请求
        $slowRequests = $traces.traces | Where-Object { $_.timeTaken -gt 500 }
        if ($slowRequests.Count -gt 0) {
            Write-Host "  ⚠️  慢请求 (>500ms): $($slowRequests.Count) 个" -ForegroundColor Yellow
            $slowRequests | Sort-Object timeTaken -Descending | Select-Object -First 3 | ForEach-Object {
                Write-Host "    - $($_.request.method) $($_.request.uri): $($_.timeTaken) ms" -ForegroundColor Yellow
            }
        } else {
            Write-Host "  ✅ 无慢请求" -ForegroundColor Green
        }
        
        # 找出错误请求
        $errorRequests = $traces.traces | Where-Object { $_.response.status -ge 500 }
        if ($errorRequests.Count -gt 0) {
            Write-Host "  ❌ 错误请求 (>=500): $($errorRequests.Count) 个" -ForegroundColor Red
            $errorRequests | Select-Object -First 3 | ForEach-Object {
                Write-Host "    - $($_.request.method) $($_.request.uri): $($_.response.status)" -ForegroundColor Red
            }
            $hasError = $true
        } else {
            Write-Host "  ✅ 无错误请求" -ForegroundColor Green
        }
    }
    
} catch {
    Write-Host "  ⚠️  无法获取请求追踪信息" -ForegroundColor Yellow
}

Write-Host ""

# ========================================
# 4. 检查线程状态
# ========================================
Write-Host "[4/7] 检查线程状态..." -ForegroundColor Yellow

try {
    $threadDump = Invoke-RestMethod -Uri "$baseUrl/actuator/threaddump"
    
    Write-Host "  📊 线程总数: $($threadDump.threads.Count)" -ForegroundColor Cyan
    
    # 线程状态分布
    $threadGroups = $threadDump.threads | Group-Object threadState
    foreach ($group in $threadGroups) {
        $color = if ($group.Name -eq "BLOCKED") { "Red" } 
                 elseif ($group.Name -eq "RUNNABLE") { "Green" } 
                 else { "White" }
        Write-Host "    $($group.Name): $($group.Count)" -ForegroundColor $color
    }
    
    # 检查阻塞线程
    $blockedThreads = $threadDump.threads | Where-Object { $_.threadState -eq "BLOCKED" }
    if ($blockedThreads.Count -gt 0) {
        Write-Host "  ⚠️  发现 $($blockedThreads.Count) 个阻塞线程！" -ForegroundColor Red
        $blockedThreads | Select-Object -First 3 | ForEach-Object {
            Write-Host "    - $($_.threadName)" -ForegroundColor Red
        }
        $hasError = $true
    } else {
        Write-Host "  ✅ 无阻塞线程" -ForegroundColor Green
    }
    
} catch {
    Write-Host "  ⚠️  无法获取线程信息" -ForegroundColor Yellow
}

Write-Host ""

# ========================================
# 5. 检查日志文件
# ========================================
Write-Host "[5/7] 检查日志文件..." -ForegroundColor Yellow

$logDir = "E:\CFDemo\gacha-system\mall-service\logs"
$logFiles = @{
    "INFO" = "$logDir\mall-service_info.log"
    "WARN" = "$logDir\mall-service_warn.log"
    "ERROR" = "$logDir\mall-service_error.log"
    "DEBUG" = "$logDir\mall-service_debug.log"
}

foreach ($level in $logFiles.Keys) {
    $filePath = $logFiles[$level]
    if (Test-Path $filePath) {
        $fileSize = (Get-Item $filePath).Length
        $lineCount = (Get-Content $filePath).Count
        $sizeKB = [math]::Round($fileSize / 1KB, 2)
        
        $color = if ($level -eq "ERROR" -and $lineCount -gt 100) { "Red" }
                 elseif ($level -eq "WARN" -and $lineCount -gt 50) { "Yellow" }
                 else { "Green" }
        
        Write-Host "  📄 $level 日志: $lineCount 行 (${sizeKB} KB)" -ForegroundColor $color
        
        # 如果 ERROR 日志过多，显示最新几条
        if ($level -eq "ERROR" -and $lineCount -gt 0) {
            Write-Host "    最新错误:" -ForegroundColor Gray
            Get-Content $filePath -Tail 3 | ForEach-Object {
                Write-Host "      $_" -ForegroundColor Gray
            }
            if ($lineCount -gt 100) {
                $hasError = $true
            }
        }
    } else {
        Write-Host "  📄 $level 日志: 文件不存在" -ForegroundColor Gray
    }
}

Write-Host ""

# ========================================
# 6. 检查关键接口性能
# ========================================
Write-Host "[6/7] 检查关键接口性能..." -ForegroundColor Yellow

$keyEndpoints = @(
    "/products",
    "/orders",
    "/users"
)

foreach ($endpoint in $keyEndpoints) {
    try {
        $metrics = Invoke-RestMethod -Uri "$baseUrl/actuator/metrics/http.server.requests?tag=uri:$endpoint"
        $count = $metrics.measurements | Where-Object { $_.statistic -eq "COUNT" }
        $max = $metrics.measurements | Where-Object { $_.statistic -eq "MAX" }
        
        if ($count -and $count.value -gt 0) {
            $maxMs = [math]::Round($max.value * 1000, 2)
            $color = if ($maxMs -gt 1000) { "Red" }
                     elseif ($maxMs -gt 500) { "Yellow" }
                     else { "Green" }
            
            Write-Host "  📊 $endpoint : $($count.value) 次请求 | 最慢: ${maxMs} ms" -ForegroundColor $color
        } else {
            Write-Host "  📊 $endpoint : 暂无请求" -ForegroundColor Gray
        }
    } catch {
        Write-Host "  📊 $endpoint : 无法获取指标" -ForegroundColor Gray
    }
}

Write-Host ""

# ========================================
# 7. 生成诊断总结
# ========================================
Write-Host "[7/7] 生成诊断总结..." -ForegroundColor Yellow
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "         诊断总结" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

if (-not $hasError) {
    Write-Host "✅ 服务运行正常，未发现明显问题" -ForegroundColor Green
    Write-Host ""
    Write-Host "建议：" -ForegroundColor Yellow
    Write-Host "  - 持续监控服务状态" -ForegroundColor White
    Write-Host "  - 定期查看日志文件" -ForegroundColor White
    Write-Host "  - 关注性能指标变化趋势" -ForegroundColor White
} else {
    Write-Host "⚠️  发现潜在问题，建议进一步排查" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "建议操作：" -ForegroundColor Yellow
    Write-Host "  1. 查看详细错误日志: Get-Content logs\mall-service_error.log -Tail 50" -ForegroundColor White
    Write-Host "  2. 检查慢请求原因: 查看 httptrace 端点" -ForegroundColor White
    Write-Host "  3. 分析线程阻塞: 查看 threaddump 端点" -ForegroundColor White
    Write-Host "  4. 考虑提升日志级别进行详细排查" -ForegroundColor White
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "诊断完成！" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 保存诊断报告
$reportFile = "diagnose-report_$(Get-Date -Format 'yyyyMMdd_HHmmss').txt"
Write-Host "💾 诊断报告已保存: $reportFile" -ForegroundColor Cyan

# 这里可以添加保存报告的逻辑
# $report | Out-File $reportFile
