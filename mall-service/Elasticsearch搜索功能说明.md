# Elasticsearch 游戏搜索功能使用指南

## 📋 功能概述

基于 Elasticsearch 实现的游戏搜索系统，支持：

✅ **全文搜索**
- 游戏名称、描述、标签、开发商等多字段搜索
- 中文分词（IK Analyzer）
- 拼音搜索（输入拼音也能搜到中文）
- 同义词支持（如"RPG"和"角色扮演"互通）

✅ **智能排序**
- 按相关度排序（默认）
- 按评分排序
- 按价格排序
- 按销量/热度排序
- 按发布日期排序

✅ **自动补全（Autocomplete）**
- 输入时实时显示建议
- 支持前缀匹配

✅ **高级过滤**
- 价格范围过滤
- 分类过滤
- 标签过滤

---

## 🔧 前置条件

### 1. 安装 Elasticsearch

```bash
# Windows (使用 Chocolatey)
choco install elasticsearch

# 或从官网下载
# https://www.elastic.co/cn/downloads/elasticsearch
```

### 2. 安装中文分词插件（IK Analyzer）

```bash
# 进入 ES 安装目录
cd elasticsearch

# 安装 IK 分词器
.\bin\elasticsearch-plugin install https://github.com/medcl/elasticsearch-analysis-ik/releases/download/v7.17.9/elasticsearch-analysis-ik-7.17.9.zip
```

### 3. 安装拼音插件（可选，用于拼音搜索）

```bash
# 安装拼音分词器
.\bin\elasticsearch-plugin install https://github.com/medcl/elasticsearch-analysis-pinyin/releases/download/v7.17.9/elasticsearch-analysis-pinyin-7.17.9.zip
```

### 4. 启动 Elasticsearch

```bash
# Windows
.\bin\elasticsearch.bat

# Linux/Mac
./bin/elasticsearch
```

验证是否启动成功：
```bash
curl http://localhost:9200
```

应该返回类似：
```json
{
  "name": "your-node-name",
  "cluster_name": "elasticsearch",
  "version": {
    "number": "7.17.9",
    ...
  }
}
```

---

## 🚀 快速开始

### 步骤 1: 启动 mall-service

```bash
cd mall-service
mvn spring-boot:run
```

### 步骤 2: 同步数据到 Elasticsearch

首次使用需要将所有游戏数据从 MySQL 同步到 ES：

```bash
# 使用 curl
curl -X POST http://localhost:8081/api/sync/games

# 或使用 PowerShell
Invoke-RestMethod -Uri "http://localhost:8081/api/sync/games" -Method Post
```

### 步骤 3: 测试搜索功能

#### 基本搜索
```bash
# 搜索关键词 "侠盗"
curl "http://localhost:8081/api/search/games?keyword=侠盗"

# 搜索并分页
curl "http://localhost:8081/api/search/games?keyword=GTA&page=0&size=10"
```

#### 按不同字段排序
```bash
# 按评分排序（从高到低）
curl "http://localhost:8081/api/search/games?keyword=&sortBy=rating&order=desc"

# 按价格排序（从低到高）
curl "http://localhost:8081/api/search/games?keyword=&sortBy=price&order=asc"

# 按销量排序（热门游戏）
curl "http://localhost:8081/api/search/games?keyword=&sortBy=sales&order=desc"

# 按发布日期排序（最新游戏）
curl "http://localhost:8081/api/search/games?keyword=&sortBy=date&order=desc"
```

#### 带过滤条件的搜索
```bash
# 价格范围：50-200元
curl "http://localhost:8081/api/search/games?keyword=&minPrice=50&maxPrice=200"

# 指定分类
curl "http://localhost:8081/api/search/games?keyword=&categories=动作,冒险"

# 指定标签
curl "http://localhost:8081/api/search/games?keyword=&tags=开放世界,多人"

# 组合条件
curl "http://localhost:8081/api/search/games?keyword=RPG&minPrice=0&maxPrice=300&sortBy=rating&order=desc"
```

#### 自动补全
```bash
# 输入 "侠"，获取建议
curl "http://localhost:8081/api/search/autocomplete?prefix=侠&size=5"

# 返回示例：
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "title": "侠盗猎车手 V",
      "coverImage": "...",
      "currentPrice": 199.00,
      "score": 1.0
    }
  ]
}
```

#### 热门搜索
```bash
# 获取最热门的10个游戏（按销量）
curl "http://localhost:8081/api/search/hot?size=10"
```

---

## 📊 API 接口文档

### 1. 搜索游戏

