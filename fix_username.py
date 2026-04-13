#!/usr/bin/env python3

file_path = '/opt/gacha-system/mall/application-prod.yml'

# 读取文件
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# 替换username
content = content.replace('username: root', 'username: mall_user')

# 写回文件
with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print('Fixed! Changed username to mall_user.')
