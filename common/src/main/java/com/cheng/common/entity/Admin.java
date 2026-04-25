package com.cheng.common.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Admin Entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "admins")
public class Admin {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;
    
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
    
    @Column(name = "real_name", length = 50)
    private String realName;
    
    @Column(name = "email", unique = true, length = 100)
    private String email;
    
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;
    
    @Column(name = "role", length = 20)
    private String role = "admin";
    
    @Column(name = "permissions", columnDefinition = "JSON")
    private String permissions;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;
    
    @Column(name = "last_login_ip", length = 50)
    private String lastLoginIp;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
