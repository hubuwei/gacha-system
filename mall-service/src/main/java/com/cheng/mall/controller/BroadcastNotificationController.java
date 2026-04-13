package com.cheng.mall.controller;

import com.cheng.common.dto.CommonResponse;
import com.cheng.mall.service.BroadcastNotificationService;
import com.cheng.mall.service.EmailNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 广播通知控制器（管理员功能）
 */
@Slf4j
@RestController
@RequestMapping("/admin/notifications")
@CrossOrigin(origins = "*")
public class BroadcastNotificationController {
    
    @Autowired
    private BroadcastNotificationService broadcastService;
    
    @Autowired
    private EmailNotificationService emailNotificationService;
    
    /**
     * 一键广播促销消息给所有用户（小铃铛广播模式）
     * 
     * POST /api/admin/notifications/broadcast-promotion
     * 
     * 请求体示例：
     * {
     *   "title": "🎉 春季大促开启！",
     *   "content": "全场游戏低至3折，限时7天！快来选购心仪的游戏吧~",
     *   "gameId": 1,
     *   "type": "promotion"  // promotion/system/order
     * }
     */
    @PostMapping("/broadcast-promotion")
    public CommonResponse<String> broadcastPromotion(@RequestBody Map<String, Object> request) {
        try {
            String title = (String) request.get("title");
            String content = (String) request.get("content");
            Long gameId = request.get("gameId") != null ? 
                Long.valueOf(request.get("gameId").toString()) : null;
            Long relatedOrderId = request.get("relatedOrderId") != null ? 
                Long.valueOf(request.get("relatedOrderId").toString()) : null;
            String type = (String) request.getOrDefault("type", "promotion");
            
            // 参数验证
            if (title == null || title.trim().isEmpty()) {
                return CommonResponse.error("标题不能为空");
            }
            if (content == null || content.trim().isEmpty()) {
                return CommonResponse.error("内容不能为空");
            }
            
            log.info("收到广播请求: title={}, gameId={}, orderId={}, type={}", title, gameId, relatedOrderId, type);
            
            // 执行广播
            broadcastService.broadcastPromotion(title, content, gameId, relatedOrderId, type);
            
            return CommonResponse.success("广播发送成功，所有用户将收到通知");
            
        } catch (Exception e) {
            log.error("广播发送失败", e);
            return CommonResponse.error("广播发送失败：" + e.getMessage());
        }
    }
    
    /**
     * 向已绑定邮箱的用户广播
     * 
     * POST /api/admin/notifications/broadcast-to-email-users
     */
    @PostMapping("/broadcast-to-email-users")
    public CommonResponse<String> broadcastToEmailUsers(@RequestBody Map<String, Object> request) {
        try {
            String title = (String) request.get("title");
            String content = (String) request.get("content");
            Long gameId = request.get("gameId") != null ? 
                Long.valueOf(request.get("gameId").toString()) : null;
            
            if (title == null || title.trim().isEmpty()) {
                return CommonResponse.error("标题不能为空");
            }
            if (content == null || content.trim().isEmpty()) {
                return CommonResponse.error("内容不能为空");
            }
            
            log.info("收到邮箱用户广播请求: title={}, gameId={}", title, gameId);
            
            broadcastService.broadcastToEmailUsers(title, content, gameId);
            
            return CommonResponse.success("已向所有邮箱用户发送通知");
            
        } catch (Exception e) {
            log.error("邮箱用户广播失败", e);
            return CommonResponse.error("广播发送失败：" + e.getMessage());
        }
    }
    
    /**
     * 手动触发愿望单折扣通知
     * 
     * POST /api/admin/notifications/send-discount-notification
     * 
     * 请求体示例：
     * {
     *   "gameId": 1,
     *   "oldPrice": 298.00,
     *   "newPrice": 198.00,
     *   "discountRate": 33
     * }
     */
    @PostMapping("/send-discount-notification")
    public CommonResponse<String> sendDiscountNotification(@RequestBody Map<String, Object> request) {
        try {
            Long gameId = Long.valueOf(request.get("gameId").toString());
            BigDecimal oldPrice = new BigDecimal(request.get("oldPrice").toString());
            BigDecimal newPrice = new BigDecimal(request.get("newPrice").toString());
            Integer discountRate = Integer.valueOf(request.get("discountRate").toString());
            
            // 参数验证
            if (gameId == null || gameId <= 0) {
                return CommonResponse.error("游戏ID无效");
            }
            if (oldPrice == null || oldPrice.compareTo(BigDecimal.ZERO) <= 0) {
                return CommonResponse.error("原价必须大于0");
            }
            if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) <= 0) {
                return CommonResponse.error("现价必须大于0");
            }
            if (discountRate == null || discountRate < 0 || discountRate > 100) {
                return CommonResponse.error("折扣率必须在0-100之间");
            }
            
            log.info("收到折扣通知请求: gameId={}, oldPrice={}, newPrice={}, discountRate={}", 
                gameId, oldPrice, newPrice, discountRate);
            
            // 异步发送折扣通知邮件
            emailNotificationService.sendDiscountNotification(gameId, oldPrice, newPrice, discountRate);
            
            return CommonResponse.success("折扣通知已发送，邮件将异步发送给愿望单用户");
            
        } catch (Exception e) {
            log.error("发送折扣通知失败", e);
            return CommonResponse.error("发送失败：" + e.getMessage());
        }
    }
}