**接口**: `GET /api/search/games`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 | 默认值 |
|------|------|------|------|--------|
| keyword | String | 否 | 搜索关键词 | - |
| page | Integer | 否 | 页码（从0开始） | 0 |
| size | Integer | 否 | 每页数量 | 20 |
| sortBy | String | 否 | 排序字段：relevance/rating/price/sales/date | relevance |
| order | String | 否 | 排序方式：asc/desc | desc |
| minPrice | Double | 否 | 最低价格 | - |
| maxPrice | Double | 否 | 最高价格 | - |
| categories | List<String> | 否 | 分类过滤 | - |
| tags | List<String> | 否 | 标签过滤 | - |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "results": [
      {
        "id": 1,
        "title": "侠盗猎车手 V",
        "shortDescription": "开放世界动作冒险游戏",
        "coverImage": "https://...",
        "basePrice": 299.00,
        "currentPrice": 199.00,
        "discountRate": 33,
        "rating": 9.5,
        "ratingCount": 150000,
        "totalSales": 500000,
        "tags": ["开放世界", "犯罪", "动作"],
        "categories": ["动作", "冒险"],
        "developer": "Rockstar Games",
        "isFeatured": true,
        "score": 3.5
      }
    ],
    "total": 1,
    "page": 0,
    "size": 20,
    "totalPages": 1
  }
}
```

### 2. 自动补全

**接口**: `GET /api/search/autocomplete`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 | 默认值 |
|------|------|------|------|--------|
| prefix | String | 是 | 输入前缀 | - |
| size | Integer | 否 | 返回数量 | 10 |

### 3. 热门搜索

**接口**: `GET /api/search/hot`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 | 默认值 |
|------|------|------|------|--------|
| size | Integer | 否 | 返回数量 | 10 |

### 4. 同步数据

**接口**: `POST /api/sync/games`

全量同步所有游戏数据到 Elasticsearch。

### 5. 增量同步

**接口**: `POST /api/sync/game/{gameId}`

同步单个游戏数据（异步执行）。

---

## 🎯 搜索特性详解

### 1. 多字段加权搜索

搜索时会对不同字段赋予不同权重：
- **标题**: 权重 3.0（最高）
- **标题拼音**: 权重 2.5
- **标签**: 权重 2.0
- **短描述**: 权重 1.5
- **开发商/发行商**: 权重 1.2
- **完整描述**: 权重 1.0

例如搜索 "GTA"，标题中包含的游戏会排在前面。

### 2. 中文分词

使用 IK Analyzer 进行中文分词：
- `ik_max_word`: 索引时使用，最大程度分词
- `ik_smart`: 搜索时使用，智能分词

示例：
```
输入："侠盗猎车手"
分词结果：["侠盗", "猎车", "车手", "侠盗猎车手"]
```

### 3. 拼音搜索

支持拼音搜索（需安装拼音插件）：
```
输入："xiadao" 或 "xd"
可以搜到："侠盗猎车手"
```

### 4. 同义词

配置了游戏相关同义词（见 `synonym.txt`）：
```
RPG = 角色扮演 = role-playing game
FPS = 第一人称射击
GTA5 = GTA V = 侠盗猎车手5
```

搜索 "角色扮演" 也会返回标记为 "RPG" 的游戏。

### 5. 智能排序

#### 相关度排序（默认）
根据 ES 的相关度评分（`_score`）排序，综合考虑：
- 关键词匹配程度
- 字段权重
- TF-IDF 算法

#### 其他排序方式
- **评分**: 按用户评分高低
- **价格**: 按当前价格
- **销量**: 按总销量（热度）
- **日期**: 按发布日期

---

## 🔍 使用示例（前端集成）

### React 示例

```jsx
import { useState, useEffect } from 'react';

