package com.cheng.mall.controller;

import com.cheng.common.dto.CommonResponse;
import com.cheng.mall.entity.Game;
import com.cheng.mall.service.GameService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 搜索控制器
 * 处理ES搜索相关请求
 */
@Slf4j
@RestController
@RequestMapping("/search")
@CrossOrigin(origins = "*")
public class SearchController {
    
    @Autowired
    private GameService gameService;
    
    /**
     * 获取搜索建议
     * 用于前端搜索框的实时建议
     */
    @GetMapping("/autocomplete")
    public CommonResponse<List<Map<String, Object>>> getSearchSuggestions(
            @RequestParam String prefix,
            @RequestParam(defaultValue = "5") int size) {
        try {
            log.info("获取搜索建议: prefix={}, size={}", prefix, size);
            
            // 调用搜索服务获取建议
            List<Game> games = gameService.searchGames(prefix);
            
            // 转换为前端需要的格式
            List<Map<String, Object>> suggestions = games.stream()
                    .limit(size)
                    .map(game -> {
                        Map<String, Object> suggestion = new java.util.HashMap<>();
                        suggestion.put("id", game.getId());
                        suggestion.put("title", game.getTitle());
                        suggestion.put("currentPrice", game.getCurrentPrice());
                        suggestion.put("basePrice", game.getBasePrice());
                        suggestion.put("discountRate", game.getDiscountRate());
                        suggestion.put("coverImage", game.getCoverImage());
                        
                        // 获取第一个分类作为category
                        List<String> categories = gameService.getGameDetail(game.getId()).getCategories();
                        if (!categories.isEmpty()) {
                            suggestion.put("category", categories.get(0));
                        }
                        
                        // 获取标签列表
                        List<Integer> tagIds = gameService.getGamesWithTags().stream()
                                .filter(g -> g.get("id").equals(game.getId()))
                                .findFirst()
                                .map(g -> (List<Integer>) g.get("tagIds"))
                                .orElse(java.util.Collections.emptyList());
                        
                        List<String> tags = tagIds.stream()
                                .map(tagId -> gameService.getAllTags().stream()
                                        .filter(tag -> tag.getId().equals(tagId))
                                        .map(tag -> tag.getName())
                                        .findFirst()
                                        .orElse(null))
                                .filter(tag -> tag != null)
                                .collect(Collectors.toList());
                        
                        suggestion.put("tags", tags);
                        
                        return suggestion;
                    })
                    .collect(Collectors.toList());
            
            return CommonResponse.success(suggestions);
        } catch (Exception e) {
            log.error("获取搜索建议失败", e);
            return CommonResponse.error("获取搜索建议失败：" + e.getMessage());
        }
    }
    
    /**
     * 执行游戏搜索
     * 用于前端搜索结果页
     */
    @GetMapping("/games")
    public CommonResponse<Map<String, Object>> searchGames(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(defaultValue = "relevance") String sortBy,
            @RequestParam(defaultValue = "desc") String order) {
        try {
            log.info("执行游戏搜索: keyword={}, page={}, size={}, sortBy={}, order={}", 
                    keyword, page, size, sortBy, order);
            
            // 调用搜索服务获取结果
            List<Game> games = gameService.searchGames(keyword);
            
            // 构建响应数据
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("total", games.size());
            result.put("size", size);
            result.put("totalPages", (games.size() + size - 1) / size);
            result.put("page", page);
            result.put("results", games.stream()
                    .map(game -> {
                        Map<String, Object> gameMap = new java.util.HashMap<>();
                        gameMap.put("id", game.getId());
                        gameMap.put("title", game.getTitle());
                        gameMap.put("shortDescription", game.getShortDescription());
                        gameMap.put("fullDescription", game.getFullDescription());
                        gameMap.put("developer", game.getDeveloper());
                        gameMap.put("publisher", game.getPublisher());
                        gameMap.put("coverImage", game.getCoverImage());
                        gameMap.put("basePrice", game.getBasePrice());
                        gameMap.put("currentPrice", game.getCurrentPrice());
                        gameMap.put("discountRate", game.getDiscountRate());
                        gameMap.put("rating", game.getRating());
                        gameMap.put("ratingCount", game.getRatingCount());
                        gameMap.put("totalSales", game.getTotalSales());
                        gameMap.put("totalReviews", game.getTotalReviews());
                        gameMap.put("isFeatured", game.getIsFeatured());
                        gameMap.put("isOnSale", game.getIsOnSale());
                        gameMap.put("releaseDate", game.getReleaseDate());
                        gameMap.put("updatedAt", game.getUpdatedAt());
                        
                        // 获取分类列表
                        List<String> categories = gameService.getGameDetail(game.getId()).getCategories();
                        gameMap.put("categories", categories);
                        
                        // 获取标签列表
                        List<Integer> tagIds = gameService.getGamesWithTags().stream()
                                .filter(g -> g.get("id").equals(game.getId()))
                                .findFirst()
                                .map(g -> (List<Integer>) g.get("tagIds"))
                                .orElse(java.util.Collections.emptyList());
                        
                        List<String> tags = tagIds.stream()
                                .map(tagId -> gameService.getAllTags().stream()
                                        .filter(tag -> tag.getId().equals(tagId))
                                        .map(tag -> tag.getName())
                                        .findFirst()
                                        .orElse(null))
                                .filter(tag -> tag != null)
                                .collect(Collectors.toList());
                        
                        gameMap.put("tags", tags);
                        
                        return gameMap;
                    })
                    .collect(Collectors.toList()));
            
            return CommonResponse.success(result);
        } catch (Exception e) {
            log.error("搜索游戏失败", e);
            return CommonResponse.error("搜索游戏失败：" + e.getMessage());
        }
    }
}