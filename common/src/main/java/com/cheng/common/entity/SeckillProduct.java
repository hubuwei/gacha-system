package com.cheng.common.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 抢购商品实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "seckill_products")
public class SeckillProduct {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String name;  // 商品名称
    
    @Column(nullable = false, length = 500)
    private String description;  // 商品描述
    
    @Column(nullable = false, precision = 10, scale = 2)
    private Double originalPrice;  // 原价
    
    @Column(nullable = false)
    private Integer seckillPoints;  // 抢购所需积分
    
    @Column(nullable = false)
    private Integer totalStock;  // 总库存
    
    @Column(nullable = false)
    private Integer remainingStock;  // 剩余库存
    
    @Column(nullable = false)
    private Integer maxPerUser;  // 每个用户限购数量
    
    @Column(nullable = false)
    private Integer intervalHours;  // 抢购间隔（小时）
    
    @Column(nullable = false)
    private LocalDateTime startTime;  // 开始时间
    
    @Column(nullable = false)
    private LocalDateTime endTime;  // 结束时间
    
    @Column(nullable = false)
    private Boolean isActive;  // 是否启用
    
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
