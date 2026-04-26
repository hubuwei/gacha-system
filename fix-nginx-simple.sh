#!/bin/bash
# 修复Nginx auth-service代理配置并测试

echo "正在修复Nginx配置..."
docker exec gacha-frontend sed -i 's|proxy_pass http://auth_service/;|proxy_pass http://auth_service/api/auth/;|' /etc/nginx/conf.d/default.conf
docker exec gacha-frontend nginx -s reload

echo "✅ Nginx配置已修复"
echo ""
echo "等待2秒后测试搜索接口..."
sleep 2

# 测试搜索接口
echo "测试URL: http://localhost/api/auth/search?keyword=test001"
curl -s http://localhost/api/auth/search?keyword=test001 | head -c 300
echo ""
echo ""
echo "如果看到JSON数据包含test001，说明修复成功！"
