package com.cheng.mall.controller;

import com.cheng.common.dto.CommonResponse;
import com.cheng.mall.entity.Notification;
import com.cheng.mall.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 通知控制器
 */
@Slf4j
@RestController
@RequestMapping("/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    /**
     * 获取用户通知列表
     * GET /api/notifications?userId=1&unreadOnly=false&page=0&size=20
     */
    @GetMapping
    public CommonResponse<Map<String, Object>> getNotifications(
            @RequestParam Long userId,
            @RequestParam(required = false, defaultValue = "false") Boolean unreadOnly,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        try {
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
            
            Page<Notification> notificationPage;
            if (unreadOnly) {
                notificationPage = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId, pageRequest);
            } else {
                notificationPage = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageRequest);
            }
            
            // 统计未读数量
            long unreadCount = notificationRepository.countByUserIdAndIsReadFalse(userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("list", notificationPage.getContent());
            result.put("total", notificationPage.getTotalElements());
            result.put("unreadCount", unreadCount);
            result.put("page", page);
            result.put("size", size);
            
            return CommonResponse.success(result);
        } catch (Exception e) {
            log.error("获取通知列表失败", e);
            return CommonResponse.error("获取通知列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 标记通知为已读
     * PUT /api/notifications/{id}/read
     */
    @PutMapping("/{id}/read")
    public CommonResponse<Void> markAsRead(@PathVariable Long id) {
        try {
            Notification notification = notificationRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("通知不存在"));
            
            notification.setIsRead(true);
            notificationRepository.save(notification);
            
            return CommonResponse.success(null);
        } catch (Exception e) {
            log.error("标记已读失败", e);
            return CommonResponse.error("标记已读失败：" + e.getMessage());
        }
    }
    
    /**
     * 标记所有通知为已读
     * PUT /api/notifications/mark-all-read?userId=1
     */
    @PutMapping("/mark-all-read")
    public CommonResponse<Void> markAllAsRead(@RequestParam Long userId) {
        try {
            notificationRepository.markAllAsRead(userId);
            return CommonResponse.success(null);
        } catch (Exception e) {
            log.error("全部标记已读失败", e);
            return CommonResponse.error("全部标记已读失败：" + e.getMessage());
        }
    }
    
    /**
     * 删除通知
     * DELETE /api/notifications/{id}
     */
    @DeleteMapping("/{id}")
    public CommonResponse<Void> deleteNotification(@PathVariable Long id) {
        try {
            notificationRepository.deleteById(id);
            return CommonResponse.success(null);
        } catch (Exception e) {
            log.error("删除通知失败", e);
            return CommonResponse.error("删除通知失败：" + e.getMessage());
        }
    }
    
    /**
     * 创建测试通知（仅用于测试）
     * POST /api/notifications/test
     */
    @PostMapping("/test")
    public CommonResponse<Notification> createTestNotification(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "system") String type,
            @RequestParam(defaultValue = "测试通知") String title,
            @RequestParam(defaultValue = "这是一条测试通知消息，用于验证展开/收起功能是否正常工作。当消息内容超过100个字符时，应该显示展开按钮。点击展开按钮后，可以看到完整的消息内容。再次点击收起按钮，消息会恢复为预览模式，只显示前两行内容。这样可以保持页面整洁，同时让用户可以选择性地查看详细内容。Steam风格的通知中心采用了深色主题，蓝色高亮，整体风格与游戏平台保持一致。") String message) {
        try {
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setType(type);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setIsRead(false);
            
            Notification saved = notificationRepository.save(notification);
            log.info("创建测试通知成功: id={}, userId={}", saved.getId(), userId);
            return CommonResponse.success(saved);
        } catch (Exception e) {
            log.error("创建测试通知失败", e);
            return CommonResponse.error("创建测试通知失败：" + e.getMessage());
        }
    }
}
