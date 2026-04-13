package com.cheng.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 抽奖结果 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GachaResult {
    
    private String itemName;       // 道具名称（用于前端显示）
    private Integer rarityLevel;   // 稀有度等级（用于前端显示）
    private Integer consumePoints; // 消耗的积分（固定为 0，不消耗积分）
    private Boolean isGuaranteed;  // 是否保底触发
}
