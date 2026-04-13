# ============================================
# Docker 一键部署脚本 (Windows PowerShell)
# 使用方法: .\docker-deploy.ps1
# ============================================

$ErrorActionPreference = "Stop"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "游戏商城系统 - Docker 部署" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# 检查Docker是否安装
try {
    docker --version | Out-Null
    Write-Host "✓ Docker环境检查通过" -ForegroundColor Green
} catch {
    Write-Host "错误: Docker未安装" -ForegroundColor Red
    Write-Host "请先安装Docker Desktop: https://www.docker.com/products/docker-desktop"
    exit 1
}

# 检查.env文件
if (-not (Test-Path ".env")) {
    Write-Host "⚠ .env文件不存在，从模板创建..." -ForegroundColor Yellow
    Copy-Item ".env.docker" ".env"
    Write-Host "请编辑 .env 文件，修改密码等配置" -ForegroundColor Red
    Write-Host ""
    Read-Host "按回车键继续..."
}

# 解析命令行参数
$action = if ($args.Count -gt 0) { $args[0] } else { "up" }

switch ($action) {
    "up" {
        Write-Host "步骤 1/4: 构建镜像..." -ForegroundColor Blue
        docker compose build --no-cache
        
        Write-Host ""
        Write-Host "步骤 2/4: 启动服务..." -ForegroundColor Blue
        docker compose up -d
        
        Write-Host ""
        Write-Host "步骤 3/4: 等待服务启动..." -ForegroundColor Blue
        Start-Sleep -Seconds 10
        
        Write-Host ""
        Write-Host "步骤 4/4: 检查服务状态..." -ForegroundColor Blue
        docker compose ps
        
        Write-Host ""
        Write-Host "==========================================" -ForegroundColor Green
        Write-Host "部署完成！" -ForegroundColor Green
        Write-Host "==========================================" -ForegroundColor Green
        Write-Host ""
        Write-Host "访问地址:" -ForegroundColor White
        Write-Host "  前端: http://localhost" -ForegroundColor Green
        Write-Host "  API:  http://localhost:8081/api" -ForegroundColor Green
        Write-Host "  RabbitMQ管理: http://localhost:15672" -ForegroundColor Green
        Write-Host ""
        Write-Host "常用命令:" -ForegroundColor White
        Write-Host "  查看日志: docker compose logs -f [service-name]"
        Write-Host "  停止服务: docker compose down"
        Write-Host "  重启服务: docker compose restart [service-name]"
        Write-Host "  查看状态: docker compose ps"
        Write-Host ""
    }
    
    "down" {
        Write-Host "停止并删除所有容器..." -ForegroundColor Yellow
        docker compose down
        Write-Host "✓ 服务已停止" -ForegroundColor Green
    }
    
    "restart" {
        Write-Host "重启所有服务..." -ForegroundColor Yellow
        docker compose restart
        Write-Host "✓ 服务已重启" -ForegroundColor Green
    }
    
    "logs" {
        $service = if ($args.Count -gt 1) { $args[1] } else { "" }
        if ($service) {
            Write-Host "查看 $service 服务日志..." -ForegroundColor Yellow
            docker compose logs -f $service
        } else {
            Write-Host "查看所有服务日志 (Ctrl+C退出)..." -ForegroundColor Yellow
            docker compose logs -f
        }
    }
    
    "status" {
        Write-Host "服务状态:" -ForegroundColor Blue
        docker compose ps
    }
    
    "rebuild" {
        Write-Host "重新构建并启动..." -ForegroundColor Yellow
        docker compose down
        docker compose build --no-cache
        docker compose up -d
        Write-Host "✓ 重建完成" -ForegroundColor Green
    }
    
    default {
        Write-Host "用法: .\docker-deploy.ps1 [up|down|restart|logs|status|rebuild]" -ForegroundColor White
        Write-Host ""
        Write-Host "命令说明:" -ForegroundColor White
        Write-Host "  up      - 构建并启动所有服务（默认）"
        Write-Host "  down    - 停止并删除所有容器"
        Write-Host "  restart - 重启所有服务"
        Write-Host "  logs    - 查看日志 (可指定服务名)"
        Write-Host "  status  - 查看服务状态"
        Write-Host "  rebuild - 重新构建并启动"
        exit 1
    }
}
