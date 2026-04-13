package com.cheng.mall.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * 订单超时管理服务（使用 Redis）
 */
@Slf4j
@Service
public class OrderTimeoutService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    @Lazy  // 延迟加载，打破循环依赖
    private OrderService orderService;
    
    @Autowired
    private RedisMessageListenerContainer redisMessageListenerContainer;
    
    private static final String ORDER_TIMEOUT_KEY = "order:timeout:";
    private static final long TIMEOUT_MINUTES = 15; // 15分钟超时
    
    /**
     * 初始化 Redis 键空间通知监听器
     */
    @PostConstruct
    public void initKeySpaceListener() {
        // 监听 __keyevent@0__:expired 事件（key 过期事件）
        redisMessageListenerContainer.addMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message, byte[] pattern) {
                String expiredKey = new String(message.getBody(), StandardCharsets.UTF_8);
                log.info("Redis Key 过期: {}", expiredKey);
                
                // 检查是否是订单超时 key
                if (expiredKey.startsWith(ORDER_TIMEOUT_KEY) && 
                    !expiredKey.contains("remind")) {
                    // 提取订单号
                    String orderNo = expiredKey.substring(ORDER_TIMEOUT_KEY.length());
                    log.info("订单超时，准备取消: orderNo={}", orderNo);
                    
                    // 异步取消订单
                    cancelExpiredOrder(orderNo);
                }
            }
        }, new PatternTopic("__keyevent@0__:expired"));
        
        log.info("Redis 键空间通知监听器已启动");
    }
    
    /**
     * 创建订单时设置超时
     */
    public void setOrderTimeout(String orderNo, Long userId) {
        String key = ORDER_TIMEOUT_KEY + orderNo;
        
        // 设置订单超时时间（15分钟）
        redisTemplate.opsForValue().set(key, userId.toString(), TIMEOUT_MINUTES, TimeUnit.MINUTES);
        
        log.info("订单超时监控已设置: orderNo={}, 超时时间={}分钟", orderNo, TIMEOUT_MINUTES);
    }
    
    /**
     * 支付成功后删除超时监控
     */
    public void removeOrderTimeout(String orderNo) {
        String key = ORDER_TIMEOUT_KEY + orderNo;
        redisTemplate.delete(key);
        
        log.info("订单超时监控已移除: orderNo={}", orderNo);
    }
    
    /**
     * 检查订单是否超时（用于前端查询）
     */
    public Long getRemainingTime(String orderNo) {
        String key = ORDER_TIMEOUT_KEY + orderNo;
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        
        if (ttl == null || ttl < 0) {
            return 0L; // 已过期或不存在
        }
        
        return ttl;
    }
    
    /**
     * 取消过期订单
     */
    private void cancelExpiredOrder(String orderNo) {
        try {
            log.info("开始取消过期订单: orderNo={}", orderNo);
            orderService.cancelOrderByOrderNo(orderNo);
            log.info("订单已成功取消: orderNo={}", orderNo);
        } catch (Exception e) {
            log.error("取消过期订单失败: orderNo={}", orderNo, e);
        }
    }
}
