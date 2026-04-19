package com.cheng.mall.controller;

import com.cheng.common.dto.CommonResponse;
import com.cheng.mall.entity.Game;
import com.cheng.mall.mq.producer.MessageProducer;
import com.cheng.mall.repository.GameRepository;
import com.cheng.mall.service.EmailNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 游戏管理控制器（管理员功能）
 */
@Slf4j
@RestController
@RequestMapping("/admin/games")
@CrossOrigin(origins = "*")
public class GameAdminController {
    
    @Autowired
    private GameRepository gameRepository;
    
    @Autowired(required = false)
    private MessageProducer messageProducer;
    
    @Autowired(required = false)
    private EmailNotificationService emailNotificationService;
    
    @Autowired(required = false)
    private com.cheng.mall.mq.producer.NotificationProducer notificationProducer;
    
    @Autowired(required = false)
    private com.cheng.mall.repository.WishlistRepository wishlistRepository;
    
    /**
     * 游戏上架
     * 
     * POST /api/admin/games/{gameId}/publish
     */
    @PostMapping("/{gameId}/publish")
    public CommonResponse<String> publishGame(@PathVariable Long gameId) {
        try {
            Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("游戏不存在"));
            
            // 更新游戏状态
            game.setIsOnSale(true);
            gameRepository.save(game);
            
            // 发送广播消息（ES 更新 + 缓存清除）
            if (messageProducer != null) {
                messageProducer.sendGameEventMessage(
                    gameId, 
                    "on", 
                    game.getTitle()
                );
                
                // 记录审计日志
                messageProducer.sendAuditLogMessage(
                    0L, // TODO: 获取管理员 ID
                    "GAME_PUBLISH",
                    "game:" + gameId,
                    "上架游戏: " + game.getTitle()
                );
            }
            
            log.info("游戏已上架: gameId={}, title={}", gameId, game.getTitle());
            
            return CommonResponse.success("游戏已上架");
            
        } catch (Exception e) {
            log.error("游戏上架失败", e);
            return CommonResponse.error("游戏上架失败：" + e.getMessage());
        }
    }
    
    /**
     * 游戏下架
     * 
     * POST /api/admin/games/{gameId}/unpublish
     */
    @PostMapping("/{gameId}/unpublish")
    public CommonResponse<String> unpublishGame(@PathVariable Long gameId) {
        try {
            Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("游戏不存在"));
            
            // 更新游戏状态
            game.setIsOnSale(false);
            gameRepository.save(game);
            
            // 发送广播消息（ES 更新 + 缓存清除）
            if (messageProducer != null) {
                messageProducer.sendGameEventMessage(
                    gameId, 
                    "off", 
                    game.getTitle()
                );
                
                // 记录审计日志
                messageProducer.sendAuditLogMessage(
                    0L, // TODO: 获取管理员 ID
                    "GAME_UNPUBLISH",
                    "game:" + gameId,
                    "下架游戏: " + game.getTitle()
                );
            }
            
            log.info("游戏已下架: gameId={}, title={}", gameId, game.getTitle());
            
            return CommonResponse.success("游戏已下架");
            
        } catch (Exception e) {
            log.error("游戏下架失败", e);
            return CommonResponse.error("游戏下架失败：" + e.getMessage());
        }
    }
    
    /**
     * 更新游戏折扣（管理员）
     * 
     * PUT /api/admin/games/{gameId}/discount
     * 
     * @param gameId 游戏ID
     * @param request 请求体包含：discountRate（折扣率0-100）、currentPrice（当前价格）
     */
    @PutMapping("/{gameId}/discount")
    public CommonResponse<String> updateGameDiscount(
            @PathVariable Long gameId,
            @RequestBody Map<String, Object> request) {
        try {
            log.info("收到折扣更新请求: gameId={}, request={}", gameId, request);
            
            // 获取参数
            Object discountRateObj = request.get("discountRate");
            Object currentPriceObj = request.get("currentPrice");
            
            log.info("参数值: discountRateObj={}, currentPriceObj={}", discountRateObj, currentPriceObj);
            
            // 参数空值检查
            if (discountRateObj == null) {
                log.warn("缺少必要参数: discountRateObj={}", discountRateObj);
                return CommonResponse.error("缺少必要参数：discountRate");
            }
            
            Integer discountRate;
            BigDecimal currentPrice = null;
            
            // 安全转换 discountRate
            if (discountRateObj instanceof Integer) {
                discountRate = (Integer) discountRateObj;
            } else if (discountRateObj instanceof Number) {
                discountRate = ((Number) discountRateObj).intValue();
            } else {
                return CommonResponse.error("折扣率格式错误");
            }
            
            // 参数验证
            if (discountRate < 0 || discountRate > 100) {
                return CommonResponse.error("折扣率必须在0-100之间");
            }
            
            // 获取原游戏信息
            Game oldGame = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("游戏不存在"));
            
            BigDecimal basePrice = oldGame.getBasePrice();
            
            // 如果没有提供 currentPrice，根据折扣率自动计算
            if (currentPriceObj == null) {
                // 根据原价和折扣率计算现价
                currentPrice = basePrice.multiply(
                    new BigDecimal(100 - discountRate)
                ).divide(new BigDecimal(100), 2, java.math.RoundingMode.HALF_UP);
                
                log.info("未提供 currentPrice，自动计算: basePrice={}, discount={}%, currentPrice={}",
                    basePrice, discountRate, currentPrice);
            } else {
                // 使用提供的价格
                try {
                    currentPrice = new BigDecimal(currentPriceObj.toString());
                } catch (NumberFormatException e) {
                    return CommonResponse.error("价格格式错误");
                }
                
                if (currentPrice.compareTo(BigDecimal.ZERO) < 0) {
                    return CommonResponse.error("价格不能为负数");
                }
            }
            
            BigDecimal oldPrice = oldGame.getCurrentPrice();
            int oldDiscount = oldGame.getDiscountRate();
            
            // 更新折扣
            oldGame.setDiscountRate(discountRate);
            oldGame.setCurrentPrice(currentPrice);
            oldGame.setIsOnSale(discountRate > 0);
            gameRepository.save(oldGame);
            
            // 如果折扣率增加且大于0，发送站内通知和邮件
            if (discountRate > oldDiscount && discountRate > 0) {
                log.info("检测到折扣提升，准备发送通知: gameId={}, oldDiscount={}%, newDiscount={}%", 
                    gameId, oldDiscount, discountRate);
                
                // 异步发送邮件通知给愿望单用户
                if (emailNotificationService != null) {
                    emailNotificationService.sendDiscountNotification(
                        gameId, oldPrice, currentPrice, discountRate
                    );
                }
                
                // 通过MQ发送站内通知给所有愿望单用户
                try {
                    if (wishlistRepository != null && notificationProducer != null) {
                        List<com.cheng.mall.entity.Wishlist> wishlists = 
                            wishlistRepository.findByGameIdAndNotifyDiscountTrue(gameId);
                        
                        for (com.cheng.mall.entity.Wishlist wishlist : wishlists) {
                            String title = "🎉 您关注的游戏降价了！";
                            String content = String.format("%s 现在享受 %d%% 折扣，仅需 ¥%s（原价 ¥%s）",
                                oldGame.getTitle(), discountRate, currentPrice, oldPrice);
                            
                            notificationProducer.sendNotificationMessage(
                                wishlist.getUserId(),
                                "promotion",
                                title,
                                content,
                                gameId,
                                null,
                                "game"
                            );
                        }
                        
                        log.info("已发送 {} 条站内通知", wishlists.size());
                    }
                } catch (Exception e) {
                    log.error("发送站内通知失败", e);
                }
                
                log.info("已触发折扣通知邮件发送: gameId={}", gameId);
            }
            
            // 记录审计日志
            if (messageProducer != null) {
                messageProducer.sendAuditLogMessage(
                    0L, // TODO: 获取管理员 ID
                    "GAME_DISCOUNT_UPDATE",
                    "game:" + gameId,
                    String.format("更新游戏折扣: %s, 原价¥%s(%d%%), 新价¥%s(%d%%)",
                        oldGame.getTitle(), oldPrice, oldDiscount, currentPrice, discountRate)
                );
            }
            
            log.info("游戏折扣更新成功: gameId={}, title={}, discount={}%, price={}",
                gameId, oldGame.getTitle(), discountRate, currentPrice);
            
            return CommonResponse.success("折扣更新成功，已通知相关用户");
            
        } catch (NumberFormatException e) {
            log.error("参数格式错误", e);
            return CommonResponse.error("参数格式错误：" + e.getMessage());
        } catch (Exception e) {
            log.error("更新游戏折扣失败", e);
            return CommonResponse.error("更新折扣失败：" + e.getMessage());
        }
    }
    
    /**
     * 测试邮件发送（仅用于调试）
     * 
     * GET /api/admin/games/test-email
     */
    @GetMapping("/test-email")
    public CommonResponse<String> testEmail() {
        try {
            log.info("开始测试邮件发送...");
            
            // 获取游戏信息
            Game game = gameRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("游戏不存在"));
            
            // 发送测试邮件
            if (emailNotificationService != null) {
                emailNotificationService.sendDiscountNotification(
                    1L, 
                    game.getBasePrice(), 
                    game.getCurrentPrice(), 
                    game.getDiscountRate()
                );
                
                log.info("测试邮件发送请求已提交");
                return CommonResponse.success("测试邮件已发送，请检查邮箱（包括垃圾箱）");
            } else {
                return CommonResponse.error("邮件服务未启用");
            }
            
        } catch (Exception e) {
            log.error("测试邮件发送失败", e);
            return CommonResponse.error("测试失败：" + e.getMessage());
        }
    }
}
