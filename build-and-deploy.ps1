# ============================================
# 一键打包脚本
# ============================================

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  Gacha System 打包脚本" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

$ErrorActionPreference = "Stop"

# 项目根目录
$ProjectRoot = $PSScriptRoot

# 输出目录
$OutputDir = Join-Path $ProjectRoot "deploy-packages"
if (Test-Path $OutputDir) {
    Remove-Item $OutputDir -Recurse -Force
}
New-Item -ItemType Directory -Path $OutputDir | Out-Null

Write-Host "[1/5] 打包 common 模块..." -ForegroundColor Yellow
Set-Location (Join-Path $ProjectRoot "common")
mvn clean install -DskipTests
Write-Host "✓ common 打包完成" -ForegroundColor Green
Write-Host ""

Write-Host "[2/5] 打包 auth-service..." -ForegroundColor Yellow
Set-Location (Join-Path $ProjectRoot "auth-service")
mvn clean package -DskipTests
Copy-Item "target\*.jar" (Join-Path $OutputDir "auth-service.jar")
Write-Host "✓ auth-service 打包完成" -ForegroundColor Green
Write-Host ""

Write-Host "[3/5] 打包 mall-service..." -ForegroundColor Yellow
Set-Location (Join-Path $ProjectRoot "mall-service")
mvn clean package -DskipTests
Copy-Item "target\*.jar" (Join-Path $OutputDir "mall-service.jar")
Write-Host "✓ mall-service 打包完成" -ForegroundColor Green
Write-Host ""

Write-Host "[4/5] 打包 gacha-service..." -ForegroundColor Yellow
Set-Location (Join-Path $ProjectRoot "gacha-service")
mvn clean package -DskipTests
Copy-Item "target\*.jar" (Join-Path $OutputDir "gacha-service.jar")
Write-Host "✓ gacha-service 打包完成" -ForegroundColor Green
Write-Host ""

Write-Host "[5/5] 打包 game-service..." -ForegroundColor Yellow
Set-Location (Join-Path $ProjectRoot "game-service")
mvn clean package -DskipTests
Copy-Item "target\*.jar" (Join-Path $OutputDir "game-service.jar")
Write-Host "✓ game-service 打包完成" -ForegroundColor Green
Write-Host ""

# 打包前端
Write-Host "[额外] 打包前端..." -ForegroundColor Yellow
Set-Location (Join-Path $ProjectRoot "game-mall")
npm run build
Write-Host "✓ 前端打包完成" -ForegroundColor Green
Write-Host ""

Write-Host "==========================================" -ForegroundColor Green
Write-Host "  打包完成！" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Green
Write-Host ""
Write-Host "文件位置: $OutputDir" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Upload deploy-packages directory to server" -ForegroundColor White
Write-Host "2. Run deploy.sh script on server" -ForegroundColor White
Write-Host ""

Set-Location $ProjectRoot
