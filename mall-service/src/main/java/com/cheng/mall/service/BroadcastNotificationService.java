package com.cheng.mall.service;

import com.cheng.common.entity.User;
import com.cheng.common.repository.UserRepository;
import com.cheng.mall.mq.producer.NotificationProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 广播通知服务（小铃铛广播模式）
 */
@Slf4j
@Service
public class BroadcastNotificationService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NotificationProducer notificationProducer;
    
    /**
     * 向所有用户广播促销消息（小铃铛广播模式）
     * 
     * @param title 通知标题
     * @param content 通知内容
     * @param gameId 关联的游戏ID（可选）
     * @param relatedOrderId 关联的订单ID（可选）
     * @param type 通知类型：promotion/system/order
     */
    public void broadcastPromotion(String title, String content, Long gameId, Long relatedOrderId, String type) {
        // 获取所有用户
        List<User> allUsers = userRepository.findAll();
        
        log.info("开始广播通知: 用户数={}, title={}, type={}", allUsers.size(), title, type);
        
        // 逐个发送通知
        int successCount = 0;
        int failCount = 0;
        for (User user : allUsers) {
            try {
                notificationProducer.sendNotificationMessage(
                    user.getId(),
                    type != null ? type : "promotion",
                    title,
                    content,
                    gameId,
                    relatedOrderId,
                    gameId != null ? "game" : (relatedOrderId != null ? "order" : "system")
                );
                successCount++;
            } catch (Exception e) {
                failCount++;
                log.error("发送通知失败: userId={}", user.getId(), e);
            }
        }
        
        log.info("广播完成: 总数={}, 成功={}, 失败={}", allUsers.size(), successCount, failCount);
    }
    
    /**
     * 向所有用户广播促销消息（兼容旧版本）
     */
    public void broadcastPromotion(String title, String content, Long gameId) {
        broadcastPromotion(title, content, gameId, null, "promotion");
    }
    
    /**
     * 向特定用户组广播（例如：已绑定邮箱的用户）
     */
    public void broadcastToEmailUsers(String title, String content, Long gameId) {
        // 获取所有已验证邮箱的用户
        List<User> usersWithEmail = userRepository.findAll().stream()
            .filter(user -> user.getEmail() != null && !user.getEmail().isEmpty())
            .collect(Collectors.toList());
        
        log.info("开始向邮箱用户广播: 用户数={}, title={}", usersWithEmail.size(), title);
        
        int successCount = 0;
        for (User user : usersWithEmail) {
            try {
                notificationProducer.sendNotificationMessage(
                    user.getId(),
                    "promotion",
                    title,
                    content,
                    gameId,
                    null,
                    "game"
                );
                successCount++;
            } catch (Exception e) {
                log.error("发送通知失败: userId={}", user.getId(), e);
            }
        }
        
        log.info("邮箱用户广播完成: 总数={}, 成功={}", usersWithEmail.size(), successCount);
    }
}
