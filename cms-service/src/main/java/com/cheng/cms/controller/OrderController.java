package com.cheng.cms.controller;

import com.cheng.common.dto.CommonResponse;
import com.cheng.cms.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Order Controller - 订单管理接口
 */
@RestController
@RequestMapping("/api/cms/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 获取订单列表
     */
    @GetMapping
    public CommonResponse<List<Map<String, Object>>> getOrders(
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) String orderStatus,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Map<String, Object> result = orderService.getOrders(orderNo, paymentStatus, orderStatus, page, size);
            return CommonResponse.success((List<Map<String, Object>>) result.get("list"));
        } catch (Exception e) {
            return CommonResponse.error("获取订单列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{id}")
    public CommonResponse<Map<String, Object>> getOrderById(@PathVariable Long id) {
        try {
            Map<String, Object> order = orderService.getOrderById(id);
            if (order == null) {
                return CommonResponse.error("订单不存在");
            }
            return CommonResponse.success(order);
        } catch (Exception e) {
            return CommonResponse.error("获取订单详情失败: " + e.getMessage());
        }
    }

    /**
     * 更新订单状态
     */
    @PutMapping("/{id}/status")
    public CommonResponse<Void> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String orderStatus) {
        try {
            orderService.updateOrderStatus(id, orderStatus);
            return CommonResponse.success(null);
        } catch (Exception e) {
            return CommonResponse.error("更新订单状态失败: " + e.getMessage());
        }
    }
}
