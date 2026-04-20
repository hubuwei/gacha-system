package com.cheng.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务
 */
@Service
public class VerificationCodeService {
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @Autowired(required = false)
    private JavaMailSender mailSender;
    
    private static final String CODE_PREFIX = "verify:code:";
    private static final int CODE_LENGTH = 6;
    private static final long EXPIRE_TIME = 5; // 5分钟过期
    
    // 内存缓存，用于Redis不可用时的降级处理
    private final java.util.Map<String, java.util.Map<String, Object>> codeCache = new java.util.concurrent.ConcurrentHashMap<>();
    
    /**
     * 生成6位随机验证码（数字+字母）
     */
    public String generateCode() {
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return code.toString();
    }
    
    /**
     * 发送验证码到手机
     */
    public void sendSmsCode(String phone) {
        String code = generateCode();
        String key = CODE_PREFIX + "sms:" + phone;
        
        // 检查是否频繁发送（60秒内只能发送一次）
        boolean isFrequent = false;
        try {
            if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
                Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                if (ttl != null && ttl > 0) {
                    throw new RuntimeException("验证码已发送，请" + ttl + "秒后再试");
                }
            }
        } catch (Exception e) {
            // Redis不可用，使用内存缓存检查
            java.util.Map<String, Object> codeInfo = codeCache.get(key);
            if (codeInfo != null) {
                long timestamp = (long) codeInfo.get("timestamp");
                if (System.currentTimeMillis() - timestamp < 60000) { // 60秒内
                    throw new RuntimeException("验证码已发送，请60秒后再试");
                }
            }
        }
        
        // TODO: 集成阿里云短信服务
        // 目前使用演示模式，将验证码打印到控制台
        System.out.println("========================================");
        System.out.println("【短信验证码 - 演示模式】");
        System.out.println("手机号: " + phone);
        System.out.println("验证码: " + code);
        System.out.println("有效期: 5分钟");
        System.out.println("提示: 配置阿里云AccessKey后可发送真实短信");
        System.out.println("========================================");
        
