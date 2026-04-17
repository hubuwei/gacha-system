#!/bin/bash
# 修复docker-compose.yml中的WECHAT_PAY_MOCK_ENABLED配置

FILE="/opt/gacha-system/docker-compose.yml"

# 使用sed修复引号问题
sed -i 's/WECHAT_PAY_MOCK_ENABLED: " true/WECHAT_PAY_MOCK_ENABLED: "true"/' "$FILE"

echo "修复完成!"
grep "WECHAT_PAY_MOCK" "$FILE"
