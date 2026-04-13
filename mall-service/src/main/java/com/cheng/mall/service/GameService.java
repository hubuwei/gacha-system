package com.cheng.mall.service;

import com.cheng.mall.dto.GameDetailDTO;
import com.cheng.mall.entity.*;
import com.cheng.mall.repository.*;
import com.cheng.mall.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 游戏服务类
 */
@Slf4j
@Service
public class GameService {
    
    @Autowired
    private GameRepository gameRepository;
    
    @Autowired
    private GameCategoryRepository categoryRepository;
    
    @Autowired
    private GameTagRepository tagRepository;
    
    @Autowired
    private GameCategoryMappingRepository categoryMappingRepository;
    
    @Autowired
    private GameTagMappingRepository tagMappingRepository;
    
    @Autowired
    private GameSystemRequirementRepository systemRequirementRepository;
    
    @Autowired
    private EmailNotificationService emailNotificationService;
    
    @Autowired
    private RedisUtil redisUtil;
    
    // 游戏详情缓存 key 前缀
    private static final String GAME_DETAIL_CACHE_KEY = "game:detail:";
    // 缓存过期时间：30分钟
    private static final long CACHE_EXPIRE_MINUTES = 30;
    
    /**
     * 获取游戏列表(分页)
     */
    public Page<Game> getGames(int page, int size, String sortBy, String order) {
        Sort sort = order.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return gameRepository.findByIsOnSaleTrue(pageable);
    }
    
    /**
     * 获取游戏列表（包含标签ID）
     */
    public List<Map<String, Object>> getGamesWithTags() {
        // 使用Pageable获取所有游戏
        Pageable pageable = PageRequest.of(0, 1000); // 最多1000个游戏
        List<Game> games = gameRepository.findByIsOnSaleTrue(pageable).getContent();
        
        return games.stream().map(game -> {
            Map<String, Object> gameMap = new HashMap<>();
            gameMap.put("id", game.getId());
            gameMap.put("title", game.getTitle());
            gameMap.put("shortDescription", game.getShortDescription());
            gameMap.put("coverImage", game.getCoverImage());
            gameMap.put("currentPrice", game.getCurrentPrice());
            gameMap.put("basePrice", game.getBasePrice());
            gameMap.put("discountRate", game.getDiscountRate());
            gameMap.put("rating", game.getRating());
            gameMap.put("totalSales", game.getTotalSales());
            gameMap.put("totalReviews", game.getTotalReviews());
            gameMap.put("isFeatured", game.getIsFeatured());
            
            // 获取分类
            List<Integer> categoryIds = categoryMappingRepository.findCategoryIdsByGameId(game.getId());
            List<String> categories = categoryIds.stream()
                .map(id -> categoryRepository.findById(id).map(GameCategory::getName).orElse(null))
                .filter(name -> name != null)
                .collect(Collectors.toList());
            gameMap.put("categories", categories);
            if (!categories.isEmpty()) {
                gameMap.put("category", categories.get(0)); // 主分类
            }
            
            // 获取标签
            List<Integer> tagIds = tagMappingRepository.findTagIdsByGameId(game.getId());
            gameMap.put("tagIds", tagIds);
            
            List<GameTag> tags = tagIds.stream()
                .map(id -> tagRepository.findById(id).orElse(null))
                .filter(tag -> tag != null)
                .collect(Collectors.toList());
            gameMap.put("tags", tags.stream().map(GameTag::getName).collect(Collectors.toList()));
            
            return gameMap;
        }).collect(Collectors.toList());
    }
    
    /**
     * 获取精选游戏
     */
    public List<Game> getFeaturedGames() {
        return gameRepository.findByIsFeaturedTrueAndIsOnSaleTrue();
    }
    
