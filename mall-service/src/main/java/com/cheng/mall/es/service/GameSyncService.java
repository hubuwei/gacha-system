package com.cheng.mall.es.service;

import com.cheng.mall.entity.*;
import com.cheng.mall.es.document.GameDocument;
import com.cheng.mall.es.repository.GameEsRepository;
import com.cheng.mall.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 游戏数据同步服务
 * 负责将 MySQL 数据同步到 Elasticsearch
 */
@Slf4j
@Service
public class GameSyncService {
    
    @Autowired
    private GameRepository gameRepository;
    
    @Autowired
    private GameCategoryMappingRepository categoryMappingRepository;
    
    @Autowired
    private GameTagMappingRepository tagMappingRepository;
    
    @Autowired
    private GameCategoryRepository categoryRepository;
    
    @Autowired
    private GameTagRepository tagRepository;
    
    @Autowired
    private GameEsRepository gameEsRepository;
    
    /**
     * 全量同步所有游戏到 Elasticsearch
     */
    @Transactional(readOnly = true)
    public void syncAllGames() {
        log.info("开始全量同步游戏数据到 Elasticsearch...");
        
        List<Game> games = gameRepository.findAll();
        List<GameDocument> documents = new ArrayList<>();
        
        for (Game game : games) {
            try {
                GameDocument document = convertToDocument(game);
                documents.add(document);
            } catch (Exception e) {
                log.error("转换游戏数据失败: {}", game.getId(), e);
            }
        }
        
        // 批量索引
        if (!documents.isEmpty()) {
            gameEsRepository.saveAll(documents);
            log.info("全量同步完成，共同步 {} 个游戏", documents.size());
        } else {
            log.warn("没有游戏数据需要同步");
        }
    }
    
    /**
     * 增量同步单个游戏
     */
    @Async
    @Transactional(readOnly = true)
    public void syncGame(Long gameId) {
        try {
            Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("游戏不存在: " + gameId));
            
            GameDocument document = convertToDocument(game);
            gameEsRepository.save(document);
            
            log.info("增量同步游戏成功: {} - {}", gameId, game.getTitle());
        } catch (Exception e) {
            log.error("增量同步游戏失败: {}", gameId, e);
        }
    }
    
    /**
     * 删除游戏索引
     */
    @Async
    public void deleteGameIndex(Long gameId) {
        try {
            gameEsRepository.deleteById(gameId);
            log.info("删除游戏索引成功: {}", gameId);
        } catch (Exception e) {
            log.error("删除游戏索引失败: {}", gameId, e);
        }
    }
    
    /**
     * 将 Game 实体转换为 GameDocument
     */
    private GameDocument convertToDocument(Game game) {
        GameDocument doc = new GameDocument();
        
        // 基本信息
        doc.setId(game.getId());
        doc.setTitle(game.getTitle());
        doc.setShortDescription(game.getShortDescription());
        doc.setFullDescription(game.getFullDescription());
        doc.setDeveloper(game.getDeveloper());
        doc.setPublisher(game.getPublisher());
        doc.setCoverImage(game.getCoverImage());
        
        // 价格信息
        doc.setBasePrice(game.getBasePrice() != null ? game.getBasePrice().doubleValue() : 0.0);
        doc.setCurrentPrice(game.getCurrentPrice() != null ? game.getCurrentPrice().doubleValue() : 0.0);
        doc.setDiscountRate(game.getDiscountRate());
        
        // 评分和销量
        doc.setRating(game.getRating() != null ? game.getRating().doubleValue() : 0.0);
        doc.setRatingCount(game.getRatingCount());
        doc.setTotalSales(game.getTotalSales());
        doc.setTotalReviews(game.getTotalReviews());
        
        // 状态
        doc.setIsFeatured(game.getIsFeatured());
        doc.setIsOnSale(game.getIsOnSale());
        
        // 时间
        doc.setReleaseDate(game.getReleaseDate() != null ? 
            game.getReleaseDate().atStartOfDay() : null);
        doc.setUpdatedAt(game.getUpdatedAt());
        
        // 获取分类列表
        List<Integer> categoryIds = categoryMappingRepository.findCategoryIdsByGameId(game.getId());
        List<String> categories = categoryIds.stream()
            .map(id -> categoryRepository.findById(id).map(GameCategory::getName).orElse(null))
            .filter(name -> name != null)
            .collect(Collectors.toList());
        doc.setCategories(categories);
        
        // 获取标签列表
        List<Integer> tagIds = tagMappingRepository.findTagIdsByGameId(game.getId());
        List<String> tags = tagIds.stream()
            .map(id -> tagRepository.findById(id).map(GameTag::getName).orElse(null))
            .filter(name -> name != null)
            .collect(Collectors.toList());
        doc.setTags(tags);
        
        // 生成拼音字段（用于拼音搜索）
        doc.setTitlePinyin(generatePinyin(game.getTitle()));
        doc.setDescriptionPinyin(generatePinyin(game.getShortDescription()));
        
        return doc;
    }
    
    /**
     * 生成拼音（简化版，实际生产环境建议使用 pinyin4j 库）
     */
    private String generatePinyin(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        // 这里只是简单返回原文
        // 实际项目中应该使用 pinyin4j 或类似库进行转换
        // 例如：侠盗猎车手 -> xiadaolieshoushou
        return text;
    }
}
