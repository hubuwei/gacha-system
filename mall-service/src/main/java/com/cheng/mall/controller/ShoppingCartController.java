package com.cheng.mall.controller;

import com.cheng.common.dto.CommonResponse;
import com.cheng.mall.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 购物车控制器
 */
@Slf4j
@RestController
@RequestMapping("/cart")
@CrossOrigin(origins = "*")
public class ShoppingCartController {
    
    @Autowired
    private ShoppingCartService cartService;
    
    /**
     * 获取用户购物车
     */
    @GetMapping
    public CommonResponse<List<Map<String, Object>>> getCart(@RequestParam Long userId) {
        try {
            List<Map<String, Object>> cart = cartService.getUserCart(userId);
            return CommonResponse.success(cart);
        } catch (Exception e) {
            log.error("获取购物车失败", e);
            return CommonResponse.error("获取购物车失败：" + e.getMessage());
        }
    }
    
    /**
     * 添加到购物车
     */
    @PostMapping
    public CommonResponse<?> addToCart(
            @RequestParam Long userId,
            @RequestParam Long gameId) {
        try {
            cartService.addToCart(userId, gameId);
            return CommonResponse.success("添加成功");
        } catch (IllegalArgumentException e) {
            return CommonResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("添加到购物车失败", e);
            return CommonResponse.error("添加失败：" + e.getMessage());
        }
    }
    
    /**
     * 从购物车移除
     */
    @DeleteMapping("/{gameId}")
    public CommonResponse<Void> removeFromCart(
            @PathVariable Long gameId,
            @RequestParam Long userId) {
        try {
            cartService.removeFromCart(userId, gameId);
            return CommonResponse.success(null);
        } catch (Exception e) {
            log.error("移除购物车项失败", e);
            return CommonResponse.error("移除失败：" + e.getMessage());
        }
    }
    
    /**
     * 更新选中状态
     */
    @PutMapping("/{gameId}/check")
    public CommonResponse<Void> updateChecked(
            @PathVariable Long gameId,
            @RequestParam Long userId,
            @RequestParam boolean checked) {
        try {
            cartService.updateChecked(userId, gameId, checked);
            return CommonResponse.success(null);
        } catch (Exception e) {
            log.error("更新选中状态失败", e);
            return CommonResponse.error("更新失败：" + e.getMessage());
        }
    }
    
    /**
     * 清空购物车
     */
    @DeleteMapping
    public CommonResponse<Void> clearCart(@RequestParam Long userId) {
        try {
            cartService.clearCart(userId);
            return CommonResponse.success(null);
        } catch (Exception e) {
            log.error("清空购物车失败", e);
            return CommonResponse.error("清空失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取购物车数量
     */
    @GetMapping("/count")
    public CommonResponse<Map<String, Object>> getCartCount(@RequestParam Long userId) {
        try {
            long count = cartService.getCartCount(userId);
            Map<String, Object> result = Map.of("count", count);
            return CommonResponse.success(result);
        } catch (Exception e) {
            log.error("获取购物车数量失败", e);
            return CommonResponse.error("获取失败：" + e.getMessage());
        }
    }
}
