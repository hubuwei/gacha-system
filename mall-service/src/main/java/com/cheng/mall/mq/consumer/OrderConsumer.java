package com.cheng.mall.mq.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.cheng.mall.config.RabbitMQConfig.*;

/**
 * 订单消息消费者
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class OrderConsumer {
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 处理订单创建消息
     * 异步执行：扣减库存、更新统计等
     */
    @RabbitListener(queues = ORDER_CREATE_QUEUE)
    public void handleOrderCreate(String message) {
        try {
            Map<String, Object> orderData = objectMapper.readValue(message, Map.class);
            
            Long orderId = ((Number) orderData.get("orderId")).longValue();
            Long userId = ((Number) orderData.get("userId")).longValue();
            Double totalAmount = ((Number) orderData.get("totalAmount")).doubleValue();
            
            log.info("收到订单创建消息: orderId={}, userId={}, amount={}", 
                orderId, userId, totalAmount);
            
            // TODO: 异步处理订单
            // 1. 扣减游戏库存（如果有库存系统）
            // 2. 更新用户购买统计
            // 3. 生成下载链接
            // 4. 其他后台任务...
            
            log.info("订单创建处理完成: orderId={}", orderId);
            
        } catch (Exception e) {
            log.error("处理订单创建消息失败: {}", message, e);
            // 可以选择重新入队或记录到死信队列
        }
    }
    
    /**
     * 处理订单邮件通知消息
     * 异步发送邮件
     */
    @RabbitListener(queues = ORDER_EMAIL_QUEUE)
    public void handleOrderEmail(String message) {
        try {
            Map<String, Object> emailData = objectMapper.readValue(message, Map.class);
            
            Long orderId = ((Number) emailData.get("orderId")).longValue();
            String userEmail = (String) emailData.get("userEmail");
            String orderNo = (String) emailData.get("orderNo");
            
            log.info("收到订单邮件通知消息: orderId={}, email={}", orderId, userEmail);
            
            // TODO: 发送邮件
            // 1. 构建邮件内容
            // 2. 调用邮件服务发送
            // 3. 记录发送日志
            
            log.info("订单邮件发送完成: orderId={}, email={}", orderId, userEmail);
            
        } catch (Exception e) {
            log.error("处理订单邮件消息失败: {}", message, e);
        }
    }
}