    /**
     * 根据ID获取游戏详情（带缓存）
     */
    public GameDetailDTO getGameDetail(Long gameId) {
        // 1. 尝试从缓存获取
        String cacheKey = GAME_DETAIL_CACHE_KEY + gameId;
        Object cached = redisUtil.get(cacheKey);
        if (cached != null) {
            log.debug("从缓存获取游戏详情: {}", gameId);
            return (GameDetailDTO) cached;
        }
        
        log.debug("从数据库获取游戏详情: {}", gameId);
        
        // 2. 缓存未命中，从数据库查询
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("游戏不存在"));
        
        GameDetailDTO dto = buildGameDetailDTO(game, gameId);
        
        // 3. 存入缓存
        redisUtil.set(cacheKey, dto, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        
        return dto;
    }
    
    /**
     * 构建游戏详情 DTO
     */
    private GameDetailDTO buildGameDetailDTO(Game game, Long gameId) {
        
        GameDetailDTO dto = new GameDetailDTO();
        
        // 基本信息
        dto.setId(game.getId());
        dto.setTitle(game.getTitle());
        dto.setShortDescription(game.getShortDescription());
        dto.setFullDescription(game.getFullDescription());
        dto.setCoverImage(game.getCoverImage());
        dto.setBannerImage(game.getBannerImage());
        dto.setScreenshots(game.getScreenshots());
        dto.setTrailerUrl(game.getTrailerUrl());
        dto.setBasePrice(game.getBasePrice());
        dto.setCurrentPrice(game.getCurrentPrice());
        dto.setDiscountRate(game.getDiscountRate());
        dto.setIsFeatured(game.getIsFeatured());
        dto.setIsOnSale(game.getIsOnSale());
        dto.setDeveloper(game.getDeveloper());
        dto.setPublisher(game.getPublisher());
        dto.setRating(game.getRating());
        dto.setRatingCount(game.getRatingCount());
        dto.setTotalSales(game.getTotalSales());
        dto.setTotalReviews(game.getTotalReviews());
        dto.setFileSize(game.getFileSize());
        dto.setVersion(game.getVersion());
        
        // 分类列表
        List<Integer> categoryIds = categoryMappingRepository.findCategoryIdsByGameId(gameId);
        List<String> categories = categoryIds.stream()
                .map(id -> categoryRepository.findById(id).map(GameCategory::getName).orElse(null))
                .filter(name -> name != null)
                .collect(Collectors.toList());
        dto.setCategories(categories);
        
        // 标签列表
        List<Integer> tagIds = tagMappingRepository.findTagIdsByGameId(gameId);
        List<GameDetailDTO.GameTagDTO> tags = tagIds.stream()
                .map(id -> tagRepository.findById(id).map(tag -> 
                    new GameDetailDTO.GameTagDTO(tag.getId(), tag.getName(), tag.getColor())
                ).orElse(null))
                .filter(tag -> tag != null)
                .collect(Collectors.toList());
        dto.setTags(tags);
        
        // 系统配置要求
        systemRequirementRepository.findByGameId(gameId).ifPresent(req -> {
            GameDetailDTO.GameSystemRequirementDTO sysReq = new GameDetailDTO.GameSystemRequirementDTO();
            sysReq.setOsMin(req.getOsMin());
            sysReq.setOsRecommended(req.getOsRecommended());
            sysReq.setCpuMin(req.getCpuMin());
            sysReq.setCpuRecommended(req.getCpuRecommended());
            sysReq.setRamMin(req.getRamMin());
            sysReq.setRamRecommended(req.getRamRecommended());
            sysReq.setGpuMin(req.getGpuMin());
            sysReq.setGpuRecommended(req.getGpuRecommended());
            sysReq.setDirectxMin(req.getDirectxMin());
            sysReq.setDirectxRecommended(req.getDirectxRecommended());
            sysReq.setStorageMin(req.getStorageMin());
            sysReq.setStorageRecommended(req.getStorageRecommended());
            sysReq.setNetworkMin(req.getNetworkMin());
            sysReq.setNetworkRecommended(req.getNetworkRecommended());
            sysReq.setSoundCardMin(req.getSoundCardMin());
            sysReq.setSoundCardRecommended(req.getSoundCardRecommended());
            sysReq.setAdditionalNotes(req.getAdditionalNotes());
            dto.setSystemRequirements(sysReq);
        });
        
        return dto;
    }
    
