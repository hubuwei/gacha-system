package com.cheng.mall.mq.consumer;

import com.cheng.mall.entity.Game;
import com.cheng.mall.es.document.GameDocument;
import com.cheng.mall.es.repository.GameEsRepository;
import com.cheng.mall.repository.GameRepository;
import com.cheng.mall.util.RedisUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

import static com.cheng.mall.config.RabbitMQConfig.*;

/**
 * 游戏事件消费者
 */
@Slf4j
@Component
public class GameEventConsumer {
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private GameRepository gameRepository;
    
    @Autowired
    private GameEsRepository gameEsRepository;
    
    @Autowired
    private RedisUtil redisUtil;
    
    /**
     * 处理 ES 索引更新消息
     */
    @RabbitListener(queues = GAME_ES_QUEUE)
    public void handleEsUpdate(String message) {
        try {
            Map<String, Object> eventData = objectMapper.readValue(message, Map.class);
            
            Long gameId = ((Number) eventData.get("gameId")).longValue();
            String eventType = (String) eventData.get("eventType");
            String gameTitle = (String) eventData.get("gameTitle");
            
            log.info("收到 ES 索引更新消息: gameId={}, eventType={}", gameId, eventType);
            
            if ("on".equals(eventType)) {
                // 游戏上架：添加到 ES 索引
                syncGameToEs(gameId);
            } else if ("off".equals(eventType)) {
                // 游戏下架：从 ES 索引删除
                gameEsRepository.deleteById(gameId);
                log.info("已从 ES 删除游戏: gameId={}", gameId);
            }
            
        } catch (Exception e) {
            log.error("处理 ES 索引更新消息失败: {}", message, e);
        }
    }
    
    /**
     * 处理缓存清除消息
     */
    @RabbitListener(queues = GAME_CACHE_QUEUE)
    public void handleCacheClear(String message) {
        try {
            Map<String, Object> eventData = objectMapper.readValue(message, Map.class);
            
            Long gameId = ((Number) eventData.get("gameId")).longValue();
            String eventType = (String) eventData.get("eventType");
            
            log.info("收到缓存清除消息: gameId={}, eventType={}", gameId, eventType);
            
            // 清除游戏详情缓存
            String cacheKey = "game:detail:" + gameId;
            redisUtil.delete(cacheKey);
            
            log.info("已清除游戏缓存: cacheKey={}", cacheKey);
            
        } catch (Exception e) {
            log.error("处理缓存清除消息失败: {}", message, e);
        }
    }
    
    /**
     * 同步游戏到 ES
     */
    private void syncGameToEs(Long gameId) {
        try {
            Game game = gameRepository.findById(gameId).orElse(null);
            if (game == null) {
                log.warn("游戏不存在: gameId={}", gameId);
                return;
            }
            
            GameDocument document = convertToDocument(game);
            gameEsRepository.save(document);
            
            log.info("已同步游戏到 ES: gameId={}, title={}", gameId, game.getTitle());
        } catch (Exception e) {
            log.error("同步游戏到 ES 失败: gameId={}", gameId, e);
        }
    }
    
    /**
     * 转换 Game 实体为 GameDocument
     */
    private GameDocument convertToDocument(Game game) {
        GameDocument doc = new GameDocument();
        doc.setId(game.getId());
        doc.setTitle(game.getTitle());
        doc.setShortDescription(game.getShortDescription());
        doc.setFullDescription(game.getFullDescription());
        doc.setCoverImage(game.getCoverImage());
        doc.setBasePrice(game.getBasePrice() != null ? game.getBasePrice().doubleValue() : 0.0);
        doc.setCurrentPrice(game.getCurrentPrice() != null ? game.getCurrentPrice().doubleValue() : 0.0);
        doc.setDiscountRate(game.getDiscountRate());
        doc.setRating(game.getRating() != null ? game.getRating().doubleValue() : 0.0);
        doc.setTotalSales(game.getTotalSales());
        doc.setIsOnSale(game.getIsOnSale());
        doc.setUpdatedAt(java.time.LocalDateTime.now());
        
        // TODO: 添加分类和标签
        // 这里可以查询数据库获取分类和标签
        
        return doc;
    }
}
