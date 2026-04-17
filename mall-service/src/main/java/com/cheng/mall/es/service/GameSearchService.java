package com.cheng.mall.es.service;

import com.cheng.mall.es.document.GameDocument;
import com.cheng.mall.es.repository.GameEsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * 游戏搜索服务
 * 支持：全文搜索、拼音搜索、同义词、多字段加权、多种排序、自动补全
 */
@Slf4j
@Service
public class GameSearchService {
    
    @Autowired
    @Lazy  // 延迟加载,避免启动时因ES未就绪导致失败
    private GameEsRepository gameEsRepository;
    
    @Autowired
    private ElasticsearchOperations elasticsearchOperations;
    
    /**
     * 全文搜索游戏
     * 
     * @param keyword 搜索关键词
     * @param page 页码（从0开始）
     * @param size 每页数量
     * @param sortBy 排序字段：relevance(相关度)/rating(评分)/price(价格)/sales(销量)/date(发布日期)
     * @param order 排序方式：asc/desc
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @param categories 分类过滤
     * @param tags 标签过滤
     * @return 搜索结果
     */
    public Map<String, Object> searchGames(
            String keyword,
            int page,
            int size,
            String sortBy,
            String order,
            Double minPrice,
            Double maxPrice,
            List<String> categories,
            List<String> tags) {
        
        log.info("搜索游戏 - 关键词: {}, 页码: {}, 排序: {}", keyword, page, sortBy);
        
        // 构建查询
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        
        // 1. 构建多字段匹配查询（带权重）
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryBuilder.withQuery(
                boolQuery()
                    // 标题匹配（权重最高）
                    .should(matchQuery("title", keyword).boost(3.0f))
                    // 标题拼音匹配
                    .should(matchQuery("titlePinyin", keyword).boost(2.5f))
                    // 标签精确匹配
                    .should(termQuery("tags", keyword).boost(2.0f))
                    // 短描述匹配
                    .should(matchQuery("shortDescription", keyword).boost(1.5f))
                    // 完整描述匹配
                    .should(matchQuery("fullDescription", keyword).boost(1.0f))
                    // 开发商/发行商匹配
                    .should(matchQuery("developer", keyword).boost(1.2f))
                    .should(matchQuery("publisher", keyword).boost(1.2f))
                    .minimumShouldMatch(1)
            );
        } else {
            // 无关键词时返回所有在售游戏
            queryBuilder.withQuery(matchAllQuery());
        }
        
        // 2. 添加过滤条件
        if (minPrice != null || maxPrice != null) {
            queryBuilder.withFilter(
                rangeQuery("currentPrice")
                    .gte(minPrice != null ? minPrice : 0)
                    .lte(maxPrice != null ? maxPrice : Double.MAX_VALUE)
            );
        }
        
        if (categories != null && !categories.isEmpty()) {
            queryBuilder.withFilter(termsQuery("categories", categories));
        }
        
        if (tags != null && !tags.isEmpty()) {
            queryBuilder.withFilter(termsQuery("tags", tags));
        }
        
        // 只返回在售游戏
        queryBuilder.withFilter(termQuery("isOnSale", true));
        
        // 3. 设置排序
        String sortField = getSortField(sortBy);
        