function GameSearch() {
  const [keyword, setKeyword] = useState('');
  const [results, setResults] = useState([]);
  const [suggestions, setSuggestions] = useState([]);
  const [sortBy, setSortBy] = useState('relevance');
  
  // 搜索游戏
  const searchGames = async () => {
    const params = new URLSearchParams({
      keyword,
      page: 0,
      size: 20,
      sortBy,
      order: 'desc'
    });
    
    const response = await fetch(`/api/search/games?${params}`);
    const result = await response.json();
    
    if (result.code === 200) {
      setResults(result.data.results);
    }
  };
  
  // 自动补全
  const fetchSuggestions = async (prefix) => {
    if (!prefix) {
      setSuggestions([]);
      return;
    }
    
    const response = await fetch(`/api/search/autocomplete?prefix=${prefix}&size=5`);
    const result = await response.json();
    
    if (result.code === 200) {
      setSuggestions(result.data);
    }
  };
  
  // 防抖搜索
  useEffect(() => {
    const timer = setTimeout(() => {
      if (keyword) {
        searchGames();
        fetchSuggestions(keyword);
      }
    }, 300);
    
    return () => clearTimeout(timer);
  }, [keyword, sortBy]);
  
  return (
    <div>
      {/* 搜索框 */}
      <input
        type="text"
        value={keyword}
        onChange={(e) => setKeyword(e.target.value)}
        placeholder="搜索游戏..."
      />
      
      {/* 自动补全建议 */}
      {suggestions.length > 0 && (
        <ul className="suggestions">
          {suggestions.map(item => (
            <li key={item.id} onClick={() => setKeyword(item.title)}>
              <img src={item.coverImage} alt="" />
              <span>{item.title}</span>
              <span>¥{item.currentPrice}</span>
            </li>
          ))}
        </ul>
      )}
      
      {/* 排序选项 */}
      <select value={sortBy} onChange={(e) => setSortBy(e.target.value)}>
        <option value="relevance">相关度</option>
        <option value="rating">评分</option>
        <option value="price">价格</option>
        <option value="sales">销量</option>
        <option value="date">发布日期</option>
      </select>
      
      {/* 搜索结果 */}
      <div className="results">
        {results.map(game => (
          <div key={game.id} className="game-card">
            <img src={game.coverImage} alt={game.title} />
            <h3>{game.title}</h3>
            <p>{game.shortDescription}</p>
            <div className="info">
              <span>评分: {game.rating}</span>
              <span>销量: {game.totalSales}</span>
              <span className="price">¥{game.currentPrice}</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
```

---

## ⚙️ 高级配置

### 1. 修改同义词

编辑文件：`src/main/resources/elasticsearch/synonym.txt`

格式：
```
同义词1,同义词2,同义词3 => 标准词
```

修改后需要重建索引：
```bash
# 1. 清空索引
curl -X POST http://localhost:8081/api/search/rebuild-index

# 2. 重新同步
curl -X POST http://localhost:8081/api/sync/games
```

### 2. 调整分词器配置

编辑文件：`src/main/resources/elasticsearch/settings.json`

可以调整：
- 拼音分词器参数
- 自定义分词规则
- 停用词配置

### 3. 性能优化

#### 批量索引
```java
// 每次批量索引 1000 条
List<GameDocument> batch = new ArrayList<>();
for (Game game : games) {
    batch.add(convertToDocument(game));
    if (batch.size() >= 1000) {
        gameEsRepository.saveAll(batch);
        batch.clear();
    }
}
if (!batch.isEmpty()) {
    gameEsRepository.saveAll(batch);
}
```

#### 刷新间隔
```yaml
# application.yml
spring:
  elasticsearch:
    rest:
      uris: http://localhost:9200
  data:
    elasticsearch:
      repositories:
        enabled: true
```

---

## 🐛 常见问题

### 1. Elasticsearch 连接失败

**错误**: `ConnectException: Connection refused`

**解决**:
- 检查 ES 是否启动：`curl http://localhost:9200`
- 检查端口是否正确（默认 9200）
- 检查防火墙设置

### 2. 中文分词不生效

**问题**: 搜索中文没有结果

**解决**:
- 确认已安装 IK 分词器插件
- 重启 Elasticsearch
- 检查索引映射是否正确使用了 `ik_max_word`

### 3. 拼音搜索不工作

**问题**: 输入拼音搜不到中文

**解决**:
- 安装拼音分词器插件
- 确保 `titlePinyin` 和 `descriptionPinyin` 字段已正确索引
- 在 `GameSyncService.generatePinyin()` 中实现真正的拼音转换（建议使用 pinyin4j 库）

### 4. 搜索结果不准确

**优化**:
- 调整字段权重（boost 值）
- 添加更多同义词
- 使用更精确的分词器配置

### 5. 性能问题

**优化建议**:
- 使用分页，避免一次返回太多数据
- 只查询需要的字段
- 使用过滤器（filter）而非查询（query）进行精确匹配
- 定期优化索引：`POST /games/_forcemerge`

---

## 📈 监控和维护

### 查看索引状态
```bash
curl http://localhost:9200/_cat/indices?v
```

### 查看索引大小
```bash
curl http://localhost:9200/_cat/indices/games?v
```

### 查看文档数量
```bash
curl http://localhost:9200/games/_count
```

### 优化索引
```bash
curl -X POST http://localhost:9200/games/_forcemerge?max_num_segments=1
```

### 删除索引
```bash
curl -X DELETE http://localhost:9200/games
```

---

## 🎉 总结

现在你的游戏商城拥有了强大的搜索功能：

✅ **全文搜索** - 支持中文、拼音、同义词
✅ **智能排序** - 5种排序方式
✅ **自动补全** - 实时搜索建议
✅ **高级过滤** - 价格、分类、标签
✅ **高性能** - 基于 Elasticsearch

下一步可以：
1. 添加搜索历史记录
2. 实现搜索推荐（基于用户行为）
3. 添加拼写纠错
4. 实现 facets（分面搜索）
5. 集成 Redis 缓存热门搜索

祝使用愉快！🚀
