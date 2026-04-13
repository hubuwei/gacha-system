package com.cheng.common.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 兑换物品实体类
 */
@Entity
@Table(name = "exchange_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeItem {
    
    /**
     * 物品 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 物品名称
     */
    @Column(nullable = false, length = 100)
    private String name;
    
    /**
     * 物品描述
     */
    @Column(length = 500)
    private String description;
    
    /**
     * 物品图标 URL
     */
    @Column(length = 500)
    private String iconUrl;
    
    /**
     * 所需积分
     */
    @Column(nullable = false)
    private Integer requiredPoints;
    
    /**
     * 每日库存总量
     */
    @Column(nullable = false)
    private Integer totalStock;
    
    /**
     * 当前剩余库存
     */
    @Column(nullable = false)
    private Integer currentStock;
    
    /**
     * 是否上架
     */
    @Column(nullable = false)
    private Boolean enabled = true;
    
    /**
     * 排序权重（数字越大越靠前）
     */
    @Column(nullable = false)
    private Integer sortWeight = 0;
    
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
