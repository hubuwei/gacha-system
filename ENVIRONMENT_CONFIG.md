# ============================================
# 环境配置说明
# ============================================

## 当前环境配置

### 开发环境（本地）
- 数据库：gacha_system_dev
- Elasticsearch：需要安装 7.17.29 + IK 分词器 7.17.29
- 配置文件：application.yml（默认）

### 生产环境（服务器）
- 数据库：gacha_system_prod
- Elasticsearch：7.17.29 + IK 分词器 7.17.29
- 配置文件：application-prod.yml
- 启动参数：--spring.profiles.active=prod

## 本地安装 Elasticsearch 7.17.29

### Windows 安装步骤

1. **下载 Elasticsearch 7.17.29**
   ```powershell
   # 下载地址
   https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-7.17.29-windows-x86_64.zip
   ```

2. **解压到指定目录**
   ```powershell
   # 建议解压到
   E:\Elasticsearch\elasticsearch-7.17.29
   ```

3. **修改配置**
   编辑 `config/elasticsearch.yml`：
   ```yaml
   cluster.name: gacha-system
   node.name: node-1
   network.host: 0.0.0.0
   http.port: 9200
   discovery.type: single-node
   ```

4. **启动 Elasticsearch**
   ```powershell
   cd E:\Elasticsearch\elasticsearch-7.17.29\bin
   .\elasticsearch.bat
   ```

5. **验证安装**
   浏览器访问：http://localhost:9200
   应该看到 JSON 响应，包含版本号 7.17.29

### 安装 IK 分词器 7.17.29

1. **下载 IK 分词器**
   ```powershell
   # 下载地址
   https://github.com/medcl/elasticsearch-analysis-ik/releases/download/v7.17.29/elasticsearch-analysis-ik-7.17.29.zip
   ```

2. **创建插件目录**
   ```powershell
   cd E:\Elasticsearch\elasticsearch-7.17.29\plugins
   mkdir ik
   ```

3. **解压到 ik 目录**
   - 将下载的 zip 文件解压到 `plugins/ik/` 目录
   - 确保所有文件直接在 `ik/` 目录下，不要有多层文件夹

4. **重启 Elasticsearch**
   ```powershell
   # 先停止（Ctrl+C）
   # 再启动
   cd E:\Elasticsearch\elasticsearch-7.17.29\bin
   .\elasticsearch.bat
   ```

5. **验证 IK 分词器**
   ```powershell
   # 使用 PowerShell 或 CMD
   curl -X POST "http://localhost:9200/_analyze" -H "Content-Type: application/json" -d "{\"analyzer\":\"ik_max_word\",\"text\":\"黑神话悟空\"}"
   ```
   
   应该返回分词结果。

## 切换环境

### 本地开发（默认）
直接运行即可，使用 `application.yml`：
```powershell
cd mall-service
mvn spring-boot:run
```

### 模拟生产环境
如果要测试生产配置：
```powershell
cd mall-service
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

需要在项目根目录创建 `.env` 文件：
```bash
DB_NAME=gacha_system_prod
DB_PASSWORD=你的MySQL密码
REDIS_PASSWORD=你的Redis密码
RABBITMQ_PASSWORD=你的RabbitMQ密码
JWT_SECRET=你的JWT密钥
```

## 服务器部署

服务器上已经配置好：
- ✅ MySQL 8.0.45
- ✅ Redis
- ✅ RabbitMQ
- ✅ Elasticsearch 7.17.29 + IK 7.17.29
- ✅ 数据库：gacha_system_prod

部署时使用：
```bash
java -jar mall-service.jar --spring.profiles.active=prod
```

或使用 systemd 服务（已配置）。

## 常见问题

### Q1: Elasticsearch 启动失败
检查 Java 版本，ES 7.17.x 需要 Java 11 或 Java 17。

### Q2: IK 分词器不生效
确认：
1. IK 版本与 ES 版本一致（都是 7.17.29）
2. 插件目录结构正确
3. 已重启 ES

### Q3: 连接不上 Elasticsearch
检查：
1. ES 是否启动成功
2. 端口 9200 是否被占用
3. 防火墙是否阻止

### Q4: 数据库连接失败
检查：
1. MySQL 是否启动
2. 数据库名称是否正确（dev vs prod）
3. 用户名密码是否正确
