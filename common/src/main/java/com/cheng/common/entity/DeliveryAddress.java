package com.cheng.common.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 收货地址实体类
 */
@Entity
@Table(name = "delivery_address")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAddress {
    
    /**
     * 地址 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 兑换记录 ID
     */
    @Column(nullable = false, unique = true)
    private Long exchangeRecordId;
    
    /**
     * 用户 ID
     */
    @Column(nullable = false)
    private Long userId;
    
    /**
     * 收货人姓名
     */
    @Column(nullable = false, length = 50)
    private String recipientName;
    
    /**
     * 联系电话
     */
    @Column(nullable = false, length = 20)
    private String phoneNumber;
    
    /**
     * 省份
     */
    @Column(nullable = false, length = 50)
    private String province;
    
    /**
     * 城市
     */
    @Column(nullable = false, length = 50)
    private String city;
    
    /**
     * 区县
     */
    @Column(nullable = false, length = 50)
    private String district;
    
    /**
     * 详细地址
     */
    @Column(nullable = false, length = 500)
    private String detailAddress;
    
    /**
     * 创建时间
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @Column(nullable = false)
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
