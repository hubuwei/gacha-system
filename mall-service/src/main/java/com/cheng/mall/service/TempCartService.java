package com.cheng.mall.service;

import com.cheng.mall.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 临时购物车服务（未登录用户使用 Redis 存储）
 */
@Slf4j
@Service
public class TempCartService {
    
    @Autowired
    private RedisUtil redisUtil;
    
    // 临时购物车 key 前缀
    private static final String TEMP_CART_KEY_PREFIX = "temp:cart:";
    // 临时购物车过期时间：7天
    private static final long TEMP_CART_EXPIRE_DAYS = 7;
    
    /**
     * 生成临时购物车 ID（用于未登录用户）
     * 
     * @return 临时购物车 ID
     */
    public String generateTempCartId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 添加商品到临时购物车
     * 
     * @param tempCartId 临时购物车 ID
     * @param gameId 游戏ID
     * @param quantity 数量
     */
    public void addToTempCart(String tempCartId, Long gameId, int quantity) {
        String cartKey = TEMP_CART_KEY_PREFIX + tempCartId;
        
        // 使用 Hash 存储，field 为 gameId，value 为 quantity
        redisUtil.hSet(cartKey, String.valueOf(gameId), quantity);
        
        // 设置过期时间
        redisUtil.expire(cartKey, TEMP_CART_EXPIRE_DAYS, TimeUnit.DAYS);
        
        log.info("添加到临时购物车: tempCartId={}, gameId={}, quantity={}", tempCartId, gameId, quantity);
    }
    
    /**
     * 从临时购物车移除商品
     * 
     * @param tempCartId 临时购物车 ID
     * @param gameId 游戏ID
     */
    public void removeFromTempCart(String tempCartId, Long gameId) {
        String cartKey = TEMP_CART_KEY_PREFIX + tempCartId;
        redisUtil.hDelete(cartKey, String.valueOf(gameId));
        
        log.info("从临时购物车移除: tempCartId={}, gameId={}", tempCartId, gameId);
    }
    
    /**
     * 获取临时购物车所有商品
     * 
     * @param tempCartId 临时购物车 ID
     * @return 商品 Map (gameId -> quantity)
     */
    public Map<Object, Object> getTempCart(String tempCartId) {
        String cartKey = TEMP_CART_KEY_PREFIX + tempCartId;
        Map<Object, Object> cartItems = redisUtil.hGetAll(cartKey);
        
        if (cartItems == null) {
            return new HashMap<>();
        }
        
        return cartItems;
    }
    
    /**
     * 更新临时购物车商品数量
     * 
     * @param tempCartId 临时购物车 ID
     * @param gameId 游戏ID
     * @param quantity 数量
     */
    public void updateTempCartQuantity(String tempCartId, Long gameId, int quantity) {
        if (quantity <= 0) {
            removeFromTempCart(tempCartId, gameId);
            return;
        }
        
        String cartKey = TEMP_CART_KEY_PREFIX + tempCartId;
        redisUtil.hSet(cartKey, String.valueOf(gameId), quantity);
        
        log.info("更新临时购物车数量: tempCartId={}, gameId={}, quantity={}", tempCartId, gameId, quantity);
    }
    
    /**
     * 清空临时购物车
     * 
     * @param tempCartId 临时购物车 ID
     */
    public void clearTempCart(String tempCartId) {
        String cartKey = TEMP_CART_KEY_PREFIX + tempCartId;
        redisUtil.delete(cartKey);
        
        log.info("清空临时购物车: tempCartId={}", tempCartId);
    }
    
    /**
     * 合并临时购物车到用户购物车（登录后）
     * 
     * @param tempCartId 临时购物车 ID
     * @param userId 用户ID
     * @return 合并的商品列表
     */
    public List<Map<String, Object>> mergeTempCartToUser(String tempCartId, Long userId) {
        Map<Object, Object> tempCartItems = getTempCart(tempCartId);
        
        if (tempCartItems == null || tempCartItems.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<Map<String, Object>> mergedItems = new ArrayList<>();
        
        for (Map.Entry<Object, Object> entry : tempCartItems.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("gameId", Long.valueOf(entry.getKey().toString()));
            item.put("quantity", Integer.valueOf(entry.getValue().toString()));
            item.put("userId", userId);
            mergedItems.add(item);
        }
        
        // 清空临时购物车
        clearTempCart(tempCartId);
        
        log.info("合并临时购物车到用户: tempCartId={}, userId={}, itemCount={}", 
            tempCartId, userId, mergedItems.size());
        
        return mergedItems;
    }
    
    /**
     * 获取临时购物车商品数量
     * 
     * @param tempCartId 临时购物车 ID
     * @return 商品种类数
     */
    public int getTempCartCount(String tempCartId) {
        String cartKey = TEMP_CART_KEY_PREFIX + tempCartId;
        Map<Object, Object> cartItems = redisUtil.hGetAll(cartKey);
        
        return cartItems != null ? cartItems.size() : 0;
    }
}
