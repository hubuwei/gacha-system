package com.cheng.common.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 抽奖配置实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "gacha_config")
public class GachaConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false, length = 50)
    private String itemName;  // 道具名称
    
    @Column(nullable = false, length = 10)
    private String rarity;  // 稀有度：N, R, SR, SSR
    
    @Column(nullable = false, precision = 5, scale = 4)
    private Double baseProb;  // 基础概率
    
    private Integer pityThreshold;  // 保底抽数
    
    @Column(nullable = false)
    private Boolean isPityGuaranteed = true;  // 是否保底必出
}
