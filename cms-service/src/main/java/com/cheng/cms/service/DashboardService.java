package com.cheng.cms.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * Dashboard Service - 数据看板业务逻辑
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 获取仪表盘统计数据
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // 总用户数
        Long totalUsers = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM users", Long.class);
        stats.put("totalUsers", totalUsers != null ? totalUsers : 0);

        // 今日新增用户
        Long todayNewUsers = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM users WHERE DATE(created_at) = CURDATE()", Long.class);
        stats.put("todayNewUsers", todayNewUsers != null ? todayNewUsers : 0);

        // 总订单数
        Long totalOrders = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM orders", Long.class);
        stats.put("totalOrders", totalOrders != null ? totalOrders : 0);

        // 今日订单成交量
        Long todayOrders = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM orders WHERE DATE(created_at) = CURDATE()", Long.class);
        stats.put("todayOrders", todayOrders != null ? todayOrders : 0);

        // 总营收（已完成的订单）
        BigDecimal totalRevenue = jdbcTemplate.queryForObject(
            "SELECT COALESCE(SUM(actual_amount), 0) FROM orders WHERE order_status = 'completed'", 
            BigDecimal.class);
        stats.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);

        // 今日销售额
        BigDecimal todayRevenue = jdbcTemplate.queryForObject(
            "SELECT COALESCE(SUM(actual_amount), 0) FROM orders WHERE order_status = 'completed' AND DATE(created_at) = CURDATE()", 
            BigDecimal.class);
        stats.put("todayRevenue", todayRevenue != null ? todayRevenue : BigDecimal.ZERO);

        // 游戏总数
        Long totalGames = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM games", Long.class);
        stats.put("totalGames", totalGames != null ? totalGames : 0);

        // 待处理订单数
        Long pendingOrders = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM orders WHERE order_status = 'pending'", Long.class);
        stats.put("pendingOrders", pendingOrders != null ? pendingOrders : 0);

        // 待审核评论数（假设有comments表）
        Long pendingReviews = 0L;
        try {
            pendingReviews = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM game_reviews WHERE status = 'pending'", Long.class);
        } catch (Exception e) {
            // 如果表不存在，默认为0
        }
        stats.put("pendingReviews", pendingReviews != null ? pendingReviews : 0);

        return stats;
    }

    /**
     * 获取本周营收数据
     */
    public List<Map<String, Object>> getWeeklyRevenue() {
        String sql = "SELECT " +
            "DATE_FORMAT(created_at, '%Y-%m-%d') as date, " +
            "COALESCE(SUM(actual_amount), 0) as revenue " +
            "FROM orders " +
            "WHERE order_status = 'completed' " +
            "AND created_at >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
            "GROUP BY DATE_FORMAT(created_at, '%Y-%m-%d') " +
            "ORDER BY date ASC";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

        // 转换为前端需要的格式，补充缺失的日期
        List<Map<String, Object>> result = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_WEEK, -6); // 从7天前开始

        for (int i = 0; i < 7; i++) {
            String dateStr = String.format("%tF", cal.getTime());
            String dayOfWeek = getDayOfWeek(cal.get(Calendar.DAY_OF_WEEK));

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", dayOfWeek);
            dayData.put("revenue", 0.0);

            // 查找是否有这一天的数据
            for (Map<String, Object> row : rows) {
                if (dateStr.equals(row.get("date"))) {
                    dayData.put("revenue", ((Number) row.get("revenue")).doubleValue());
                    break;
                }
            }

            result.add(dayData);
            cal.add(Calendar.DAY_OF_WEEK, 1);
        }

        return result;
    }

    /**
     * 获取星期几的中文名称
     */
    private String getDayOfWeek(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.MONDAY: return "周一";
            case Calendar.TUESDAY: return "周二";
            case Calendar.WEDNESDAY: return "周三";
            case Calendar.THURSDAY: return "周四";
            case Calendar.FRIDAY: return "周五";
            case Calendar.SATURDAY: return "周六";
            case Calendar.SUNDAY: return "周日";
            default: return "";
        }
    }
}