        if ("relevance".equals(sortBy)) {
            // 按相关度排序（默认）- 不需要额外设置，ES 默认按 _score 排序
        } else {
            org.springframework.data.domain.Sort sort = "asc".equalsIgnoreCase(order) ?
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Order.asc(sortField)) :
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Order.desc(sortField));
            queryBuilder.withSort(sort);
        }
        
        // 4. 分页
        queryBuilder.withPageable(PageRequest.of(page, size));
        
        // 5. 执行搜索
        Query query = queryBuilder.build();
        SearchHits<GameDocument> searchHits = elasticsearchOperations.search(query, GameDocument.class);
        
        // 6. 转换结果
        List<Map<String, Object>> results = searchHits.getSearchHits().stream()
            .map(hit -> convertToMap(hit.getContent(), hit.getScore()))
            .collect(Collectors.toList());
        
        // 7. 构建响应
        Map<String, Object> response = new HashMap<>();
        response.put("results", results);
        response.put("total", searchHits.getTotalHits());
        response.put("page", page);
        response.put("size", size);
        response.put("totalPages", (int) Math.ceil((double) searchHits.getTotalHits() / size));
        
        return response;
    }
    
    /**
     * 自动补全（Autocomplete）
     * 
     * @param prefix 输入前缀
     * @param size 返回数量
     * @return 补全建议列表
     */
    public List<Map<String, Object>> autocomplete(String prefix, int size) {
        log.info("自动补全 - 前缀: {}", prefix);
        
        if (prefix == null || prefix.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        // 使用 completion suggester 进行自动补全
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        
        // 匹配标题以输入前缀开头的游戏
        queryBuilder.withQuery(
            boolQuery()
                .must(prefixQuery("title.keyword", prefix))
                .filter(termQuery("isOnSale", true))
        );
        
        queryBuilder.withPageable(PageRequest.of(0, size));
        
        Query query = queryBuilder.build();
        SearchHits<GameDocument> searchHits = elasticsearchOperations.search(query, GameDocument.class);
        
        return searchHits.getSearchHits().stream()
            .map(hit -> {
                GameDocument doc = hit.getContent();
                Map<String, Object> suggestion = new HashMap<>();
                suggestion.put("id", doc.getId());
                suggestion.put("title", doc.getTitle());
                suggestion.put("coverImage", doc.getCoverImage());
                suggestion.put("currentPrice", doc.getCurrentPrice());
                suggestion.put("basePrice", doc.getBasePrice());
                suggestion.put("discountRate", doc.getDiscountRate());
                // 添加分类信息（取第一个分类）
                if (doc.getCategories() != null && !doc.getCategories().isEmpty()) {
                    suggestion.put("category", doc.getCategories().get(0));
                } else {
                    suggestion.put("category", "游戏");
                }
                // 添加标签信息（取前两个标签）
                if (doc.getTags() != null && !doc.getTags().isEmpty()) {
                    suggestion.put("tags", doc.getTags().subList(0, Math.min(2, doc.getTags().size())));
                } else {
                    suggestion.put("tags", Collections.emptyList());
                }
                suggestion.put("score", hit.getScore());
                return suggestion;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 获取热门搜索词
     * 
     * @param size 返回数量
     * @return 热门游戏列表（按销量排序）
     */
    public List<Map<String, Object>> getHotSearches(int size) {
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withQuery(matchAllQuery());
        queryBuilder.withFilter(termQuery("isOnSale", true));
        
        // 按销量降序排序
        org.springframework.data.domain.Sort sort = 
            org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Order.desc("totalSales")
            );
        queryBuilder.withSort(sort);
        
        queryBuilder.withPageable(PageRequest.of(0, size));
        
        Query query = queryBuilder.build();
        SearchHits<GameDocument> searchHits = elasticsearchOperations.search(query, GameDocument.class);
        
        return searchHits.getSearchHits().stream()
            .map(hit -> {
                GameDocument doc = hit.getContent();
                Map<String, Object> game = new HashMap<>();
                game.put("id", doc.getId());
                game.put("title", doc.getTitle());
                game.put("coverImage", doc.getCoverImage());
                game.put("currentPrice", doc.getCurrentPrice());
                game.put("totalSales", doc.getTotalSales());
                return game;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 索引单个游戏
     */
    public void indexGame(GameDocument game) {
        gameEsRepository.save(game);
        log.info("索引游戏成功: {}", game.getTitle());
    }
    
    /**
     * 批量索引游戏
     */
    public void indexGames(List<GameDocument> games) {
        gameEsRepository.saveAll(games);
        log.info("批量索引游戏成功，数量: {}", games.size());
    }
    
    /**
     * 删除游戏索引
     */
    public void deleteGame(Long gameId) {
        gameEsRepository.deleteById(gameId);
        log.info("删除游戏索引: {}", gameId);
    }
    
    /**
     * 重建索引
     */
    public void rebuildIndex() {
        gameEsRepository.deleteAll();
        log.info("已清空游戏索引");
    }
    
    /**
     * 转换为 Map
     */
    private Map<String, Object> convertToMap(GameDocument doc, Float score) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", doc.getId());
        map.put("title", doc.getTitle());
        map.put("shortDescription", doc.getShortDescription());
        map.put("coverImage", doc.getCoverImage());
        map.put("basePrice", doc.getBasePrice());
        map.put("currentPrice", doc.getCurrentPrice());
        map.put("discountRate", doc.getDiscountRate());
        map.put("rating", doc.getRating());
        map.put("ratingCount", doc.getRatingCount());
        map.put("totalSales", doc.getTotalSales());
        map.put("tags", doc.getTags());
        map.put("categories", doc.getCategories());
        map.put("developer", doc.getDeveloper());
        map.put("isFeatured", doc.getIsFeatured());
        map.put("score", score); // 相关度分数
        return map;
    }
    
    /**
     * 获取排序字段
     */
    private String getSortField(String sortBy) {
        if (sortBy == null) {
            return "_score";
        }
        
        switch (sortBy.toLowerCase()) {
            case "rating":
                return "rating";
            case "price":
                return "currentPrice";
            case "sales":
                return "totalSales";
            case "date":
                return "releaseDate";
            case "reviews":
                return "totalReviews";
            default:
                return "_score";
        }
    }
}
