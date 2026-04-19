package com.cheng.mall.mq.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 通知消息生产者
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class NotificationProducer {
    
    @Autowired
    @Lazy
    private RabbitTemplate rabbitTemplate;
    
    private static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    private static final String NOTIFICATION_ROUTING_KEY = "notification.send";
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 发送通知消息到队列
     */
    public void sendNotificationMessage(Long userId, String type, String title, String content,
                                       Long relatedGameId, Long relatedOrderId, String relatedType) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("userId", userId);
            message.put("type", type);
            message.put("title", title);
            message.put("content", content);
            message.put("relatedGameId", relatedGameId);
            message.put("relatedOrderId", relatedOrderId);
            message.put("relatedType", relatedType);
            
            rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, NOTIFICATION_ROUTING_KEY, message);
            log.info("发送通知消息成功: userId={}, type={}, title={}", userId, type, title);
        } catch (Exception e) {
            log.error("发送通知消息失败", e);
        }
    }
    
    /**
     * 发送促销通知（批量用户）
     */
    public void sendPromotionNotification(java.util.List<Long> userIds, String title, String content, 
                                         Long gameId) {
        for (Long userId : userIds) {
            sendNotificationMessage(userId, "promotion", title, content, gameId, null, "game");
        }
        log.info("批量发送促销通知: 用户数={}, title={}", userIds.size(), title);
    }
}
