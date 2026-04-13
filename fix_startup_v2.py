#!/usr/bin/env python3

file_path = '/opt/gacha-system/start-mall-final.sh'

# 读取文件
with open(file_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

# 修改包含密码的行
new_lines = []
for line in lines:
    if '--spring.datasource.password=' in line:
        new_lines.append('  --spring.datasource.password="Xc037417!" \\\n')
    elif '--spring.redis.password=' in line:
        new_lines.append('  --spring.redis.password="Xc037417!" \\\n')
    elif '--spring.rabbitmq.password=' in line:
        new_lines.append('  --spring.rabbitmq.password="Xc037417!" \\\n')
    else:
        new_lines.append(line)

# 写回文件
with open(file_path, 'w', encoding='utf-8') as f:
    f.writelines(new_lines)

print('Fixed startup script with proper quoting.')
