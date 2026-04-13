package com.cheng.mall.controller;

import com.cheng.common.dto.CommonResponse;
import com.cheng.mall.service.OrderService;
import com.cheng.mall.service.OrderTimeoutService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单控制器
 */
@Slf4j
@RestController
@RequestMapping("/orders")
@CrossOrigin(origins = "*")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private OrderTimeoutService orderTimeoutService;
    
    /**
     * 创建订单
     */
    @PostMapping("/create")
    public CommonResponse<Map<String, Object>> createOrder(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            String paymentMethod = (String) request.get("paymentMethod");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) request.get("items");
            
            Map<String, Object> result = orderService.createOrder(userId, paymentMethod, items);
            return CommonResponse.success(result);
        } catch (IllegalArgumentException e) {
            return CommonResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("创建订单失败", e);
            return CommonResponse.error("创建订单失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取用户订单列表
     */
    @GetMapping
    public CommonResponse<List<Map<String, Object>>> getOrders(
            @RequestParam Long userId,
            @RequestParam(required = false) String status) {
        try {
            List<Map<String, Object>> orders = orderService.getUserOrders(userId, status);
            return CommonResponse.success(orders);
        } catch (Exception e) {
            log.error("获取订单列表失败", e);
            return CommonResponse.error("获取订单列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取订单详情
     */
    @GetMapping("/{orderId}")
    public CommonResponse<Map<String, Object>> getOrderDetail(
            @PathVariable Long orderId,
            @RequestParam Long userId) {
        try {
            Map<String, Object> order = orderService.getOrderDetail(orderId, userId);
            return CommonResponse.success(order);
        } catch (IllegalArgumentException e) {
            return CommonResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("获取订单详情失败", e);
            return CommonResponse.error("获取订单详情失败：" + e.getMessage());
        }
    }
    
    /**
     * 取消订单
     */
    @PostMapping("/{orderId}/cancel")
    public CommonResponse<Void> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam Long userId) {
        try {
            orderService.cancelOrder(orderId, userId);
            return CommonResponse.success(null);
        } catch (IllegalArgumentException e) {
            return CommonResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("取消订单失败", e);
            return CommonResponse.error("取消订单失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取已购游戏列表
     */
    @GetMapping("/purchased-games")
    public CommonResponse<List<Map<String, Object>>> getPurchasedGames(@RequestParam Long userId) {
        try {
            List<Map<String, Object>> games = orderService.getPurchasedGames(userId);
            return CommonResponse.success(games);
        } catch (Exception e) {
            log.error("获取已购游戏失败", e);
            return CommonResponse.error("获取已购游戏失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取订单剩余时间（秒）
     */
    @GetMapping("/{orderNo}/timeout")
    public CommonResponse<Map<String, Object>> getOrderTimeout(@PathVariable String orderNo) {
        try {
            Long remainingSeconds = orderTimeoutService.getRemainingTime(orderNo);
            
            Map<String, Object> result = new HashMap<>();
            result.put("remainingSeconds", remainingSeconds);
            result.put("expired", remainingSeconds <= 0);
            
            if (remainingSeconds > 0) {
                long minutes = remainingSeconds / 60;
                long seconds = remainingSeconds % 60;
                result.put("countdown", String.format("%d:%02d", minutes, seconds));
            } else {
                result.put("countdown", "已过期");
            }
            
            return CommonResponse.success(result);
        } catch (Exception e) {
            log.error("获取订单超时信息失败", e);
            return CommonResponse.error("获取超时信息失败：" + e.getMessage());
        }
    }
}
