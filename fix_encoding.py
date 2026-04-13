#!/usr/bin/env python3

file_path = '/opt/gacha-system/mall/application-prod.yml'

# 读取文件
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# 在spring节点下添加http编码配置
# 找到 "spring:" 这一行,在其后添加配置
lines = content.split('\n')
new_lines = []
for i, line in enumerate(lines):
    new_lines.append(line)
    # 在 spring: 之后立即添加编码配置
    if line.strip() == 'spring:':
        new_lines.append('  http:')
        new_lines.append('    encoding:')
        new_lines.append('      charset: UTF-8')
        new_lines.append('      enabled: true')
        new_lines.append('      force: true')

# 写回文件
with open(file_path, 'w', encoding='utf-8') as f:
    f.write('\n'.join(new_lines))

print('Fixed! Added HTTP encoding configuration.')
