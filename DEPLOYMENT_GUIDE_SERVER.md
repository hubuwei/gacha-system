# 服务器部署完整指南

## 问题说明
1. auth-service已更新并重启（包含搜索接口）
2. Nginx配置需要修复（auth-service代理路径重复问题）
3. 需要构建并部署前端生产版本（game-mall和cms-admin）

## 步骤1：修复Nginx配置

在服务器上执行以下命令：

```bash
ssh root@111.228.12.167

# 修复auth-service代理配置
docker exec gacha-frontend sed -i 's|proxy_pass http://auth_service/;|proxy_pass http://auth_service/api/auth/;|' /etc/nginx/conf.d/default.conf

# 重新加载Nginx
docker exec gacha-frontend nginx -s reload

# 测试搜索接口
curl http://localhost/api/auth/search?keyword=test001
```

如果看到JSON数据返回，说明修复成功！

## 步骤2：构建前端生产版本

### 2.1 构建game-mall

```bash
cd E:\CFDemo\gacha-system\game-mall

# 安装依赖（如果还没安装）
npm install

# 构建生产版本
npm run build

# 生成的文件在 dist/ 目录
```

### 2.2 构建cms-admin

```bash
cd E:\CFDemo\gacha-system\cms-admin

# 安装依赖（如果还没安装）
npm install

# 构建生产版本
npm run build

# 生成的文件在 dist/ 目录
```

## 步骤3：上传到服务器

### 3.1 上传game-mall

```powershell
# 压缩dist目录
cd E:\CFDemo\gacha-system\game-mall
Compress-Archive -Path dist\* -DestinationPath game-mall-dist.zip -Force

# 上传到服务器
scp game-mall-dist.zip root@111.228.12.167:/tmp/

# 在服务器上解压并部署
ssh root@111.228.12.167 << 'EOF'
mkdir -p /opt/gacha-system/frontend
rm -rf /opt/gacha-system/frontend/*
unzip /tmp/game-mall-dist.zip -d /opt/gacha-system/frontend/
docker cp /opt/gacha-system/frontend/. gacha-frontend:/usr/share/nginx/html/
docker exec gacha-frontend nginx -s reload
echo "✅ game-mall部署完成"
EOF
```

### 3.2 上传cms-admin

```powershell
# 压缩dist目录
cd E:\CFDemo\gacha-system\cms-admin
Compress-Archive -Path dist\* -DestinationPath cms-admin-dist.zip -Force

# 上传到服务器
scp cms-admin-dist.zip root@111.228.12.167:/tmp/

# 在服务器上解压并部署
ssh root@111.228.12.167 << 'EOF'
mkdir -p /opt/gacha-system/cms
rm -rf /opt/gacha-system/cms/*
unzip /tmp/cms-admin-dist.zip -d /opt/gacha-system/cms/
docker cp /opt/gacha-system/cms/. gacha-frontend:/usr/share/nginx/html/cms/
docker exec gacha-frontend nginx -s reload
echo "✅ cms-admin部署完成"
EOF
```

## 步骤4：验证部署

访问以下URL验证：
- 主页：http://111.228.12.167
- CMS后台：http://111.228.12.167/cms
- 好友系统搜索：在主页进入好友系统，搜索test001

## 快速一键部署脚本

创建一个PowerShell脚本 `deploy-all.ps1`：

```powershell
# deploy-all.ps1
$SERVER = "root@111.228.12.167"

Write-Host "=== 开始部署 ===" -ForegroundColor Green

# 1. 修复Nginx配置
Write-Host "1. 修复Nginx配置..." -ForegroundColor Yellow
ssh $SERVER "docker exec gacha-frontend sed -i 's|proxy_pass http://auth_service/;|proxy_pass http://auth_service/api/auth/;|' /etc/nginx/conf.d/default.conf && docker exec gacha-frontend nginx -s reload"

# 2. 构建game-mall
Write-Host "2. 构建game-mall..." -ForegroundColor Yellow
cd E:\CFDemo\gacha-system\game-mall
npm run build
Compress-Archive -Path dist\* -DestinationPath game-mall-dist.zip -Force
scp game-mall-dist.zip ${SERVER}:/tmp/

# 3. 构建cms-admin  
Write-Host "3. 构建cms-admin..." -ForegroundColor Yellow
cd E:\CFDemo\gacha-system\cms-admin
npm run build
Compress-Archive -Path dist\* -DestinationPath cms-admin-dist.zip -Force
scp cms-admin-dist.zip ${SERVER}:/tmp/

# 4. 部署到服务器
Write-Host "4. 部署到服务器..." -ForegroundColor Yellow
ssh $SERVER @"
mkdir -p /opt/gacha-system/frontend /opt/gacha-system/cms
rm -rf /opt/gacha-system/frontend/* /opt/gacha-system/cms/*
unzip -o /tmp/game-mall-dist.zip -d /opt/gacha-system/frontend/
unzip -o /tmp/cms-admin-dist.zip -d /opt/gacha-system/cms/
docker cp /opt/gacha-system/frontend/. gacha-frontend:/usr/share/nginx/html/
docker cp /opt/gacha-system/cms/. gacha-frontend:/usr/share/nginx/html/cms/
docker exec gacha-frontend nginx -s reload
echo '✅ 部署完成！'
"@

Write-Host "=== 部署完成！访问 http://111.228.12.167 ===" -ForegroundColor Green
```

然后执行：
```powershell
.\deploy-all.ps1
```

## 注意事项

1. **确保服务器有足够的磁盘空间**
2. **确保npm/node已安装**
3. **如果构建失败，检查是否有编译错误**
4. **部署前建议备份当前版本**

## 回滚方案

如果部署后出现问题，可以回滚：

```bash
# 如果有备份
docker cp /backup/nginx-html-backup/. gacha-frontend:/usr/share/nginx/html/
docker exec gacha-frontend nginx -s reload
```
