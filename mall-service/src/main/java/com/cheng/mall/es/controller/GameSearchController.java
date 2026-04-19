package com.cheng.mall.es.controller;

import com.cheng.common.dto.CommonResponse;
import com.cheng.mall.es.service.GameSearchService;
import com.cheng.mall.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 游戏搜索控制器
 */
@Slf4j
@RestController
@RequestMapping("/search")
@CrossOrigin(origins = "*")
public class GameSearchController {
    
    @Autowired
    private GameSearchService searchService;
    
    @Autowired(required = false)
    private RedisUtil redisUtil;
    
    // 搜索限流：每秒最多10次
    private static final int SEARCH_RATE_LIMIT = 10;
    private static final long SEARCH_RATE_WINDOW = 1; // 1秒
    
    /**
     * 搜索游戏（带限流）
     * 
     * GET /api/search/games?keyword=xxx&page=0&size=20&sortBy=relevance&order=desc
     */
    @GetMapping("/games")
    public CommonResponse<Map<String, Object>> searchGames(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "relevance") String sortBy,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) List<String> categories,
            @RequestParam(required = false) List<String> tags,
            HttpServletRequest request) {
        
        // 限流检查
        String clientIp = getClientIp(request);
        
        if (redisUtil != null) {
            String rateLimitKey = "rate:limit:search:" + clientIp;
            
            if (!redisUtil.checkRateLimit(rateLimitKey, SEARCH_RATE_LIMIT, SEARCH_RATE_WINDOW)) {
                log.warn("搜索限流: IP={}", clientIp);
                return CommonResponse.error("请求过于频繁，请稍后再试");
            }
        } else {
            log.warn("Redis 未启用，跳过搜索限流: IP={}", clientIp);
        }
        
        try {
            Map<String, Object> result = searchService.searchGames(
                keyword, page, size, sortBy, order, minPrice, maxPrice, categories, tags
            );
            return CommonResponse.success(result);
        } catch (Exception e) {
            log.error("搜索游戏失败", e);
            return CommonResponse.error("搜索失败：" + e.getMessage());
        }
    }
    
    /**
     * 自动补全
     * 
     * GET /api/search/autocomplete?prefix=xxx&size=10
     */
    @GetMapping("/autocomplete")
    public CommonResponse<List<Map<String, Object>>> autocomplete(
            @RequestParam String prefix,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<Map<String, Object>> suggestions = searchService.autocomplete(prefix, size);
            return CommonResponse.success(suggestions);
        } catch (Exception e) {
            log.error("自动补全失败", e);
            return CommonResponse.error("自动补全失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取热门搜索
     * 
     * GET /api/search/hot?size=10
     */
    @GetMapping("/hot")
    public CommonResponse<List<Map<String, Object>>> getHotSearches(
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<Map<String, Object>> hotGames = searchService.getHotSearches(size);
            return CommonResponse.success(hotGames);
        } catch (Exception e) {
            log.error("获取热门搜索失败", e);
            return CommonResponse.error("获取热门搜索失败：" + e.getMessage());
        }
    }
    
    /**
     * 重建索引（管理员功能）
     * 
     * POST /api/search/rebuild-index
     */
    @PostMapping("/rebuild-index")
    public CommonResponse<String> rebuildIndex() {
        try {
            searchService.rebuildIndex();
            return CommonResponse.success("索引已清空，请调用同步接口重新导入数据");
        } catch (Exception e) {
            log.error("重建索引失败", e);
            return CommonResponse.error("重建索引失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取客户端 IP 地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果是多个 IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
