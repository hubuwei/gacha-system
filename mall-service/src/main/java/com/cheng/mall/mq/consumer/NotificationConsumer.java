package com.cheng.mall.mq.consumer;

import com.cheng.mall.entity.Notification;
import com.cheng.mall.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 通知消息消费者
 */
@Slf4j
@Component
public class NotificationConsumer {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    /**
     * 监听通知队列
     */
    @RabbitListener(queues = "notification.queue")
    public void handleNotificationMessage(Map<String, Object> message) {
        try {
            log.info("收到MQ消息: {}", message);
            
            Long userId = Long.valueOf(message.get("userId").toString());
            String type = (String) message.get("type");
            String title = (String) message.get("title");
            String content = (String) message.get("content");
            
            log.info("解析后的消息: userId={}, type={}, title={}, content={}", userId, type, title, content);
            
            Long relatedGameId = message.get("relatedGameId") != null ? 
                Long.valueOf(message.get("relatedGameId").toString()) : null;
            Long relatedOrderId = message.get("relatedOrderId") != null ? 
                Long.valueOf(message.get("relatedOrderId").toString()) : null;
            String relatedType = (String) message.get("relatedType");
            
            // 创建通知
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setType(type);
            notification.setTitle(title);
            notification.setMessage(content);
            notification.setIsRead(false);
            notification.setRelatedGameId(relatedGameId);
            notification.setRelatedOrderId(relatedOrderId);
            notification.setRelatedType(relatedType);
            
            Notification saved = notificationRepository.save(notification);
            
            log.info("处理通知消息成功: id={}, userId={}, type={}, title={}, messageLength={}", 
                saved.getId(), userId, type, title, content != null ? content.length() : 0);
        } catch (Exception e) {
            log.error("处理通知消息失败: {}", message, e);
        }
    }
}
