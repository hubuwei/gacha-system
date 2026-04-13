package com.cheng.auth.service;

import com.cheng.common.dto.LoginRequest;
import com.cheng.common.dto.LoginResponse;
import com.cheng.common.entity.User;
import com.cheng.common.entity.Wallet;
import com.cheng.common.repository.UserRepository;
import com.cheng.common.repository.WalletRepository;
import com.cheng.common.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 用户认证服务
 */
@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private WalletRepository walletRepository;
    
    /**
     * 用户登录
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        System.out.println("=== 尝试登录 ===");
        System.out.println("用户名：" + request.getUsername());
        
        // 查找用户
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    System.out.println("错误：用户不存在");
                    return new RuntimeException("用户名或密码错误");
                });
        
        System.out.println("找到用户 ID: " + user.getId());
        
        // 验证密码
        String inputPasswordHash = encodePassword(request.getPassword());
        System.out.println("输入的密码哈希：" + inputPasswordHash);
        System.out.println("数据库中的密码：" + user.getPassword());
        
        if (!inputPasswordHash.equals(user.getPassword())) {
            System.out.println("错误：密码不匹配");
            throw new RuntimeException("用户名或密码错误");
        }
        
        System.out.println("密码验证通过");
        
        // 生成 Token
        String token = null;
        try {
            token = JwtUtil.generateToken(user.getId(), user.getUsername());
            System.out.println("生成的 Token: " + token);
        } catch (Exception e) {
            System.out.println("生成 Token 时发生错误：" + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Token 生成失败：" + e.getMessage());
        }
        
        // 获取钱包信息
        Wallet wallet = walletRepository.findById(user.getId()).orElse(null);
        Integer points = wallet != null ? wallet.getPoints() : 0;
        Double balance = wallet != null ? wallet.getBalance() : 0.0;
        
        System.out.println("=== 登录成功 ===");
        
        // 返回响应
        return new LoginResponse(
                user.getId(),
                user.getUsername(),
                token,
                points,
                balance,
                null  // 不再使用 currentServer
        );
    }
    
    /**
     * 用户注册
     */
    @Transactional
    public User register(String username, String password, String phone, String email) {
        // 验证用户名格式
        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("用户名不能为空");
        }
        
        if (username.length() < 3 || username.length() > 20) {
            throw new RuntimeException("用户名长度必须在 3-20 个字符之间");
        }
        
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            throw new RuntimeException("用户名只能包含字母、数字和下划线");
        }
        
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 验证密码强度
        if (password == null || password.isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }
        
        if (password.length() < 6 || password.length() > 50) {
            throw new RuntimeException("密码长度必须在 6-50 个字符之间");
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(encodePassword(password));
        
        // 设置选填字段
        if (phone != null && !phone.trim().isEmpty()) {
            user.setPhone(phone.trim());
            user.setPhoneVerified(false);
        }
        
        if (email != null && !email.trim().isEmpty()) {
            user.setEmail(email.trim());
            user.setEmailVerified(false);
        }
        
        // 设置默认值
        user.setAccountStatus(1);
        user.setUserLevel(1);
        user.setExperiencePoints(0);
        
        User savedUser = userRepository.save(user);
        
        // 创建初始钱包（赠送 1000 积分）
        Wallet wallet = new Wallet();
        wallet.setUserId(savedUser.getId());
        // Wallet 已移除@MapsId 和 User 关联，不需要设置 user
        wallet.setPoints(1000);
        wallet.setBalance(0.0);
        walletRepository.save(wallet);
        
        return savedUser;
    }
    
    /**
     * 密码加密（简化版，实际应该用 BCrypt）
     */
    private String encodePassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("加密失败", e);
        }
    }
}
