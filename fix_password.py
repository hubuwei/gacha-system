#!/usr/bin/env python3
import re

file_path = '/opt/gacha-system/mall/application-prod.yml'

# 读取文件
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# 替换密码,添加引号
content = re.sub(r'password: Xc037417!', 'password: "Xc037417!"', content)

# 写回文件
with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print('Fixed! Password is now quoted.')
