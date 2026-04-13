package com.cheng.mall.service;

import com.cheng.mall.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 用户会话管理服务（基于 Redis）
 */
@Slf4j
@Service
public class SessionService {
    
    @Autowired
    private RedisUtil redisUtil;
    
    // Session key 前缀
    private static final String SESSION_KEY_PREFIX = "session:";
    // Session 过期时间：24小时
    private static final long SESSION_EXPIRE_HOURS = 24;
    
    /**
     * 创建会话
     * 
     * @param userId 用户ID
     * @return session token
     */
    public String createSession(Long userId) {
        String sessionToken = UUID.randomUUID().toString().replace("-", "");
        String sessionKey = SESSION_KEY_PREFIX + sessionToken;
        
        // 存储用户信息到 Redis
        redisUtil.hSet(sessionKey, "userId", userId);
        redisUtil.hSet(sessionKey, "createdAt", System.currentTimeMillis());
        
        // 设置过期时间
        redisUtil.expire(sessionKey, SESSION_EXPIRE_HOURS, TimeUnit.HOURS);
        
        log.info("创建会话: userId={}, token={}", userId, sessionToken);
        
        return sessionToken;
    }
    
    /**
     * 获取会话信息
     * 
     * @param sessionToken session token
     * @return 用户信息，null 表示会话不存在或已过期
     */
    public Map<Object, Object> getSession(String sessionToken) {
        if (sessionToken == null || sessionToken.isEmpty()) {
            return null;
        }
        
        String sessionKey = SESSION_KEY_PREFIX + sessionToken;
        Map<Object, Object> sessionData = redisUtil.hGetAll(sessionKey);
        
        if (sessionData == null || sessionData.isEmpty()) {
            log.debug("会话不存在或已过期: {}", sessionToken);
            return null;
        }
        
        // 刷新过期时间（滑动窗口）
        redisUtil.expire(sessionKey, SESSION_EXPIRE_HOURS, TimeUnit.HOURS);
        
        return sessionData;
    }
    
    /**
     * 获取用户ID
     * 
     * @param sessionToken session token
     * @return 用户ID，null 表示会话无效
     */
    public Long getUserId(String sessionToken) {
        Map<Object, Object> sessionData = getSession(sessionToken);
        if (sessionData == null) {
            return null;
        }
        
        Object userIdObj = sessionData.get("userId");
        if (userIdObj instanceof Integer) {
            return ((Integer) userIdObj).longValue();
        } else if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        }
        
        return null;
    }
    
    /**
     * 销毁会话（登出）
     * 
     * @param sessionToken session token
     */
    public void destroySession(String sessionToken) {
        if (sessionToken == null || sessionToken.isEmpty()) {
            return;
        }
        
        String sessionKey = SESSION_KEY_PREFIX + sessionToken;
        redisUtil.delete(sessionKey);
        
        log.info("销毁会话: {}", sessionToken);
    }
    
    /**
     * 检查会话是否有效
     * 
     * @param sessionToken session token
     * @return true-有效，false-无效
     */
    public boolean isValidSession(String sessionToken) {
        return getUserId(sessionToken) != null;
    }
}
