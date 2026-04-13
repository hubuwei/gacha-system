package com.cheng.mall.service;

import com.cheng.common.entity.User;
import com.cheng.common.repository.UserRepository;
import com.cheng.mall.entity.Game;
import com.cheng.mall.entity.Wishlist;
import com.cheng.mall.repository.GameRepository;
import com.cheng.mall.repository.WishlistRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 邮件通知服务
 */
@Slf4j
@Service
public class EmailNotificationService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private WishlistRepository wishlistRepository;
    
    @Autowired
    private GameRepository gameRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 发送折扣通知邮件
     */
    @Async
    public void sendDiscountNotification(Long gameId, BigDecimal oldPrice, BigDecimal newPrice, int discountRate) {
        try {
            // 获取需要通知的用户列表
            List<Wishlist> wishlists = wishlistRepository.findByNotifyDiscountTrue();
            
            for (Wishlist wishlist : wishlists) {
                if (!wishlist.getGameId().equals(gameId)) {
                    continue;
                }
                
                User user = userRepository.findById(wishlist.getUserId()).orElse(null);
                if (user == null || user.getEmail() == null || user.getEmail().isEmpty()) {
                    continue;
                }
                
                Game game = gameRepository.findById(gameId).orElse(null);
                if (game == null) {
                    continue;
                }
                
                // 发送邮件
                sendDiscountEmail(user.getEmail(), user.getUsername(), game, oldPrice, newPrice, discountRate);
            }
            
            log.info("折扣通知邮件发送完成，游戏ID: {}, 折扣: {}%", gameId, discountRate);
        } catch (Exception e) {
            log.error("发送折扣通知邮件失败", e);
        }
    }
    
    /**
     * 发送单封折扣邮件（HTML格式）
     */
    private void sendDiscountEmail(String toEmail, String username, Game game, 
                                   BigDecimal oldPrice, BigDecimal newPrice, int discountRate) {
        try {
            javax.mail.internet.MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom("1712133303@qq.com");  // 必须与授权用户一致
            helper.setTo(toEmail);
            helper.setSubject("🎮 【折扣提醒】" + game.getTitle() + " 现在打折啦！");
            
            String htmlContent = buildDiscountEmailContent(username, game, oldPrice, newPrice, discountRate);
            helper.setText(htmlContent, true);  // true 表示 HTML 格式
            
            mailSender.send(mimeMessage);
            log.info("折扣邮件发送成功: {}", toEmail);
        } catch (Exception e) {
            log.error("发送折扣邮件失败: {}", toEmail, e);
        }
    }
    
    /**
     * 构建折扣邮件内容（Steam风格HTML邮件）
     */
    private String buildDiscountEmailContent(String username, Game game, 
                                             BigDecimal oldPrice, BigDecimal newPrice, int discountRate) {
        StringBuilder sb = new StringBuilder();
        
        // HTML邮件头
        sb.append("<!DOCTYPE html>\n");
        sb.append("<html>\n<head>\n");
        sb.append("<meta charset=\"UTF-8\">\n");
        sb.append("<style>\n");
        sb.append("body { font-family: 'Microsoft YaHei', Arial, sans-serif; background-color: #1b2838; color: #c6d4df; margin: 0; padding: 20px; }\n");
        sb.append(".container { max-width: 600px; margin: 0 auto; background-color: #171a21; border-radius: 8px; overflow: hidden; }\n");
        sb.append(".header { background: linear-gradient(135deg, #66c0f4 0%, #1b2838 100%); padding: 30px; text-align: center; }\n");
        sb.append(".header h1 { color: #ffffff; margin: 0; font-size: 24px; }\n");
        sb.append(".content { padding: 30px; }\n");
        sb.append(".game-info { background-color: #1b2838; padding: 20px; border-radius: 6px; margin: 20px 0; }\n");
        sb.append(".game-title { color: #66c0f4; font-size: 20px; margin-bottom: 15px; }\n");
        sb.append(".price-box { display: flex; align-items: center; gap: 15px; margin: 15px 0; }\n");
        sb.append(".old-price { color: #738895; text-decoration: line-through; font-size: 16px; }\n");
        sb.append(".discount-badge { background-color: #4c6b22; color: #a4d007; padding: 5px 10px; border-radius: 4px; font-weight: bold; font-size: 18px; }\n");
        sb.append(".new-price { color: #a4d007; font-size: 24px; font-weight: bold; }\n");
        sb.append(".savings { color: #a4d007; font-size: 14px; margin-top: 10px; }\n");
        sb.append(".description { color: #8f98a0; line-height: 1.6; margin: 20px 0; }\n");
        sb.append(".cta-button { display: inline-block; background: linear-gradient(to right, #47bfff 0%, #1a44c2 100%); color: #ffffff; padding: 15px 40px; text-decoration: none; border-radius: 6px; font-weight: bold; margin: 20px 0; }\n");
        sb.append(".footer { background-color: #171a21; padding: 20px; text-align: center; color: #8f98a0; font-size: 12px; border-top: 1px solid #2a475e; }\n");
        sb.append("</style>\n</head>\n<body>\n");
        
        // 邮件主体
        sb.append("<div class=\"container\">\n");
        sb.append("<div class=\"header\">\n");
        sb.append("<h1>🎮 您愿望单上的游戏正在打折！</h1>\n");
        sb.append("</div>\n\n");
        
        sb.append("<div class=\"content\">\n");
        sb.append("<p style=\"color: #c6d4df; font-size: 16px;\">亲爱的 <strong>").append(username).append("</strong>，</p>\n");
        sb.append("<p style=\"color: #8f98a0;\">好消息！您愿望单中的游戏现在正在特价促销：</p>\n\n");
        
        // 游戏信息卡片
        sb.append("<div class=\"game-info\">\n");
        sb.append("<div class=\"game-title\">").append(game.getTitle()).append("</div>\n\n");
        
        // 价格信息
        sb.append("<div class=\"price-box\">\n");
        sb.append("<span class=\"old-price\">¥").append(oldPrice).append("</span>\n");
        sb.append("<span class=\"discount-badge\">-").append(discountRate).append("%</span>\n");
        sb.append("<span class=\"new-price\">¥").append(newPrice).append("</span>\n");
        sb.append("</div>\n\n");
        
        // 节省金额
        sb.append("<div class=\"savings\">💰 立省 ¥").append(oldPrice.subtract(newPrice)).append("</div>\n\n");
        
        // 游戏简介
        if (game.getShortDescription() != null && !game.getShortDescription().isEmpty()) {
            sb.append("<div class=\"description\">");
            sb.append("<strong>📝 游戏简介：</strong><br>");
            sb.append(game.getShortDescription());
            sb.append("</div>\n\n");
        }
        
        // 购买按钮
        sb.append("<div style=\"text-align: center; margin: 30px 0;\">\n");
        sb.append("<a href=\"http://111.228.12.167/games/").append(game.getId()).append("\" class=\"cta-button\">立即查看并购买</a>\n");
        sb.append("</div>\n\n");
        
        sb.append("<p style=\"color: #8f98a0; font-size: 14px;\">⏰ 优惠限时，抓紧时间行动吧！</p>\n");
        sb.append("</div>\n\n");
        
        // 页脚
        sb.append("<div class=\"footer\">\n");
        sb.append("<p>此邮件由 VICE CITY STORE 自动发送</p>\n");
        sb.append("<p>如果您不想再收到此类邮件，可以在<a href=\"http://111.228.12.167/profile\" style=\"color: #66c0f4;\">个人中心</a>中关闭折扣通知</p>\n");
        sb.append("<p style=\"margin-top: 15px; color: #4a5f7a;\">© 2026 VICE CITY STORE. All rights reserved.</p>\n");
        sb.append("</div>\n\n");
        
        sb.append("</div>\n</body>\n</html>");
        
        return sb.toString();
    }
}
