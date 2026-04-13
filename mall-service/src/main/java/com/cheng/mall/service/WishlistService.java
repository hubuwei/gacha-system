package com.cheng.mall.service;

import com.cheng.mall.entity.Game;
import com.cheng.mall.entity.Wishlist;
import com.cheng.mall.repository.GameRepository;
import com.cheng.mall.repository.WishlistRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 愿望单服务
 */
@Slf4j
@Service
public class WishlistService {
    
    @Autowired
    private WishlistRepository wishlistRepository;
    
    @Autowired
    private GameRepository gameRepository;
    
    /**
     * 获取用户愿望单列表（包含游戏信息）
     */
    public List<Map<String, Object>> getUserWishlist(Long userId) {
        List<Wishlist> wishlists = wishlistRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        return wishlists.stream().map(item -> {
            Game game = gameRepository.findById(item.getGameId()).orElse(null);
            
            Map<String, Object> wishlistItem = new HashMap<>();
            wishlistItem.put("wishlistId", item.getId());
            wishlistItem.put("gameId", item.getGameId());
            wishlistItem.put("priority", item.getPriority());
            wishlistItem.put("notifyDiscount", item.getNotifyDiscount());
            wishlistItem.put("notifyRelease", item.getNotifyRelease());
            wishlistItem.put("createdAt", item.getCreatedAt());
            
            if (game != null) {
                Map<String, Object> gameInfo = new HashMap<>();
                gameInfo.put("id", game.getId());
                gameInfo.put("title", game.getTitle());
                gameInfo.put("coverImage", game.getCoverImage());
                gameInfo.put("currentPrice", game.getCurrentPrice());
                gameInfo.put("basePrice", game.getBasePrice());
                gameInfo.put("discountRate", game.getDiscountRate());
                wishlistItem.put("game", gameInfo);
            } else {
                wishlistItem.put("game", null);
            }
            
            return wishlistItem;
        }).collect(Collectors.toList());
    }
    
    /**
     * 添加到愿望单
     */
    @Transactional
    public Wishlist addToWishlist(Long userId, Long gameId, Integer priority, Boolean notifyDiscount) {
        // 检查是否已存在
        var existing = wishlistRepository.findByUserIdAndGameId(userId, gameId);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("该游戏已在愿望单中");
        }
        
        // 验证游戏是否存在
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("游戏不存在"));
        
        // 创建愿望单项
        Wishlist wishlist = new Wishlist();
        wishlist.setUserId(userId);
        wishlist.setGameId(gameId);
        wishlist.setPriority(priority != null ? priority : 1);
        wishlist.setNotifyDiscount(notifyDiscount != null ? notifyDiscount : true);
        wishlist.setNotifyRelease(true);
        
        return wishlistRepository.save(wishlist);
    }
    
    /**
     * 从愿望单移除
     */
    @Transactional
    public void removeFromWishlist(Long userId, Long gameId) {
        wishlistRepository.deleteByUserIdAndGameId(userId, gameId);
    }
    
    /**
     * 更新通知设置
     */
    @Transactional
    public void updateNotification(Long userId, Long gameId, Boolean notifyDiscount, Boolean notifyRelease) {
        var wishlist = wishlistRepository.findByUserIdAndGameId(userId, gameId)
            .orElseThrow(() -> new IllegalArgumentException("愿望单项不存在"));
        
        if (notifyDiscount != null) {
            wishlist.setNotifyDiscount(notifyDiscount);
        }
        if (notifyRelease != null) {
            wishlist.setNotifyRelease(notifyRelease);
        }
        
        wishlistRepository.save(wishlist);
    }
    
    /**
     * 清空愿望单
     */
    @Transactional
    public void clearWishlist(Long userId) {
        List<Wishlist> items = wishlistRepository.findByUserIdOrderByCreatedAtDesc(userId);
        wishlistRepository.deleteAll(items);
    }
    
    /**
     * 获取愿望单数量
     */
    public long getWishlistCount(Long userId) {
        return wishlistRepository.countByUserId(userId);
    }
}
