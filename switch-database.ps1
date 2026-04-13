# ============================================
# 数据库环境切换脚本 - Windows PowerShell
# 用途：快速切换开发和生产数据库配置
# ============================================

param(
    [Parameter(Mandatory=$true)]
    [ValidateSet("dev", "prod", "demo")]
    [string]$Environment,
    
    [string]$MySQLUser = "root",
    [string]$MySQLPassword = ""
)

# 颜色输出函数
function Write-ColorOutput {
    param([string]$Message, [string]$Color = "White")
    Write-Host $Message -ForegroundColor $Color
}

# 获取当前目录
$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$DatabaseDir = Join-Path $ProjectRoot "database"

Write-ColorOutput "`n========================================" "Cyan"
Write-ColorOutput "  游戏商城 - 数据库环境切换工具" "Cyan"
Write-ColorOutput "========================================`n" "Cyan"

# 根据环境选择配置
switch ($Environment) {
    "dev" {
        $DbName = "gacha_system_dev"
        $InitScript = "init-dev.sql"
        $Description = "开发环境（包含测试数据）"
    }
    "prod" {
        $DbName = "gacha_system_prod"
        $InitScript = "schema-production.sql"
        $Description = "生产环境（仅表结构）"
    }
    "demo" {
        $DbName = "gacha_system_demo"
        $InitScript = "init-dev.sql"
        $Description = "演示环境（包含示例数据）"
    }
}

Write-ColorOutput "环境: $Environment" "Yellow"
Write-ColorOutput "数据库: $DbName" "Yellow"
Write-ColorOutput "说明: $Description`n" "Yellow"

# 确认操作
$confirm = Read-Host "是否继续？(y/n)"
if ($confirm -ne "y" -and $confirm -ne "Y") {
    Write-ColorOutput "操作已取消" "Red"
    exit
}

# 检查 MySQL 是否可用
Write-ColorOutput "`n检查 MySQL 连接..." "Cyan"
try {
    $mysqlCheck = mysql --version 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "MySQL 未安装或未添加到 PATH"
    }
    Write-ColorOutput "✓ MySQL 已安装: $mysqlCheck" "Green"
} catch {
    Write-ColorOutput "✗ 错误: $_" "Red"
    Write-ColorOutput "请确保 MySQL 已安装并添加到系统 PATH" "Red"
    exit 1
}

# 创建数据库
Write-ColorOutput "`n创建数据库: $DbName" "Cyan"
$createDbCmd = "CREATE DATABASE IF NOT EXISTS \`"$DbName\`" DEFAULT CHARACTER SET utf8mb4 DEFAULT COLLATE utf8mb4_unicode_ci;"

if ($MySQLPassword) {
    mysql -u $MySQLUser -p$MySQLPassword -e $createDbCmd
} else {
    mysql -u $MySQLUser -e $createDbCmd
}

if ($LASTEXITCODE -eq 0) {
    Write-ColorOutput "✓ 数据库创建成功" "Green"
} else {
    Write-ColorOutput "✗ 数据库创建失败" "Red"
    exit 1
}

# 执行初始化脚本
Write-ColorOutput "`n执行初始化脚本: $InitScript" "Cyan"
$scriptPath = Join-Path $DatabaseDir $InitScript

if (-not (Test-Path $scriptPath)) {
    Write-ColorOutput "✗ 错误: 脚本文件不存在: $scriptPath" "Red"
    exit 1
}

if ($MySQLPassword) {
    mysql -u $MySQLUser -p$MySQLPassword $DbName < $scriptPath
} else {
    mysql -u $MySQLUser $DbName < $scriptPath
}

if ($LASTEXITCODE -eq 0) {
    Write-ColorOutput "✓ 初始化脚本执行成功" "Green"
} else {
    Write-ColorOutput "✗ 初始化脚本执行失败" "Red"
    exit 1
}

# 更新各服务的 .env 文件
Write-ColorOutput "`n更新服务配置文件..." "Cyan"

$services = @("auth-service", "game-service", "gacha-service", "mall-service")
foreach ($service in $services) {
    $envFile = Join-Path $ProjectRoot "$service\.env"
    $envExample = Join-Path $ProjectRoot "$service\.env.example"
    
    if (Test-Path $envFile) {
        # 读取现有 .env 文件
        $content = Get-Content $envFile -Raw
        
        # 替换数据库名称
        $content = $content -replace "DB_NAME=.*", "DB_NAME=$DbName"
        
        # 根据环境调整其他配置
        if ($Environment -eq "prod") {
            $content = $content -replace "JPA_DDL_AUTO=.*", "JPA_DDL_AUTO=validate"
            $content = $content -replace "JPA_SHOW_SQL=.*", "JPA_SHOW_SQL=false"
        } else {
            $content = $content -replace "JPA_DDL_AUTO=.*", "JPA_DDL_AUTO=update"
            $content = $content -replace "JPA_SHOW_SQL=.*", "JPA_SHOW_SQL=true"
        }
        
        # 写回文件
        Set-Content -Path $envFile -Value $content -NoNewline
        Write-ColorOutput "  ✓ 已更新: $service\.env" "Green"
    } elseif (Test-Path $envExample) {
        # 如果 .env 不存在，从 .env.example 复制
        Copy-Item $envExample $envFile
        
        # 修改复制的文件
        $content = Get-Content $envFile -Raw
        $content = $content -replace "DB_NAME=.*", "DB_NAME=$DbName"
        
        if ($Environment -eq "prod") {
            $content = $content -replace "JPA_DDL_AUTO=.*", "JPA_DDL_AUTO=validate"
            $content = $content -replace "JPA_SHOW_SQL=.*", "JPA_SHOW_SQL=false"
        } else {
            $content = $content -replace "JPA_DDL_AUTO=.*", "JPA_DDL_AUTO=update"
            $content = $content -replace "JPA_SHOW_SQL=.*", "JPA_SHOW_SQL=true"
        }
        
        Set-Content -Path $envFile -Value $content -NoNewline
        Write-ColorOutput "  ✓ 已创建: $service\.env" "Green"
    } else {
        Write-ColorOutput "  ⚠ 未找到: $service\.env.example" "Yellow"
    }
}

# 显示测试账号信息
if ($Environment -eq "dev" -or $Environment -eq "demo") {
    Write-ColorOutput "`n========================================" "Cyan"
    Write-ColorOutput "  测试账号信息" "Cyan"
    Write-ColorOutput "========================================" "Cyan"
    Write-ColorOutput "管理员: admin / admin123" "White"
    Write-ColorOutput "用户1:  testuser1 / user123" "White"
    Write-ColorOutput "用户2:  testuser2 / user123" "White"
    Write-ColorOutput "========================================`n" "Cyan"
}

Write-ColorOutput "✓ 环境切换完成！" "Green"
Write-ColorOutput "数据库: $DbName" "Green"
Write-ColorOutput "环境: $Environment`n" "Green"

Write-ColorOutput "下一步：" "Yellow"
Write-ColorOutput "1. 检查各服务的 .env 配置是否正确" "White"
Write-ColorOutput "2. 启动各个服务: mvn spring-boot:run" "White"
Write-ColorOutput "3. 访问前端应用进行测试`n" "White"