    /**
     * 搜索游戏
     */
    public List<Game> searchGames(String keyword) {
        return gameRepository.searchByTitle(keyword);
    }
    
    /**
     * 根据分类获取游戏
     */
    public List<Game> getGamesByCategory(Integer categoryId) {
        return gameRepository.findByCategoryId(categoryId);
    }
    
    /**
     * 根据标签获取游戏
     */
    public List<Map<String, Object>> getGamesByTag(Integer tagId) {
        // 通过标签ID获取游戏ID列表
        List<Long> gameIds = tagMappingRepository.findGameIdsByTagId(tagId);
        
        if (gameIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 获取游戏详情
        List<Game> games = gameRepository.findAllById(gameIds);
        
        return games.stream().map(game -> {
            Map<String, Object> gameMap = new HashMap<>();
            gameMap.put("id", game.getId());
            gameMap.put("title", game.getTitle());
            gameMap.put("shortDescription", game.getShortDescription());
            gameMap.put("coverImage", game.getCoverImage());
            gameMap.put("currentPrice", game.getCurrentPrice());
            gameMap.put("basePrice", game.getBasePrice());
            gameMap.put("discountRate", game.getDiscountRate());
            gameMap.put("rating", game.getRating());
            gameMap.put("isFeatured", game.getIsFeatured());
            
            // 获取分类
            List<Integer> categoryIds = categoryMappingRepository.findCategoryIdsByGameId(game.getId());
            List<String> categories = categoryIds.stream()
                .map(id -> categoryRepository.findById(id).map(GameCategory::getName).orElse(null))
                .filter(name -> name != null)
                .collect(Collectors.toList());
            gameMap.put("categories", categories);
            if (!categories.isEmpty()) {
                gameMap.put("category", categories.get(0));
            }
            
            // 获取标签
            List<Integer> tagIds = tagMappingRepository.findTagIdsByGameId(game.getId());
            gameMap.put("tagIds", tagIds);
            
            List<GameTag> tags = tagIds.stream()
                .map(id -> tagRepository.findById(id).orElse(null))
                .filter(tag -> tag != null)
                .collect(Collectors.toList());
            gameMap.put("tags", tags.stream().map(GameTag::getName).collect(Collectors.toList()));
            
            return gameMap;
        }).collect(Collectors.toList());
    }
    
    /**
     * 获取所有分类
     */
    public List<GameCategory> getAllCategories() {
        return categoryRepository.findByIsActiveTrueOrderBySortOrderAsc();
    }
    
    /**
     * 获取所有标签
     */
    public List<GameTag> getAllTags() {
        return tagRepository.findAllByOrderBySortOrderAsc();
    }
    
    /**
     * 更新游戏折扣（触发邮件通知）
     */
    @Transactional
    public void updateGameDiscount(Long gameId, int discountRate, java.math.BigDecimal currentPrice) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("游戏不存在"));
        
        java.math.BigDecimal oldPrice = game.getCurrentPrice();
        int oldDiscount = game.getDiscountRate();
        
        // 更新折扣
        game.setDiscountRate(discountRate);
        game.setCurrentPrice(currentPrice);
        game.setIsOnSale(discountRate > 0);
        gameRepository.save(game);
        
        // 如果折扣率增加，发送通知
        if (discountRate > oldDiscount && discountRate > 0) {
            emailNotificationService.sendDiscountNotification(
                gameId, oldPrice, currentPrice, discountRate
            );
        }
        
        // 清除缓存
        String cacheKey = GAME_DETAIL_CACHE_KEY + gameId;
        redisUtil.delete(cacheKey);
        log.info("已清除游戏详情缓存: {}", gameId);
    }
}
