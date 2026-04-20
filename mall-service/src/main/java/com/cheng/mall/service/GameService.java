package com.cheng.mall.service;

import com.cheng.mall.dto.GameDetailDTO;
import com.cheng.mall.entity.*;
import com.cheng.mall.es.document.GameDocument;
import com.cheng.mall.es.repository.GameEsRepository;
import com.cheng.mall.repository.*;
import com.cheng.mall.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
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
    
    @Autowired(required = false)
    private RedisUtil redisUtil;
    
    @Autowired(required = false)
    private GameEsRepository gameEsRepository;
    
    @Autowired(required = false)
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
    
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
        if (redisUtil != null) {
            Object cached = redisUtil.get(cacheKey);
            if (cached != null) {
                log.debug("从缓存获取游戏详情: {}", gameId);
                return (GameDetailDTO) cached;
            }
        }
        
        log.debug("从数据库获取游戏详情: {}", gameId);
        
        // 2. 缓存未命中，从数据库查询
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("游戏不存在"));
        
        GameDetailDTO dto = buildGameDetailDTO(game, gameId);
        
        // 3. 存入缓存
        if (redisUtil != null) {
            redisUtil.set(cacheKey, dto, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        }
        
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
        // 首先尝试使用Elasticsearch搜索
        if (elasticsearchRestTemplate != null) {
            try {
                Criteria criteria = new Criteria()
                    .or(Criteria.where("title").contains(keyword))
                    .or(Criteria.where("shortDescription").contains(keyword))
                    .or(Criteria.where("fullDescription").contains(keyword))
                    .or(Criteria.where("developer").contains(keyword))
                    .or(Criteria.where("publisher").contains(keyword))
                    .or(Criteria.where("tags").contains(keyword))
                    .or(Criteria.where("categories").contains(keyword))
                    .or(Criteria.where("titlePinyin").contains(keyword))
                    .or(Criteria.where("descriptionPinyin").contains(keyword));
                
                CriteriaQuery query = new CriteriaQuery(criteria);
                query.setPageable(PageRequest.of(0, 100));
                
                SearchHits<GameDocument> searchHits = elasticsearchRestTemplate.search(query, GameDocument.class);
                
                List<Long> gameIds = searchHits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .map(GameDocument::getId)
                    .collect(java.util.stream.Collectors.toList());
                
                if (!gameIds.isEmpty()) {
                    return gameRepository.findAllById(gameIds);
                }
            } catch (Exception e) {
                log.error("Elasticsearch搜索失败，回退到数据库搜索", e);
            }
        }
        
        // 回退到数据库搜索
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
     * 同步游戏到Elasticsearch
     */
    public void syncGameToEs(Long gameId) {
        if (gameEsRepository != null) {
            try {
                Game game = gameRepository.findById(gameId)
                        .orElseThrow(() -> new RuntimeException("游戏不存在: " + gameId));
                
                // 转换为GameDocument
                GameDocument document = new GameDocument();
                document.setId(game.getId());
                document.setTitle(game.getTitle());
                document.setShortDescription(game.getShortDescription());
                document.setFullDescription(game.getFullDescription());
                document.setDeveloper(game.getDeveloper());
                document.setPublisher(game.getPublisher());
                document.setCoverImage(game.getCoverImage());
                document.setBasePrice(game.getBasePrice() != null ? game.getBasePrice().doubleValue() : 0.0);
                document.setCurrentPrice(game.getCurrentPrice() != null ? game.getCurrentPrice().doubleValue() : 0.0);
                document.setDiscountRate(game.getDiscountRate());
                document.setRating(game.getRating() != null ? game.getRating().doubleValue() : 0.0);
                document.setRatingCount(game.getRatingCount());
                document.setTotalSales(game.getTotalSales());
                document.setTotalReviews(game.getTotalReviews());
                document.setIsFeatured(game.getIsFeatured());
                document.setIsOnSale(game.getIsOnSale());
                document.setReleaseDate(game.getReleaseDate() != null ? game.getReleaseDate().atStartOfDay() : null);
                document.setUpdatedAt(game.getUpdatedAt());
                
                // 获取分类列表
                List<Integer> categoryIds = categoryMappingRepository.findCategoryIdsByGameId(game.getId());
                List<String> categories = categoryIds.stream()
                    .map(id -> categoryRepository.findById(id).map(GameCategory::getName).orElse(null))
                    .filter(name -> name != null)
                    .collect(java.util.stream.Collectors.toList());
                document.setCategories(categories);
                
                // 获取标签列表
                List<Integer> tagIds = tagMappingRepository.findTagIdsByGameId(game.getId());
                List<String> tags = tagIds.stream()
                    .map(id -> tagRepository.findById(id).map(GameTag::getName).orElse(null))
                    .filter(name -> name != null)
                    .collect(java.util.stream.Collectors.toList());
                document.setTags(tags);
                
                // 保存到ES
                gameEsRepository.save(document);
                log.info("游戏同步到ES成功: {} - {}", gameId, game.getTitle());
            } catch (Exception e) {
                log.error("游戏同步到ES失败: {}", gameId, e);
            }
        }
    }
    
    /**
     * 全量同步所有游戏到Elasticsearch
     */
    public void syncAllGamesToEs() {
        if (gameEsRepository != null) {
            try {
                List<Game> games = gameRepository.findAll();
                List<GameDocument> documents = new ArrayList<>();
                
                for (Game game : games) {
                    try {
                        GameDocument document = new GameDocument();
                        document.setId(game.getId());
                        document.setTitle(game.getTitle());
                        document.setShortDescription(game.getShortDescription());
                        document.setFullDescription(game.getFullDescription());
                        document.setDeveloper(game.getDeveloper());
                        document.setPublisher(game.getPublisher());
                        document.setCoverImage(game.getCoverImage());
                        document.setBasePrice(game.getBasePrice() != null ? game.getBasePrice().doubleValue() : 0.0);
                        document.setCurrentPrice(game.getCurrentPrice() != null ? game.getCurrentPrice().doubleValue() : 0.0);
                        document.setDiscountRate(game.getDiscountRate());
                        document.setRating(game.getRating() != null ? game.getRating().doubleValue() : 0.0);
                        document.setRatingCount(game.getRatingCount());
                        document.setTotalSales(game.getTotalSales());
                        document.setTotalReviews(game.getTotalReviews());
                        document.setIsFeatured(game.getIsFeatured());
                        document.setIsOnSale(game.getIsOnSale());
                        document.setReleaseDate(game.getReleaseDate() != null ? game.getReleaseDate().atStartOfDay() : null);
                        document.setUpdatedAt(game.getUpdatedAt());
                        
                        // 获取分类列表
                        List<Integer> categoryIds = categoryMappingRepository.findCategoryIdsByGameId(game.getId());
                        List<String> categories = categoryIds.stream()
                            .map(id -> categoryRepository.findById(id).map(GameCategory::getName).orElse(null))
                            .filter(name -> name != null)
                            .collect(java.util.stream.Collectors.toList());
                        document.setCategories(categories);
                        
                        // 获取标签列表
                        List<Integer> tagIds = tagMappingRepository.findTagIdsByGameId(game.getId());
                        List<String> tags = tagIds.stream()
                            .map(id -> tagRepository.findById(id).map(GameTag::getName).orElse(null))
                            .filter(name -> name != null)
                            .collect(java.util.stream.Collectors.toList());
                        document.setTags(tags);
                        
                        documents.add(document);
                    } catch (Exception e) {
                        log.error("转换游戏数据失败: {}", game.getId(), e);
                    }
                }
                
                if (!documents.isEmpty()) {
                    gameEsRepository.saveAll(documents);
                    log.info("全量同步游戏到ES完成，共同步 {} 个游戏", documents.size());
                } else {
                    log.warn("没有游戏数据需要同步到ES");
                }
            } catch (Exception e) {
                log.error("全量同步游戏到ES失败", e);
            }
        }
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
        
        // 同步到ES
        syncGameToEs(gameId);
        
        // 如果折扣率增加，发送通知
        if (discountRate > oldDiscount && discountRate > 0) {
            emailNotificationService.sendDiscountNotification(
                gameId, oldPrice, currentPrice, discountRate
            );
        }
        
        // 清除缓存
        if (redisUtil != null) {
            String cacheKey = GAME_DETAIL_CACHE_KEY + gameId;
            redisUtil.delete(cacheKey);
            log.info("已清除游戏详情缓存: {}", gameId);
        }
    }
}
