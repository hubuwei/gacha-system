package com.cheng.mall.controller;

import com.cheng.common.dto.CommonResponse;
import com.cheng.mall.dto.GameDetailDTO;
import com.cheng.mall.entity.Game;
import com.cheng.mall.entity.GameCategory;
import com.cheng.mall.entity.GameTag;
import com.cheng.mall.service.GameService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 游戏控制器
 */
@Slf4j
@RestController
@RequestMapping("/games")
@CrossOrigin(origins = "*")
public class GameController {
    
    @Autowired
    private GameService gameService;
    
    /**
     * 获取游戏列表(分页)
     */
    @GetMapping
    public CommonResponse<Map<String, Object>> getGames(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String order) {
        try {
            Page<Game> games = gameService.getGames(page, size, sortBy, order);
            
            Map<String, Object> result = new HashMap<>();
            result.put("content", games.getContent());
            result.put("totalElements", games.getTotalElements());
            result.put("totalPages", games.getTotalPages());
            result.put("currentPage", games.getNumber());
            result.put("pageSize", games.getSize());
            
            return CommonResponse.success(result);
        } catch (Exception e) {
            log.error("获取游戏列表失败", e);
            return CommonResponse.error("获取游戏列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取所有游戏（包含标签ID，用于前端筛选）
     */
    @GetMapping("/all-with-tags")
    public CommonResponse<List<Map<String, Object>>> getAllGamesWithTags() {
        try {
            List<Map<String, Object>> games = gameService.getGamesWithTags();
            return CommonResponse.success(games);
        } catch (Exception e) {
            log.error("获取游戏列表失败", e);
            return CommonResponse.error("获取游戏列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取精选游戏
     */
    @GetMapping("/featured")
    public CommonResponse<List<Game>> getFeaturedGames() {
        try {
            List<Game> games = gameService.getFeaturedGames();
            return CommonResponse.success(games);
        } catch (Exception e) {
            log.error("获取精选游戏失败", e);
            return CommonResponse.error("获取精选游戏失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取游戏详情
     */
    @GetMapping("/{id}")
    public CommonResponse<GameDetailDTO> getGameDetail(@PathVariable Long id) {
        try {
            GameDetailDTO detail = gameService.getGameDetail(id);
            return CommonResponse.success(detail);
        } catch (Exception e) {
            log.error("获取游戏详情失败", e);
            return CommonResponse.error("获取游戏详情失败：" + e.getMessage());
        }
    }
    
    /**
     * 搜索游戏
     */
    @GetMapping("/search")
    public CommonResponse<List<Game>> searchGames(@RequestParam String keyword) {
        try {
            List<Game> games = gameService.searchGames(keyword);
            return CommonResponse.success(games);
        } catch (Exception e) {
            log.error("搜索游戏失败", e);
            return CommonResponse.error("搜索游戏失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据分类获取游戏
     */
    @GetMapping("/category/{categoryId}")
    public CommonResponse<List<Game>> getGamesByCategory(@PathVariable Integer categoryId) {
        try {
            List<Game> games = gameService.getGamesByCategory(categoryId);
            return CommonResponse.success(games);
        } catch (Exception e) {
            log.error("获取分类游戏失败", e);
            return CommonResponse.error("获取分类游戏失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据标签获取游戏
     */
    @GetMapping("/tag/{tagId}")
    public CommonResponse<List<Map<String, Object>>> getGamesByTag(@PathVariable Integer tagId) {
        try {
            List<Map<String, Object>> games = gameService.getGamesByTag(tagId);
            return CommonResponse.success(games);
        } catch (Exception e) {
            log.error("获取标签游戏失败", e);
            return CommonResponse.error("获取标签游戏失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取所有分类
     */
    @GetMapping("/categories")
    public CommonResponse<List<GameCategory>> getCategories() {
        try {
            List<GameCategory> categories = gameService.getAllCategories();
            return CommonResponse.success(categories);
        } catch (Exception e) {
            log.error("获取分类列表失败", e);
            return CommonResponse.error("获取分类列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取所有标签
     */
    @GetMapping("/tags")
    public CommonResponse<List<GameTag>> getTags() {
        try {
            List<GameTag> tags = gameService.getAllTags();
            return CommonResponse.success(tags);
        } catch (Exception e) {
            log.error("获取标签列表失败", e);
            return CommonResponse.error("获取标签列表失败：" + e.getMessage());
        }
    }
}
