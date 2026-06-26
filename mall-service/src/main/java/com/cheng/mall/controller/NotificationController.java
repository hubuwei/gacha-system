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
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    
    private final NotificationService notificationService;
    
    /**
     * 获取用户的通知列表（分页）
     */
    @GetMapping("")
    public CommonResponse<Page<UserNotification>> getNotifications(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<UserNotification> notifications = notificationService.getNotifications(userId, page, size);
        return CommonResponse.success(notifications);
    }
    
    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread-count")
    public CommonResponse<Long> getUnreadCount(@RequestParam Long userId) {
        long count = notificationService.getUnreadCount(userId);
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
    @PostMapping("/mark-all-read")
    public CommonResponse<Void> markAllAsRead(@RequestParam Long userId) {
        notificationService.markAllAsRead(userId);
        return CommonResponse.success(null);
    }
    
    /**
     * 删除通知
     */
    @DeleteMapping("/{id}")
    public CommonResponse<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return CommonResponse.success(null);
    }
}
