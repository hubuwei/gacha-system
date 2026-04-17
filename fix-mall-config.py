#!/usr/bin/env python3
import re

filepath = '/opt/gacha-system/mall-service/src/main/resources/application-prod.yml'
with open(filepath, 'r') as f:
    content = f.read()

# Fix datasource
content = content.replace(
    'jdbc:mysql://localhost:3306/gacha_system_prod?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&zeroDateTimeBehavior=convertToNull',
    'jdbc:mysql://${DB_HOST:mysql}:${DB_PORT:3306}/${DB_NAME:gacha_system_prod}?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&zeroDateTimeBehavior=convertToNull'
)
content = content.replace('    username: root', '    username: ${DB_USERNAME:root}')
content = content.replace('    password: Xc037417!', '    password: ${DB_PASSWORD:Xc037417!}')

# Fix Redis
content = content.replace('    host: localhost\n    port: 6379\n    password: Xc037417!',
    '    host: ${REDIS_HOST:redis}\n    port: ${REDIS_PORT:6379}\n    password: ${REDIS_PASSWORD:Xc037417!}')

# Fix RabbitMQ  
content = content.replace('    host: localhost\n    port: 5672\n    username: admin\n    password: Xc037417!',
    '    host: ${RABBITMQ_HOST:rabbitmq}\n    port: ${RABBITMQ_PORT:5672}\n    username: ${RABBITMQ_USERNAME:admin}\n    password: ${RABBITMQ_PASSWORD:Xc037417!}')

with open(filepath, 'w') as f:
    f.write(content)

print('Config fixed successfully')
