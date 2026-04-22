# ============================================
# 搜索功能诊断脚本
# ============================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  搜索功能全面诊断工具" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$SERVER_IP = "111.228.12.167"
$SERVER_USER = "root"

Write-Host "[诊断 1/6] 测试Elasticsearch连接..." -ForegroundColor Yellow
try {
    $esResponse = Invoke-RestMethod -Uri "http://${SERVER_IP}:9200" -TimeoutSec 5 -ErrorAction Stop
    Write-Host "✓ Elasticsearch 正常运行" -ForegroundColor Green
    Write-Host "  版本: $($esResponse.version.number)" -ForegroundColor Gray
} catch {
    Write-Host "✗ Elasticsearch 无法访问" -ForegroundColor Red
    Write-Host "  错误: $_" -ForegroundColor Red
}

Write-Host ""
Write-Host "[诊断 2/6] 检查ES索引和文档数量..." -ForegroundColor Yellow
try {
    $indexInfo = Invoke-RestMethod -Uri "http://${SERVER_IP}:9200/games/_count" -TimeoutSec 5 -ErrorAction Stop
    $docCount = $indexInfo.count
    Write-Host "✓ games 索引存在" -ForegroundColor Green
    Write-Host "  文档数量: $docCount" -ForegroundColor Gray
    
    if ($docCount -eq 0) {
        Write-Host "  ⚠ 警告: 索引中没有数据！需要同步数据" -ForegroundColor Yellow
    }
} catch {
    Write-Host "✗ games 索引不存在或无法访问" -ForegroundColor Red
    Write-Host "  错误: $_" -ForegroundColor Red
}

