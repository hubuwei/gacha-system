package com.cheng.common.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 钱包实体类（虚拟币 + 积分）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "wallets")
public class Wallet {
    
    @Id
    @Column(name = "user_id")
    private Long userId;
    
    // 移除@MapsId 和@OneToOne，直接通过 userId 作为主键
    // @OneToOne(fetch = FetchType.LAZY)
    // @MapsId
    // @JoinColumn(name = "user_id")
    // private User user;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private Double balance = 0.0;  // 虚拟货币余额
    
    @Column(nullable = false)
    private Integer points = 0;  // 积分
    
    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
