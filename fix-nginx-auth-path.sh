#!/bin/bash

# 修复Nginx配置中的auth-service代理路径重复问题

echo "正在修复Nginx配置..."

# 进入Nginx容器
docker exec gacha-frontend bash -c 'cat > /etc/nginx/conf.d/default.conf << '"'"'EOF'"'"'
# 上游服务器配置（使用变量实现动态DNS解析）
upstream auth_service {
    server gacha-auth-service:8084;
}

upstream game_service {
    server gacha-game-service:8082;
}

upstream mall_service {
    server gacha-mall-service:8081;
}

server {
    listen 80;
    server_name localhost;
    root /usr/share/nginx/html;
    index index.html;

    # 认证服务
    location /api/auth/ {
        proxy_pass http://auth_service/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # 超时设置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # 游戏服务
    location /api/game/ {
        proxy_pass http://game_service/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # 超时设置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # 商城服务（主要API）
    location /api/ {
        proxy_pass http://mall_service/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # 超时设置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # WebSocket支持（好友系统实时通知）
    location /ws {
        proxy_pass http://mall_service/ws;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_read_timeout 86400s;
        proxy_send_timeout 86400s;
    }

    # CMS管理系统
    location /cms {
        alias /usr/share/nginx/html/cms;
        try_files $uri $uri/ /cms/index.html;
        expires 1d;
        add_header Cache-Control "public";
    }

    # CMS API代理（如果需要）
    location /api/cms/ {
        proxy_pass http://mall_service/api/cms/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 上传文件
    location /uploads/ {
        proxy_pass http://auth_service/uploads/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 静态资源
    location /GamePapers/ {
        alias /opt/gacha-system/GamePapers/;
        expires 30d;
        add_header Cache-Control "public, immutable";
    }

    # 前端路由
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 错误页面
    error_page 500 502 503 504 /50x.html;
    location = /50x.html {
        root /usr/share/nginx/html;
    }
}
EOF'

# 重新加载Nginx配置
docker exec gacha-frontend nginx -s reload

echo "✅ Nginx配置已修复并重新加载"
echo ""
echo "测试搜索接口："
sleep 2
curl -s http://localhost/api/auth/search?keyword=test001&page=1&size=10 | python3 -m json.tool || echo "请在本地测试: http://111.228.12.167/api/auth/search?keyword=test001"