        // 存储到Redis，5分钟过期
        try {
            redisTemplate.opsForValue().set(key, code, EXPIRE_TIME, TimeUnit.MINUTES);
        } catch (Exception e) {
            // Redis不可用，使用内存缓存
            java.util.Map<String, Object> codeInfo = new java.util.HashMap<>();
            codeInfo.put("code", code);
            codeInfo.put("timestamp", System.currentTimeMillis());
            codeCache.put(key, codeInfo);
            System.out.println("Redis不可用，已使用内存缓存存储验证码");
        }
    }
    
    /**
     * 发送验证码到邮箱
     */
    public void sendEmailCode(String email) {
        sendEmailCode(email, "login"); // 默认用于登录
    }
    
    /**
     * 发送验证码到邮箱（指定用途）
     * @param email 邮箱地址
     * @param purpose 用途：login-登录, register-注册
     */
    public void sendEmailCode(String email, String purpose) {
        String code = generateCode();
        String key = CODE_PREFIX + "email:" + email;
        
        // 检查是否频繁发送（60秒内只能发送一次）
        boolean isFrequent = false;
        try {
            if (redisTemplate != null) {
                if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
                    Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                    if (ttl != null && ttl > 0) {
                        isFrequent = true;
                    }
                }
            }
        } catch (Exception e) {
            // Redis不可用，使用内存缓存检查
            System.out.println("Redis不可用，使用内存缓存检查频繁发送: " + e.getMessage());
            java.util.Map<String, Object> codeInfo = codeCache.get(key);
            if (codeInfo != null) {
                long timestamp = (long) codeInfo.get("timestamp");
                if (System.currentTimeMillis() - timestamp < 60000) { // 60秒内
                    isFrequent = true;
                }
            }
        }
        
        if (isFrequent) {
            throw new RuntimeException("验证码已发送，请60秒后再试");
        }
        
        // 根据用途设置不同的邮件内容
        String subject;
        String content;
        
        if ("register".equals(purpose)) {
            subject = "🎮 VICE CITY STORE - 注册验证码";
            content = "这是您此次注册的验证码：\n\n" +
                     "━━━━━━━━━━━━━━━━━━━━━━\n" +
                     "      " + code + "\n" +
                     "━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                     "• 验证码有效期：5分钟\n" +
                     "• 请勿将验证码告知他人\n" +
                     "• 如非本人操作，请忽略此邮件\n\n" +
                     "VICE CITY STORE 团队";
        } else {
            subject = "🎮 VICE CITY STORE - 登录验证码";
            content = "这是您此次登录的验证码：\n\n" +
                     "━━━━━━━━━━━━━━━━━━━━━━\n" +
                     "      " + code + "\n" +
                     "━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                     "• 验证码有效期：5分钟\n" +
                     "• 请勿将验证码告知他人\n" +
                     "• 如非本人操作，请忽略此邮件\n\n" +
                     "VICE CITY STORE 团队";
        }
        
        // 发送邮件
        if (mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom("1712133303@qq.com");
                message.setTo(email);
                message.setSubject(subject);
                message.setText(content);
                
                mailSender.send(message);
                System.out.println("✅ 验证码邮件已发送到: " + email + " (用途: " + purpose + ")");
            } catch (Exception e) {
                System.err.println("❌ 邮件发送失败: " + e.getMessage());
                // 即使邮件发送失败，也继续存储验证码（演示模式）
            }
        } else {
            // 演示模式：打印到控制台
            System.out.println("========================================");
            System.out.println("【邮箱验证码 - 演示模式】");
            System.out.println("邮箱: " + email);
            System.out.println("验证码: " + code);
            System.out.println("用途: " + ("register".equals(purpose) ? "注册" : "登录"));
            System.out.println("有效期: 5分钟");
            System.out.println("提示: 配置邮件服务后可发送真实邮件");
            System.out.println("========================================");
        }
        
        // 存储到Redis，严格5分钟过期
        try {
            if (redisTemplate != null) {
                redisTemplate.opsForValue().set(key, code, EXPIRE_TIME, TimeUnit.MINUTES);
                System.out.println("验证码已存储到Redis");
            } else {
                // Redis模板不可用，使用内存缓存
                System.out.println("Redis模板不可用，使用内存缓存存储验证码");
                java.util.Map<String, Object> codeInfo = new java.util.HashMap<>();
                codeInfo.put("code", code);
                codeInfo.put("timestamp", System.currentTimeMillis());
                codeCache.put(key, codeInfo);
                System.out.println("验证码已存储到内存缓存");
            }
        } catch (Exception e) {
            // Redis不可用，使用内存缓存
            System.out.println("Redis不可用，使用内存缓存存储验证码: " + e.getMessage());
            java.util.Map<String, Object> codeInfo = new java.util.HashMap<>();
            codeInfo.put("code", code);
            codeInfo.put("timestamp", System.currentTimeMillis());
            codeCache.put(key, codeInfo);
            System.out.println("验证码已存储到内存缓存");
        }
    }
    
    /**
     * 验证验证码
     */
    public boolean verifyCode(String type, String target, String code) {
        String key = CODE_PREFIX + type + ":" + target;
        String storedCode = null;
        
        try {
            if (redisTemplate != null) {
                storedCode = redisTemplate.opsForValue().get(key);
            }
        } catch (Exception e) {
            // Redis不可用，从内存缓存获取
            System.out.println("Redis不可用，从内存缓存获取验证码: " + e.getMessage());
            java.util.Map<String, Object> codeInfo = codeCache.get(key);
            if (codeInfo != null) {
                storedCode = (String) codeInfo.get("code");
                long timestamp = (long) codeInfo.get("timestamp");
                // 检查是否过期（5分钟）
                if (System.currentTimeMillis() - timestamp > 300000) {
                    codeCache.remove(key);
                    storedCode = null;
                }
            }
        }
        
        if (storedCode == null) {
            throw new RuntimeException("验证码已过期或不存在，请重新获取");
        }
        
        if (!storedCode.equalsIgnoreCase(code)) {
            throw new RuntimeException("验证码错误");
        }
        
        // 验证成功后删除验证码（一次性使用）
        try {
            if (redisTemplate != null) {
                redisTemplate.delete(key);
                System.out.println("验证码已从Redis删除");
            }
        } catch (Exception e) {
            // Redis不可用，从内存缓存删除
            System.out.println("Redis不可用，从内存缓存删除验证码: " + e.getMessage());
            codeCache.remove(key);
            System.out.println("验证码已从内存缓存删除");
        }
        
        return true;
    }
}
