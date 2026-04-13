package com.cheng.mall.service;

import com.cheng.mall.entity.Game;
import com.cheng.mall.entity.ShoppingCart;
import com.cheng.mall.repository.GameRepository;
import com.cheng.mall.repository.ShoppingCartRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 购物车服务
 */
@Slf4j
@Service
public class ShoppingCartService {
    
    @Autowired
    private ShoppingCartRepository cartRepository;
    
    @Autowired
    private GameRepository gameRepository;
    
    /**
     * 获取用户购物车列表（包含游戏信息）
     */
    public List<Map<String, Object>> getUserCart(Long userId) {
        List<ShoppingCart> cartItems = cartRepository.findByUserIdOrderByAddedAtDesc(userId);
        
        return cartItems.stream().map(item -> {
            Game game = gameRepository.findById(item.getGameId()).orElse(null);
            
            Map<String, Object> cartItem = new HashMap<>();
            cartItem.put("cartId", item.getId());
            cartItem.put("gameId", item.getGameId());
            cartItem.put("quantity", item.getQuantity());
            cartItem.put("checked", item.getChecked());
            cartItem.put("addedAt", item.getAddedAt());
            
            if (game != null) {
                Map<String, Object> gameInfo = new HashMap<>();
                gameInfo.put("id", game.getId());
                gameInfo.put("title", game.getTitle());
                gameInfo.put("coverImage", game.getCoverImage());
                gameInfo.put("currentPrice", game.getCurrentPrice());
                gameInfo.put("basePrice", game.getBasePrice());
                gameInfo.put("discountRate", game.getDiscountRate());
                cartItem.put("game", gameInfo);
            } else {
                cartItem.put("game", null);
            }
            
            return cartItem;
        }).collect(Collectors.toList());
    }
    
    /**
     * 添加到购物车
     */
    @Transactional
    public ShoppingCart addToCart(Long userId, Long gameId) {
        // 检查是否已存在
        var existing = cartRepository.findByUserIdAndGameId(userId, gameId);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("该游戏已在购物车中");
        }
        
        // 验证游戏是否存在
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("游戏不存在"));
        
        // 创建购物车项
        ShoppingCart cartItem = new ShoppingCart();
        cartItem.setUserId(userId);
        cartItem.setGameId(gameId);
        cartItem.setQuantity(1);
        cartItem.setChecked(true);
        
        return cartRepository.save(cartItem);
    }
    
    /**
     * 从购物车移除
     */
    @Transactional
    public void removeFromCart(Long userId, Long gameId) {
        cartRepository.deleteByUserIdAndGameId(userId, gameId);
    }
    
    /**
     * 更新购物车项选中状态
     */
    @Transactional
    public void updateChecked(Long userId, Long gameId, boolean checked) {
        var cartItem = cartRepository.findByUserIdAndGameId(userId, gameId)
            .orElseThrow(() -> new IllegalArgumentException("购物车项不存在"));
        
        cartItem.setChecked(checked);
        cartRepository.save(cartItem);
    }
    
    /**
     * 清空购物车
     */
    @Transactional
    public void clearCart(Long userId) {
        List<ShoppingCart> items = cartRepository.findByUserIdOrderByAddedAtDesc(userId);
        cartRepository.deleteAll(items);
    }
    
    /**
     * 获取购物车数量
     */
    public long getCartCount(Long userId) {
        return cartRepository.countByUserId(userId);
    }
}