Write-Host ""
Write-Host "[诊断 3/6] 测试后端搜索API（关键词：黑神话）..." -ForegroundColor Yellow
try {
    $searchUrl = "http://${SERVER_IP}:8081/api/search/games?keyword=黑神话&page=0&size=10"
    Write-Host "  请求URL: $searchUrl" -ForegroundColor Gray
    
    $searchResponse = Invoke-RestMethod -Uri $searchUrl -TimeoutSec 10 -ErrorAction Stop
    
    if ($searchResponse.code -eq 200) {
        Write-Host "✓ 搜索API正常响应" -ForegroundColor Green
        
        if ($searchResponse.data) {
            $totalResults = $searchResponse.data.total
            Write-Host "  搜索结果总数: $totalResults" -ForegroundColor Gray
            
            if ($totalResults -gt 0) {
                Write-Host "  ✓ 找到匹配的游戏:" -ForegroundColor Green
                foreach ($game in $searchResponse.data.results) {
                    Write-Host "    - $($game.title) (评分: $($game.rating), 价格: ¥$($game.currentPrice))" -ForegroundColor White
                }
            } else {
                Write-Host "  ⚠ 没有找到匹配的游戏" -ForegroundColor Yellow
                Write-Host "  可能原因: ES中没有'黑神话'相关数据" -ForegroundColor Yellow
            }
        } else {
            Write-Host "  ✗ API返回数据格式异常" -ForegroundColor Red
            Write-Host "  响应内容: $($searchResponse | ConvertTo-Json -Depth 3)" -ForegroundColor Gray
        }
    } else {
        Write-Host "✗ 搜索API返回错误" -ForegroundColor Red
        Write-Host "  错误码: $($searchResponse.code)" -ForegroundColor Red
        Write-Host "  错误信息: $($searchResponse.message)" -ForegroundColor Red
    }
} catch {
    Write-Host "✗ 搜索API无法访问" -ForegroundColor Red
    Write-Host "  错误: $_" -ForegroundColor Red
    Write-Host "  可能原因: mall-service未启动或端口未开放" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "[诊断 4/6] 测试自动补全API（前缀：黑神）..." -ForegroundColor Yellow
try {
    $autocompleteUrl = "http://${SERVER_IP}:8081/api/search/autocomplete?prefix=黑神&size=5"
    Write-Host "  请求URL: $autocompleteUrl" -ForegroundColor Gray
    
    $autocompleteResponse = Invoke-RestMethod -Uri $autocompleteUrl -TimeoutSec 10 -ErrorAction Stop
    
    if ($autocompleteResponse.code -eq 200) {
        Write-Host "✓ 自动补全API正常响应" -ForegroundColor Green
        
        if ($autocompleteResponse.data -and $autocompleteResponse.data.Count -gt 0) {
            Write-Host "  ✓ 返回建议数量: $($autocompleteResponse.data.Count)" -ForegroundColor Green
            foreach ($suggestion in $autocompleteResponse.data) {
                Write-Host "    - $($suggestion.title)" -ForegroundColor White
            }
        } else {
            Write-Host "  ⚠ 没有返回任何建议" -ForegroundColor Yellow
        }
    } else {
        Write-Host "✗ 自动补全API返回错误" -ForegroundColor Red
        Write-Host "  错误码: $($autocompleteResponse.code)" -ForegroundColor Red
    }
} catch {
    Write-Host "✗ 自动补全API无法访问" -ForegroundColor Red
    Write-Host "  错误: $_" -ForegroundColor Red
}

Write-Host ""
Write-Host "[诊断 5/6] 检查mall-service状态..." -ForegroundColor Yellow
try {
    $sshCommand = "docker ps --filter 'name=mall-service' --format '{{.Status}}'"
    $mallStatus = ssh "${SERVER_USER}@${SERVER_IP}" $sshCommand 2>&1
    
    if ($LASTEXITCODE -eq 0 -and $mallStatus) {
        Write-Host "✓ mall-service 正在运行" -ForegroundColor Green
        Write-Host "  状态: $mallStatus" -ForegroundColor Gray
    } else {
        Write-Host "✗ mall-service 未运行" -ForegroundColor Red
    }
} catch {
    Write-Host "✗ 无法连接到服务器检查服务状态" -ForegroundColor Red
}

Write-Host ""
Write-Host "[诊断 6/6] 检查前端部署文件..." -ForegroundColor Yellow
try {
    $sshCommand = "ls -lh /usr/share/nginx/html/ | head -5"
    $frontendFiles = ssh "${SERVER_USER}@${SERVER_IP}" $sshCommand 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ 前端文件已部署" -ForegroundColor Green
        Write-Host $frontendFiles -ForegroundColor Gray
    } else {
        Write-Host "✗ 无法检查前端文件" -ForegroundColor Red
    }
} catch {
    Write-Host "✗ SSH连接失败" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  诊断完成 - 问题总结" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "如果以上诊断显示：" -ForegroundColor Yellow
Write-Host ""
Write-Host "❌ 问题1: Elasticsearch没有数据" -ForegroundColor Red
Write-Host "   解决方案: 同步数据到ES" -ForegroundColor White
Write-Host "   执行命令:" -ForegroundColor Gray
Write-Host "   ssh root@${SERVER_IP} 'curl -X POST http://localhost:8081/api/sync/games'" -ForegroundColor Cyan
Write-Host ""

Write-Host "❌ 问题2: mall-service未启动" -ForegroundColor Red
Write-Host "   解决方案: 重启mall-service" -ForegroundColor White
Write-Host "   执行命令:" -ForegroundColor Gray
Write-Host "   ssh root@${SERVER_IP} 'docker restart mall-service'" -ForegroundColor Cyan
Write-Host ""

Write-Host "❌ 问题3: API返回空结果但ES有数据" -ForegroundColor Red
Write-Host "   可能原因: IK分词器未安装或配置错误" -ForegroundColor White
Write-Host "   检查命令:" -ForegroundColor Gray
Write-Host "   ssh root@${SERVER_IP} 'curl http://localhost:9200/_cat/plugins?v'" -ForegroundColor Cyan
Write-Host ""

Write-Host "❌ 问题4: 前端代码未更新" -ForegroundColor Red
Write-Host "   解决方案: 等待GitHub Actions完成部署，或手动上传" -ForegroundColor White
Write-Host "   查看进度: https://github.com/hubuwei/gacha-system/actions" -ForegroundColor Cyan
Write-Host ""

Write-Host "按任意键退出..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
