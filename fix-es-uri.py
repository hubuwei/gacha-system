#!/usr/bin/env python3
filepath = '/opt/gacha-system/mall-service/src/main/resources/application-prod.yml'
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace(
    'uris: ${ES_URIS:http://localhost:9200}',
    'uris: ${ES_URIS:http://elasticsearch:9200}'
)

with open(filepath, 'w') as f:
    f.write(content)

print('ES URI fixed')
