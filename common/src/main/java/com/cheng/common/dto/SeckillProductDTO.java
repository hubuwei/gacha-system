package com.cheng.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 抢购商品 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeckillProductDTO {
    
    private Long id;
    private String name;  // 商品名称
    private String description;  // 商品描述
    private Double originalPrice;  // 原价
    private Integer seckillPoints;  // 抢购所需积分
    private Integer totalStock;  // 总库存
    private Integer remainingStock;  // 剩余库存
    private Integer maxPerUser;  // 每个用户限购数量
    private Integer intervalHours;  // 抢购间隔（小时）
    private LocalDateTime startTime;  // 开始时间
    private LocalDateTime endTime;  // 结束时间
    private Boolean isActive;  // 是否启用
}
