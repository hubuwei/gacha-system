package com.cheng.cms.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * Order Service - 订单管理业务逻辑
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 获取订单列表（支持筛选和分页）
     */
    public Map<String, Object> getOrders(String orderNo, String paymentStatus, String orderStatus, int page, int size) {
        StringBuilder countSql = new StringBuilder(
            "SELECT COUNT(*) FROM orders o WHERE 1=1"
        );
        StringBuilder querySql = new StringBuilder(
            "SELECT o.id, o.order_no, o.user_id, o.total_amount, o.discount_amount, " +
            "o.actual_amount, o.payment_method, o.payment_status, o.payment_time, " +
            "o.order_status, o.created_at, u.username " +
            "FROM orders o LEFT JOIN users u ON o.user_id = u.id WHERE 1=1"
        );

        List<Object> params = new ArrayList<>();

        // 订单号筛选
        if (orderNo != null && !orderNo.trim().isEmpty()) {
            countSql.append(" AND o.order_no LIKE ?");
            querySql.append(" AND o.order_no LIKE ?");
            params.add("%" + orderNo + "%");
        }

        // 支付状态筛选
        if (paymentStatus != null && !paymentStatus.trim().isEmpty()) {
            countSql.append(" AND o.payment_status = ?");
            querySql.append(" AND o.payment_status = ?");
            params.add(paymentStatus);
        }

        // 订单状态筛选
        if (orderStatus != null && !orderStatus.trim().isEmpty()) {
            countSql.append(" AND o.order_status = ?");
            querySql.append(" AND o.order_status = ?");
            params.add(orderStatus);
        }

        // 查询总数
        Long total = jdbcTemplate.queryForObject(countSql.toString(), Long.class, params.toArray());

        // 分页查询
        int offset = (page - 1) * size;
        querySql.append(" ORDER BY o.created_at DESC LIMIT ? OFFSET ?");
        params.add(size);
        params.add(offset);

        List<Map<String, Object>> orders = jdbcTemplate.queryForList(querySql.toString(), params.toArray());

        // 转换数据格式
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> order : orders) {
            Map<String, Object> item = new HashMap<>();
            item.put("key", String.valueOf(order.get("id")));
            item.put("id", order.get("id"));
            item.put("orderNo", order.get("order_no"));
            item.put("userId", order.get("user_id"));
            item.put("username", order.get("username"));
            item.put("totalAmount", ((BigDecimal) order.get("total_amount")).doubleValue());
            item.put("discountAmount", ((BigDecimal) order.get("discount_amount")).doubleValue());
            item.put("actualAmount", ((BigDecimal) order.get("actual_amount")).doubleValue());
            item.put("paymentMethod", order.get("payment_method"));
            item.put("paymentStatus", order.get("payment_status"));
            item.put("paymentTime", order.get("payment_time"));
            item.put("orderStatus", order.get("order_status"));
            item.put("createdAt", order.get("created_at"));
            result.add(item);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("list", result);
        response.put("total", total);
        response.put("page", page);
        response.put("size", size);

        return response;
    }

    /**
     * 根据ID获取订单详情
     */
    public Map<String, Object> getOrderById(Long id) {
        String sql = "SELECT o.*, u.username, u.email " +
            "FROM orders o LEFT JOIN users u ON o.user_id = u.id WHERE o.id = ?";
        List<Map<String, Object>> orders = jdbcTemplate.queryForList(sql, id);

        if (orders.isEmpty()) {
            return null;
        }

        Map<String, Object> order = orders.get(0);
        Map<String, Object> result = new HashMap<>();
        result.put("id", order.get("id"));
        result.put("orderNo", order.get("order_no"));
        result.put("userId", order.get("user_id"));
        result.put("username", order.get("username"));
        result.put("email", order.get("email"));
        result.put("totalAmount", ((BigDecimal) order.get("total_amount")).doubleValue());
        result.put("discountAmount", ((BigDecimal) order.get("discount_amount")).doubleValue());
        result.put("actualAmount", ((BigDecimal) order.get("actual_amount")).doubleValue());
        result.put("paymentMethod", order.get("payment_method"));
        result.put("paymentStatus", order.get("payment_status"));
        result.put("paymentTime", order.get("payment_time"));
        result.put("orderStatus", order.get("order_status"));
        result.put("refundTime", order.get("refund_time"));
        result.put("refundReason", order.get("refund_reason"));
        result.put("remark", order.get("remark"));
        result.put("createdAt", order.get("created_at"));
        result.put("updatedAt", order.get("updated_at"));

        return result;
    }

    /**
     * 更新订单状态
     */
    public void updateOrderStatus(Long id, String orderStatus) {
        String sql = "UPDATE orders SET order_status = ?, updated_at = NOW() WHERE id = ?";
        jdbcTemplate.update(sql, orderStatus, id);
    }
}
