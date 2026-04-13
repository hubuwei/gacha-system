package com.cheng.common.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String username;
    
    @Column(name = "password_hash", nullable = false, length = 255)
    private String password;
    
    @Column(unique = true, length = 20)
    private String phone;
    
    @Column(name = "phone_verified")
    private Boolean phoneVerified = false;
    
    @Column(unique = true, length = 100)
    private String email;
    
    @Column(name = "email_verified")
    private Boolean emailVerified = false;
    
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;
    
    @Column(length = 50)
    private String nickname;
    
    @Column(length = 200)
    private String signature;
    
    @Column(name = "account_status")
    private Integer accountStatus = 1;
    
    @Column(name = "user_level")
    private Integer userLevel = 1;
    
    @Column(name = "experience_points")
    private Integer experiencePoints = 0;
    
    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;
    
    @Column(name = "last_login_ip", length = 50)
    private String lastLoginIp;
    
    @Column(name = "login_type", length = 20)
    private String loginType;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
