#!/bin/bash
echo "正在修复Nginx配置..."

# 修复auth-service代理
docker exec gacha-frontend sed -i 's|proxy_pass http://auth_service/api/auth/;|proxy_pass http://auth_service/;|' /etc/nginx/conf.d/default.conf

# 修复game-service代理
docker exec gacha-frontend sed -i 's|proxy_pass http://game_service/api/game/;|proxy_pass http://game_service/;|' /etc/nginx/conf.d/default.conf

# 修复mall-service代理
docker exec gacha-frontend sed -i 's|proxy_pass http://mall_service/api/;|proxy_pass http://mall_service/;|' /etc/nginx/conf.d/default.conf

# 重新加载Nginx
docker exec gacha-frontend nginx -s reload

echo "✅ Nginx配置已修复"
echo ""
echo "等待2秒后测试搜索接口..."
sleep 2

# 测试搜索接口
echo "测试URL: http://localhost/api/auth/search?keyword=test001"
curl -s http://localhost/api/auth/search?keyword=test001 | head -c 500
echo ""
echo ""
echo "如果看到JSON数据包含test001，说明修复成功！"
