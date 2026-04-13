package com.cheng.auth.service;

import com.cheng.common.entity.User;
import com.cheng.common.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户大区选择服务（已废弃，使用 Region 替代）
 */
@Service
public class UserServerService {
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 选择大区（已废弃）
     */
    @Transactional
    public User selectServer(Long userId, String serverCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 不再设置 currentServer
        return userRepository.save(user);
    }
}
