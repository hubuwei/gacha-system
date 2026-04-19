package com.cheng.mall.mq.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.cheng.mall.config.RabbitMQConfig.*;

/**
 * 消息生产者
 */
@Slf4j
@Component
public class MessageProducer {
    
    @Autowired(required = false)
    private RabbitTemplate rabbitTemplate;
    
    @Autowired(required = false)
    private ObjectMapper objectMapper;
    
    /**
     * 发送订单创建消息
     * 
     * @param orderId 订单ID
     * @param userId 用户ID
     * @param totalAmount 订单金额
     */
    public void sendOrderCreateMessage(Long orderId, Long userId, Double totalAmount) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("orderId", orderId);
            message.put("userId", userId);
            message.put("totalAmount", totalAmount);
            message.put("timestamp", System.currentTimeMillis());
            
            rabbitTemplate.convertAndSend(
                ORDER_EXCHANGE,
                ORDER_CREATE_ROUTING_KEY,
                objectMapper.writeValueAsString(message)
            );
            
            log.info("发送订单创建消息: orderId={}", orderId);
        } catch (Exception e) {
            log.error("发送订单创建消息失败", e);
        }
    }
    
    /**
     * 发送订单邮件通知消息
     * 
     * @param orderId 订单ID
     * @param userEmail 用户邮箱
     * @param orderNo 订单号
     */
    public void sendOrderEmailMessage(Long orderId, String userEmail, String orderNo) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("orderId", orderId);
            message.put("userEmail", userEmail);
            message.put("orderNo", orderNo);
            message.put("timestamp", System.currentTimeMillis());
            
            rabbitTemplate.convertAndSend(
                ORDER_EXCHANGE,
                ORDER_EMAIL_ROUTING_KEY,
                objectMapper.writeValueAsString(message)
            );
            
            log.info("发送订单邮件通知消息: orderId={}", orderId);
        } catch (Exception e) {
            log.error("发送订单邮件通知消息失败", e);
        }
    }
    
    /**
     * 发送游戏上架/下架事件（广播）
     * 
     * @param gameId 游戏ID
     * @param eventType 事件类型：on/off
     * @param gameTitle 游戏标题
     */
    public void sendGameEventMessage(Long gameId, String eventType, String gameTitle) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("gameId", gameId);
            message.put("eventType", eventType); // on-上架, off-下架
            message.put("gameTitle", gameTitle);
            message.put("timestamp", System.currentTimeMillis());
            
            // Fanout 交换机会广播到所有绑定的队列
            rabbitTemplate.convertAndSend(
                GAME_EXCHANGE,
                "", // Fanout 不需要 routing key
                objectMapper.writeValueAsString(message)
            );
            
            log.info("发送游戏事件消息: gameId={}, eventType={}", gameId, eventType);
        } catch (Exception e) {
            log.error("发送游戏事件消息失败", e);
        }
    }
    
    /**
     * 发送审计日志消息
     * 
     * @param userId 操作用户ID
     * @param action 操作类型
     * @param target 操作目标
     * @param details 详细信息
     */
    public void sendAuditLogMessage(Long userId, String action, String target, String details) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("userId", userId);
            message.put("action", action);
            message.put("target", target);
            message.put("details", details);
            message.put("timestamp", System.currentTimeMillis());
            
            String routingKey = "log.audit." + action;
            
            rabbitTemplate.convertAndSend(
                LOG_EXCHANGE,
                routingKey,
                objectMapper.writeValueAsString(message)
            );
            
            log.debug("发送审计日志: userId={}, action={}", userId, action);
        } catch (Exception e) {
            log.error("发送审计日志失败", e);
        }
    }
}
