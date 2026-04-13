package com.cheng.common.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 兑换记录实体类
 */
@Entity
@Table(name = "exchange_record")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRecord {
    
    /**
     * 兑换记录 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 用户 ID
     */
    @Column(nullable = false)
    private Long userId;
    
    /**
     * 物品 ID
     */
    @Column(nullable = false)
    private Long itemId;
    
    /**
     * 消耗积分
     */
    @Column(nullable = false)
    private Integer usedPoints;
    
    /**
     * 兑换状态：0-待填写地址，1-已填写地址，2-已发货，3-已完成，-1-已取消
     */
    @Column(nullable = false)
    private Integer status = 0;
    
    /**
     * 兑换日期
     */
    @Column(nullable = false)
    private LocalDate exchangeDate;
    
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
        if (exchangeDate == null) {
            exchangeDate = LocalDate.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
