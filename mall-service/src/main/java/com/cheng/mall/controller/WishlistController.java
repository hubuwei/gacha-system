package com.cheng.mall.controller;

import com.cheng.common.dto.CommonResponse;
import com.cheng.mall.service.WishlistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 愿望单控制器
 */
@Slf4j
@RestController
@RequestMapping("/wishlist")
@CrossOrigin(origins = "*")
public class WishlistController {
    
    @Autowired
    private WishlistService wishlistService;
    
    /**
     * 获取用户愿望单
     */
    @GetMapping
    public CommonResponse<List<Map<String, Object>>> getWishlist(@RequestParam Long userId) {
        try {
            List<Map<String, Object>> wishlist = wishlistService.getUserWishlist(userId);
            return CommonResponse.success(wishlist);
        } catch (Exception e) {
            log.error("获取愿望单失败", e);
            return CommonResponse.error("获取愿望单失败：" + e.getMessage());
        }
    }
    
    /**
     * 添加到愿望单
     */
    @PostMapping
    public CommonResponse<?> addToWishlist(
            @RequestParam Long userId,
            @RequestParam Long gameId,
            @RequestParam(required = false) Integer priority,
            @RequestParam(required = false) Boolean notifyDiscount) {
        try {
            wishlistService.addToWishlist(userId, gameId, priority, notifyDiscount);
            return CommonResponse.success("添加成功");
        } catch (IllegalArgumentException e) {
            return CommonResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("添加到愿望单失败", e);
            return CommonResponse.error("添加失败：" + e.getMessage());
        }
    }
    
    /**
     * 从愿望单移除
     */
    @DeleteMapping("/{gameId}")
    public CommonResponse<Void> removeFromWishlist(
            @PathVariable Long gameId,
            @RequestParam Long userId) {
        try {
            wishlistService.removeFromWishlist(userId, gameId);
            return CommonResponse.success(null);
        } catch (Exception e) {
            log.error("移除愿望单项失败", e);
            return CommonResponse.error("移除失败：" + e.getMessage());
        }
    }
    
    /**
     * 更新通知设置
     */
    @PutMapping("/{gameId}/notify")
    public CommonResponse<Void> updateNotification(
            @PathVariable Long gameId,
            @RequestParam Long userId,
            @RequestParam(required = false) Boolean notifyDiscount,
            @RequestParam(required = false) Boolean notifyRelease) {
        try {
            wishlistService.updateNotification(userId, gameId, notifyDiscount, notifyRelease);
            return CommonResponse.success(null);
        } catch (Exception e) {
            log.error("更新通知设置失败", e);
            return CommonResponse.error("更新失败：" + e.getMessage());
        }
    }
    
    /**
     * 清空愿望单
     */
    @DeleteMapping
    public CommonResponse<Void> clearWishlist(@RequestParam Long userId) {
        try {
            wishlistService.clearWishlist(userId);
            return CommonResponse.success(null);
        } catch (Exception e) {
            log.error("清空愿望单失败", e);
            return CommonResponse.error("清空失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取愿望单数量
     */
    @GetMapping("/count")
    public CommonResponse<Map<String, Object>> getWishlistCount(@RequestParam Long userId) {
        try {
            long count = wishlistService.getWishlistCount(userId);
            Map<String, Object> result = Map.of("count", count);
            return CommonResponse.success(result);
        } catch (Exception e) {
            log.error("获取愿望单数量失败", e);
            return CommonResponse.error("获取失败：" + e.getMessage());
        }
    }
}
