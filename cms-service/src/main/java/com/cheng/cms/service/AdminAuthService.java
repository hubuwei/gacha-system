package com.cheng.cms.service;

import com.cheng.common.entity.Admin;
import com.cheng.common.repository.AdminRepository;
import com.cheng.cms.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AdminAuthService {
    
    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * Admin login
     */
    @Transactional(readOnly = true)
    public Map<String, Object> login(String username, String password, String ip) {
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));
        
        if (!admin.getIsActive()) {
            throw new RuntimeException("账号已被禁用");
        }
        
        if (!passwordEncoder.matches(password, admin.getPasswordHash())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // Update last login info
        admin.setLastLoginTime(LocalDateTime.now());
        admin.setLastLoginIp(ip);
        adminRepository.save(admin);
        
        // Generate JWT token
        String token = jwtUtil.generateToken(admin.getId(), admin.getUsername(), admin.getRole());
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("adminId", admin.getId());
        result.put("username", admin.getUsername());
        result.put("realName", admin.getRealName());
        result.put("role", admin.getRole());
        result.put("email", admin.getEmail());
        result.put("avatarUrl", admin.getAvatarUrl());
        
        return result;
    }
    
    /**
     * Get admin info by ID
     */
    @Transactional(readOnly = true)
    public Admin getAdminById(Long adminId) {
        return adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("管理员不存在"));
    }
    
    /**
     * Test password matching
     */
    public boolean passwordMatches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
    
    /**
     * Encode password
     */
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
