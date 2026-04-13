#!/usr/bin/env python3

file_path = '/opt/gacha-system/mall/application-prod.yml'

# 读取文件
with open(file_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

# 替换http.encoding为server.servlet.encoding
new_lines = []
i = 0
while i < len(lines):
    line = lines[i]
    # 找到 "  http:" 这一行
    if line.strip() == 'http:' and i > 0 and 'spring' in lines[i-1]:
        # 跳过http及其子项(encoding, charset等)
        new_lines.append('  server:\n')
        new_lines.append('    servlet:\n')
        new_lines.append('      encoding:\n')
        new_lines.append('        charset: UTF-8\n')
        new_lines.append('        enabled: true\n')
        new_lines.append('        force: true\n')
        # 跳过原来的http块
        i += 1
        while i < len(lines) and (lines[i].startswith('    ') or lines[i].strip() == ''):
            if not lines[i].startswith('    ') and lines[i].strip() != '':
                break
            i += 1
        continue
    else:
        new_lines.append(line)
    i += 1

# 写回文件
with open(file_path, 'w', encoding='utf-8') as f:
    f.writelines(new_lines)

print('Fixed! Changed to server.servlet.encoding.')
