package com.cheng.mall.service;

import com.cheng.mall.entity.UserNotification;
import com.cheng.mall.repository.UserNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 通知服务类
 * 负责WebSocket实时推送和站内信存储
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final UserNotificationRepository notificationRepository;
    
    /**
     * 发送好友申请通知
     */
    @Transactional
    public void sendFriendRequestNotification(Long receiveUid, Long applyUid, String message) {
        // 1. 保存到数据库
        UserNotification notification = new UserNotification();
        notification.setUid(receiveUid);
        notification.setType("FRIEND_REQUEST");
        notification.setTitle("新的好友申请");
        notification.setContent(message != null ? message : "有人想加你为好友");
        notification.setFromUid(applyUid);
        notificationRepository.save(notification);
        
        // 2. WebSocket实时推送
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "FRIEND_REQUEST");
        payload.put("fromUid", applyUid);
        payload.put("message", notification.getContent());
        payload.put("notificationId", notification.getId());
        payload.put("timestamp", System.currentTimeMillis());
        
        try {
            messagingTemplate.convertAndSendToUser(
                receiveUid.toString(),
                "/topic/notifications",
                payload
            );
            log.info("好友申请通知已推送给用户 {}: 来自用户 {}", receiveUid, applyUid);
        } catch (Exception e) {
            log.error("WebSocket推送失败: {}", e.getMessage());
        }
    }
    
    /**
     * 发送好友申请被同意的通知
     */
    @Transactional
    public void sendFriendAcceptedNotification(Long uid, Long friendUid) {
        UserNotification notification = new UserNotification();
        notification.setUid(uid);
        notification.setType("FRIEND_ACCEPTED");
        notification.setTitle("好友申请已通过");
        notification.setContent("你们已成为好友，开始聊天吧！");
        notification.setFromUid(friendUid);
        notificationRepository.save(notification);
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "FRIEND_ACCEPTED");
        payload.put("friendUid", friendUid);
        payload.put("notificationId", notification.getId());
        payload.put("timestamp", System.currentTimeMillis());
        
        try {
            messagingTemplate.convertAndSendToUser(
                uid.toString(),
                "/topic/notifications",
                payload
            );
            log.info("好友同意通知已推送给用户 {}", uid);
        } catch (Exception e) {
            log.error("WebSocket推送失败: {}", e.getMessage());
        }
    }
    
    /**
     * 发送好友上线通知
     */
    public void sendFriendOnlineNotification(Long uid, Long friendUid) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "FRIEND_ONLINE");
        payload.put("friendUid", friendUid);
        payload.put("timestamp", System.currentTimeMillis());
        
        try {
            messagingTemplate.convertAndSendToUser(
                uid.toString(),
                "/topic/notifications",
                payload
            );
            log.debug("好友上线通知已推送给用户 {}: 好友 {} 上线", uid, friendUid);
        } catch (Exception e) {
            log.error("WebSocket推送失败: {}", e.getMessage());
        }
    }
    
    /**
     * 发送好友离线通知
     */
    public void sendFriendOfflineNotification(Long uid, Long friendUid) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "FRIEND_OFFLINE");
        payload.put("friendUid", friendUid);
        payload.put("timestamp", System.currentTimeMillis());
        
        try {
            messagingTemplate.convertAndSendToUser(
                uid.toString(),
                "/topic/notifications",
                payload
            );
        } catch (Exception e) {
            log.error("WebSocket推送失败: {}", e.getMessage());
        }
    }
    
    /**
     * 发送好友游戏中通知
     */
    public void sendFriendInGameNotification(Long uid, Long friendUid, Long gameId, String gameName) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "FRIEND_IN_GAME");
        payload.put("friendUid", friendUid);
        payload.put("gameId", gameId);
        payload.put("gameName", gameName);
        payload.put("timestamp", System.currentTimeMillis());
        
        try {
            messagingTemplate.convertAndSendToUser(
                uid.toString(),
                "/topic/notifications",
                payload
            );
            log.debug("好友游戏中通知已推送给用户 {}: 好友 {} 正在玩 {}", uid, friendUid, gameName);
        } catch (Exception e) {
            log.error("WebSocket推送失败: {}", e.getMessage());
        }
    }
    
    /**
     * 获取用户的通知列表（分页）
     */
    public Page<UserNotification> getNotifications(Long uid, int page, int size) {
        return notificationRepository.findByUidOrderByCreatedAtDesc(
            uid, 
            PageRequest.of(page - 1, size)
        );
    }
    
    /**
     * 获取未读通知数量
     */
    public long getUnreadCount(Long uid) {
        return notificationRepository.countByUidAndIsRead(uid, 0);
    }
    
    /**
     * 标记通知为已读
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.markAsRead(notificationId);
        log.info("通知 {} 已标记为已读", notificationId);
    }
    
    /**
     * 标记所有通知为已读
     */
    @Transactional
    public void markAllAsRead(Long uid) {
        var unreadNotifications = notificationRepository.findByUidAndIsReadOrderByCreatedAtDesc(uid, 0);
        unreadNotifications.forEach(notification -> {
            notification.setIsRead(1);
            notificationRepository.save(notification);
        });
        log.info("用户 {} 的所有通知已标记为已读，共 {} 条", uid, unreadNotifications.size());
    }
    
    /**
     * Send game invitation notification
     */
    @Transactional
    public void sendGameInvitationNotification(Long inviteeUid, Long inviterUid, Long gameId, String message) {
        // 1. Save to database
        UserNotification notification = new UserNotification();
        notification.setUid(inviteeUid);
        notification.setType("GAME_INVITATION");
        notification.setTitle("游戏邀请");
        notification.setContent(message != null ? message : "邀请你一起玩游戏");
        notification.setFromUid(inviterUid);
        notificationRepository.save(notification);
        
        // 2. WebSocket real-time push
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "GAME_INVITATION");
        payload.put("inviterUid", inviterUid);
        payload.put("gameId", gameId);
        payload.put("message", notification.getContent());
        payload.put("notificationId", notification.getId());
        payload.put("timestamp", System.currentTimeMillis());
        
        try {
            messagingTemplate.convertAndSendToUser(
                inviteeUid.toString(),
                "/topic/notifications",
                payload
            );
            log.info("游戏邀请通知已推送给用户 {}: 来自用户 {}", inviteeUid, inviterUid);
        } catch (Exception e) {
            log.error("WebSocket推送失败: {}", e.getMessage());
        }
    }
}
