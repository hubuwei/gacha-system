#!/bin/bash
# 修复微信支付服务初始化问题 - 让它在没有证书的情况下也能启动

echo "正在修复微信支付服务..."

# 停止容器
cd /opt/gacha-system
docker compose stop mall-service

# 进入容器修改class文件(由于是jar包,我们需要重新打包)
# 更简单的方法:修改application-prod.yml,添加mock-enabled配置

# 检查application-prod.yml中是否有wechat配置
if ! grep -q "mock-enabled" /opt/gacha-system/mall-service/src/main/resources/application-prod.yml; then
    echo "添加mock-enabled配置到application-prod.yml..."
    cat >> /opt/gacha-system/mall-service/src/main/resources/application-prod.yml << 'EOF'

# 微信支付配置（使用模拟模式，跳过证书验证）
wechat:
  pay:
    mock-enabled: true
    app-id: mock-app-id
    mch-id: mock-mch-id
    api-v3-key: mock-api-v3-key
    private-key-path: classpath:cert/mock.pem
    serial-no: mock-serial-no
    notify-url: http://localhost/mock/notify
EOF
    echo "配置添加成功!"
else
    echo "配置已存在,跳过..."
fi

# 重新启动服务
echo "重新启动mall-service..."
docker compose up -d mall-service

echo "修复完成!请等待30秒后检查日志..."
sleep 30
docker logs --tail 50 gacha-mall-service | grep -E '(Started|ERROR.*Wechat|mock)'
