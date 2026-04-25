package com.cheng.cms.controller;

import com.cheng.common.dto.CommonResponse;
import com.cheng.cms.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Dashboard Controller - 数据看板接口
 */
@RestController
@RequestMapping("/api/cms/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 获取统计数据
     */
    @GetMapping("/stats")
    public CommonResponse<Map<String, Object>> getStats() {
        try {
            Map<String, Object> stats = dashboardService.getDashboardStats();
            return CommonResponse.success(stats);
        } catch (Exception e) {
            return CommonResponse.error("获取统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取本周营收数据
     */
    @GetMapping("/weekly-revenue")
    public CommonResponse<Object> getWeeklyRevenue() {
        try {
            Object revenueData = dashboardService.getWeeklyRevenue();
            return CommonResponse.success(revenueData);
        } catch (Exception e) {
            return CommonResponse.error("获取营收数据失败: " + e.getMessage());
        }
    }
}
