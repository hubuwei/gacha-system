package com.cheng.mall.service;

import com.cheng.mall.entity.*;
import com.cheng.mall.mq.producer.MessageProducer;
import com.cheng.mall.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单服务
 */
@Slf4j
@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderItemRepository orderItemRepository;
    
    @Autowired
    private ShoppingCartRepository cartRepository;
    
    @Autowired
    private GameRepository gameRepository;
    
    @Autowired
    private WalletService walletService;
    
    @Autowired
    private MessageProducer messageProducer;
    
    @Autowired
    private WechatPayService wechatPayService;
    
    @Autowired
    private OrderTimeoutService orderTimeoutService;
    
    /**
     * 创建订单并支付
     */
    @Transactional
    public Map<String, Object> createOrder(Long userId, String paymentMethod, List<Map<String, Object>> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("购物车不能为空");
        }
        
        // 生成订单号
        String orderNo = generateOrderNo();
        
        // 计算订单金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        
        for (Map<String, Object> item : items) {
            Long gameId = Long.valueOf(item.get("gameId").toString());
            Integer quantity = Integer.valueOf(item.get("quantity").toString());
            
            Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("游戏不存在: " + gameId));
            
            // 检查用户是否已经购买过该游戏
            if (hasUserPurchasedGame(userId, gameId)) {
                throw new IllegalArgumentException("您已拥有游戏《" + game.getTitle() + "》，无需重复购买");
            }
            
            BigDecimal originalPrice = game.getBasePrice().multiply(new BigDecimal(quantity));
            BigDecimal actualPrice = game.getCurrentPrice().multiply(new BigDecimal(quantity));
            
            totalAmount = totalAmount.add(actualPrice);
            discountAmount = discountAmount.add(originalPrice.subtract(actualPrice));
            
            OrderItem orderItem = new OrderItem();
            orderItem.setGameId(gameId);
            orderItem.setGameTitle(game.getTitle());
            orderItem.setGameCover(game.getCoverImage());
            orderItem.setQuantity(quantity);
            orderItem.setOriginalPrice(game.getBasePrice());
            orderItem.setActualPrice(game.getCurrentPrice());
            orderItem.setDiscountRate(game.getDiscountRate());
            
            orderItems.add(orderItem);
        }
        
        // 创建订单
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setActualAmount(totalAmount);
        order.setPaymentMethod(paymentMethod);
        order.setPaymentStatus("pending");
        order.setOrderStatus("pending");
        
        order = orderRepository.save(order);
        
        // 保存订单详情
        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemRepository.save(item);
        }
        
        // 如果支付方式是余额支付，立即扣款
        Map<String, Object> result = new HashMap<>();
        
        if ("balance".equals(paymentMethod)) {
            try {
                walletService.deductBalance(userId, totalAmount, order.getId(), orderNo);
                // 更新订单状态为已支付
                order.setPaymentStatus("paid");
                order.setOrderStatus("completed");
                order.setPaymentTime(java.time.LocalDateTime.now());
                orderRepository.save(order);
            } catch (IllegalArgumentException e) {
                // 余额不足，取消订单
                order.setOrderStatus("cancelled");
                orderRepository.save(order);
                throw new IllegalArgumentException("余额不足，请充值后重试");
            }
        }
        // TODO: 微信支付功能已临时注释（生产环境待配置）
        /* else if ("wechat".equals(paymentMethod)) {
            // 微信支付：生成 Native 二维码
            try {
                Map<String, Object> wechatResult = wechatPayService.createNativeOrder(
                    orderNo, 
                    totalAmount.doubleValue(), 
                    "游戏商城订单-" + orderNo
                );
                // 将微信返回的 codeUrl 添加到结果中
                result.put("codeUrl", wechatResult.get("codeUrl"));
                result.put("mockMode", wechatResult.get("mockMode"));
                result.put("tip", wechatResult.get("tip"));
                log.info("微信 Native 下单成功: orderNo={}, mock={}", orderNo, wechatResult.get("mockMode"));
            } catch (Exception e) {
                log.error("微信 Native 下单失败，订单将被取消: orderNo={}", orderNo, e);
                order.setOrderStatus("cancelled");
                orderRepository.save(order);
                throw new RuntimeException("微信下单失败: " + e.getMessage());
            }
        } */
        else if ("alipay".equals(paymentMethod)) {
            // TODO: 支付宝支付逻辑
            log.info("支付宝支付待实现: orderNo={}", orderNo);
        }
        
        // 清空购物车中已购买的商品
        for (Map<String, Object> item : items) {
            Long gameId = Long.valueOf(item.get("gameId").toString());
            cartRepository.deleteByUserIdAndGameId(userId, gameId);
        }
        
        // 发送 MQ 消息（异步处理）
        messageProducer.sendOrderCreateMessage(order.getId(), userId, totalAmount.doubleValue());
        messageProducer.sendOrderEmailMessage(order.getId(), "user@example.com", orderNo); // TODO: 获取用户邮箱
        
        // 记录审计日志
        messageProducer.sendAuditLogMessage(
            userId, 
            "ORDER_CREATE", 
            "order:" + orderNo, 
            String.format("创建订单，金额: %.2f", totalAmount)
        );
        
        // 设置订单超时监控（Redis）
        if ("pending".equals(order.getOrderStatus())) {
            orderTimeoutService.setOrderTimeout(orderNo, userId);
        }
        
        // 返回订单信息
        result.put("orderId", order.getId());
        result.put("orderNo", order.getOrderNo());
        result.put("totalAmount", order.getTotalAmount());
        result.put("actualAmount", order.getActualAmount());
        result.put("paymentStatus", order.getPaymentStatus());
        result.put("orderStatus", order.getOrderStatus());
        
        return result;
    }
    
    /**
     * 获取用户订单列表
     */
    public List<Map<String, Object>> getUserOrders(Long userId, String status) {
        List<Order> orders;
        
        if (status != null && !status.equals("all")) {
            orders = orderRepository.findByUserIdAndOrderStatus(userId, status);
        } else {
            orders = orderRepository.findByUserIdOrderByCreatedAtDesc(
                userId, 
                org.springframework.data.domain.PageRequest.of(0, 100)
            ).getContent();
        }
        
        // 过滤掉已过期的待支付订单（超过15分钟）
        LocalDateTime now = LocalDateTime.now();
        List<Order> validOrders = new ArrayList<>();
        List<String> expiredOrderNos = new ArrayList<>();
        
        for (Order order : orders) {
            // 如果不是待支付状态，直接保留
            if (!"pending".equals(order.getOrderStatus())) {
                validOrders.add(order);
                continue;
            }
            
            // 检查是否超过15分钟
            LocalDateTime createTime = order.getCreatedAt();
            if (createTime == null) {
                validOrders.add(order);
                continue;
            }
            
            long minutesPassed = java.time.Duration.between(createTime, now).toMinutes();
            // 如果超过15分钟，标记为过期
            if (minutesPassed >= 15) {
                log.info("订单已过期，将自动取消: orderNo={}, 创建时间={}, 已过{}分钟", 
                    order.getOrderNo(), createTime, minutesPassed);
                expiredOrderNos.add(order.getOrderNo());
            } else {
                validOrders.add(order); // 未过期，保留
            }
        }
        
        // 异步取消过期订单（不阻塞返回）
        if (!expiredOrderNos.isEmpty()) {
            final List<String> toCancel = new ArrayList<>(expiredOrderNos);
            new Thread(() -> {
                try {
                    Thread.sleep(100); // 稍延迟执行，确保响应先返回
                    for (String orderNo : toCancel) {
                        cancelOrderByOrderNo(orderNo);
                    }
                } catch (Exception e) {
                    log.error("批量取消过期订单失败", e);
                }
            }).start();
        }
        
        return validOrders.stream()
            .map(this::convertToMap)
            .collect(Collectors.toList());
    }
    
    /**
     * 获取订单详情
     */
    public Map<String, Object> getOrderDetail(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("订单不存在"));
        
        if (!order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权查看此订单");
        }
        
        Map<String, Object> result = convertToMap(order);
        
        // 获取订单详情
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        List<Map<String, Object>> itemMaps = items.stream()
            .map(item -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", item.getId());
                map.put("gameId", item.getGameId());
                map.put("gameTitle", item.getGameTitle());
                map.put("gameCover", item.getGameCover());
                map.put("quantity", item.getQuantity());
                map.put("originalPrice", item.getOriginalPrice());
                map.put("actualPrice", item.getActualPrice());
                map.put("discountRate", item.getDiscountRate());
                return map;
            })
            .collect(Collectors.toList());
        
        result.put("items", itemMaps);
        
        return result;
    }
    
    /**
     * 取消订单
     */
    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("订单不存在"));
        
        if (!order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权操作此订单");
        }
        
        if (!"pending".equals(order.getOrderStatus())) {
            throw new IllegalArgumentException("只能取消待支付的订单");
        }
        
        order.setOrderStatus("cancelled");
        orderRepository.save(order);
    }
    
    /**
     * 根据订单号取消订单（用于 Redis 超时自动取消）
     */
    @Transactional
    public void cancelOrderByOrderNo(String orderNo) {
        // 直接通过订单号查询，更高效
        Order order = orderRepository.findByOrderNo(orderNo).orElse(null);
        
        if (order == null) {
            log.warn("订单不存在: orderNo={}", orderNo);
            return;
        }
        
        if (!"pending".equals(order.getOrderStatus())) {
            log.debug("订单状态不是待支付，无需取消: orderNo={}, status={}", orderNo, order.getOrderStatus());
            return;
        }
        
        // 取消订单
        order.setOrderStatus("cancelled");
        orderRepository.save(order);
        
        log.info("订单已自动取消: orderNo={}, userId={}", orderNo, order.getUserId());
        
        // 发送审计日志
        messageProducer.sendAuditLogMessage(
            order.getUserId(),
            "ORDER_AUTO_CANCEL",
            "order:" + orderNo,
            String.format("订单超时15分钟未支付，自动取消")
        );
    }
    
    /**
     * 检查用户是否已购买某游戏
     */
    private boolean hasUserPurchasedGame(Long userId, Long gameId) {
        List<Order> completedOrders = orderRepository.findByUserIdAndOrderStatus(userId, "completed");
        for (Order order : completedOrders) {
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
            for (OrderItem item : items) {
                if (item.getGameId().equals(gameId)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * 获取已购游戏列表
     */
    public List<Map<String, Object>> getPurchasedGames(Long userId) {
        // 查询已完成订单中的游戏
        List<Order> completedOrders = orderRepository.findByUserIdAndOrderStatus(userId, "completed");
        
        Set<Long> purchasedGameIds = new HashSet<>();
        for (Order order : completedOrders) {
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
            for (OrderItem item : items) {
                purchasedGameIds.add(item.getGameId());
            }
        }
        
        // 获取游戏详情
        List<Game> games = gameRepository.findAllById(purchasedGameIds);
        
        return games.stream()
            .map(game -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", game.getId());
                map.put("title", game.getTitle());
                map.put("coverImage", game.getCoverImage());
                map.put("purchaseDate", LocalDateTime.now()); // TODO: 从订单获取实际购买时间
                map.put("installed", false); // TODO: 实现安装状态跟踪
                map.put("playTime", 0.0); // TODO: 实现游戏时长跟踪
                return map;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 转换为 Map
     */
    private Map<String, Object> convertToMap(Order order) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", order.getId());
        map.put("orderNo", order.getOrderNo());
        map.put("userId", order.getUserId());
        map.put("totalAmount", order.getTotalAmount());
        map.put("discountAmount", order.getDiscountAmount());
        map.put("actualAmount", order.getActualAmount());
        map.put("paymentMethod", order.getPaymentMethod());
        map.put("paymentStatus", order.getPaymentStatus());
        map.put("paymentTime", order.getPaymentTime());
        map.put("orderStatus", order.getOrderStatus());
        map.put("createdAt", order.getCreatedAt());
        map.put("updatedAt", order.getUpdatedAt());
        return map;
    }
    
    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        int random = (int) (Math.random() * 9000) + 1000;
        return "ORD" + timestamp + random;
    }
}
