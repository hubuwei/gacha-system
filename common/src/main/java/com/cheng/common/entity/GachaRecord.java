package com.cheng.common.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 抽奖记录实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "gacha_records")
public class GachaRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private Integer regionId;  // 大区 ID
    
    @Column(nullable = false)
    private Integer itemId;  // 抽中道具 ID（关联 gacha_config.id）
    
    @Column(nullable = false)
    private Boolean isPity = false;  // 是否触发保底
    
    @Column(nullable = false, precision = 10, scale = 2)
    private Double cost = 10.0;  // 本次消耗金额
    
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
