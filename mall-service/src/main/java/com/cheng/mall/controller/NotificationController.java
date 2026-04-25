package com.cheng.mall.controller;

import com.cheng.common.dto.CommonResponse;
import com.cheng.mall.entity.UserNotification;
import com.cheng.mall.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

/**
 * 通知控制器
 */
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {
    
    private final NotificationService notificationService;
    
    /**
     * 获取用户的通知列表（分页）
     */
    @GetMapping("/list")
    public CommonResponse<Page<UserNotification>> getNotifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        // TODO: 从JWT Token获取当前用户ID
        Long uid = 1L;
        Page<UserNotification> notifications = notificationService.getNotifications(uid, page, size);
        return CommonResponse.success(notifications);
    }
    
    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread-count")
    public CommonResponse<Long> getUnreadCount() {
        // TODO: 从JWT Token获取当前用户ID
        Long uid = 1L;
        long count = notificationService.getUnreadCount(uid);
        return CommonResponse.success(count);
    }
    
    /**
     * 标记通知为已读
     */
    @PostMapping("/{id}/read")
    public CommonResponse<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return CommonResponse.success(null);
    }
    
    /**
     * 标记所有通知为已读
     */
    @PostMapping("/read-all")
    public CommonResponse<Void> markAllAsRead() {
        // TODO: 从JWT Token获取当前用户ID
        Long uid = 1L;
        notificationService.markAllAsRead(uid);
        return CommonResponse.success(null);
    }
}
