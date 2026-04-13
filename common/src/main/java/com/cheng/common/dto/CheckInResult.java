package com.cheng.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

/**
 * 签到结果 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckInResult {
    
    /**
     * 是否签到成功
     */
    private Boolean success;
    
    /**
     * 提示信息
     */
    private String message;
    
    /**
     * 本次签到日期
     */
    private LocalDate checkInDate;
    
    /**
     * 连续签到天数
     */
    private Integer consecutiveDays;
    
    /**
     * 本次获得积分
     */
    private Integer rewardPoints;
    
    /**
     * 本次获得虚拟货币
     */
    private Double rewardBalance;
    
    /**
     * 额外奖励描述（如第 7 天、30 天奖励）
     */
    private String bonusMessage;
    
    /**
     * 距离下次特殊奖励还差多少天
     */
    private Integer nextBonusDays;
}
