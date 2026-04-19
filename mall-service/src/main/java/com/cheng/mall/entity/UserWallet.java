package com.cheng.mall.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户钱包实体
 */
@Data
@Entity
@Table(name = "user_wallets")
public class UserWallet {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;
    
    @Column(name = "frozen_balance", precision = 10, scale = 2)
    private BigDecimal frozenBalance = BigDecimal.ZERO;
    
    @Column(name = "total_recharge", precision = 10, scale = 2)
    private BigDecimal totalRecharge = BigDecimal.ZERO;
    
    @Column(name = "total_consumed", precision = 10, scale = 2)
    private BigDecimal totalConsumed = BigDecimal.ZERO;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}