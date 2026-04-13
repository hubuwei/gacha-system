package com.cheng.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 抽奖批量结果 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GachaBatchResult {
    
    private List<GachaResult> results;
    private Integer totalConsumePoints;  // 消耗的积分
    private Integer newPointsBalance;    // 剩余积分
    private Double newBalance;           // 剩余余额
}
