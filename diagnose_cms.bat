@echo off
chcp 65001 >nul
echo ========================================
echo CMS系统诊断
echo ========================================
echo.

echo [1] 检查cms-service容器...
ssh root@111.228.12.167 "docker ps | grep cms"
echo.

echo [2] 检查Nginx配置中的cms_service...
ssh root@111.228.12.167 "docker exec gacha-frontend grep -A 3 'cms_service' /etc/nginx/conf.d/default.conf"
echo.

echo [3] 测试直接访问cms-service...
ssh root@111.228.12.167 "curl -s http://localhost:8085/api/cms/dashboard/stats | head -c 100"
echo.
echo.

echo [4] 测试通过Nginx访问...
ssh root@111.228.12.167 "curl -s http://localhost/api/cms/dashboard/stats | head -c 100"
echo.
echo.

echo [5] 检查前端文件是否有硬编码...
ssh root@111.228.12.167 "find /usr/share/nginx/html/cms -name '*.js' -exec grep -l 'localhost:8085' {} \; 2>/dev/null || echo 'No hardcoded localhost found'"
echo.

echo ========================================
echo 诊断完成
echo ========================================
pause
