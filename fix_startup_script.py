#!/usr/bin/env python3

file_path = '/opt/gacha-system/start-mall-final.sh'

# 读取文件
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# 替换所有未加引号的密码
content = content.replace('--spring.datasource.password=Xc037417!', '--spring.datasource.password="Xc037417!"')
content = content.replace('--spring.redis.password=Xc037417!', '--spring.redis.password="Xc037417!"')
content = content.replace('--spring.rabbitmq.password=Xc037417!', '--spring.rabbitmq.password="Xc037417!"')

# 写回文件
with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print('Fixed! All passwords are now quoted in startup script.')
