package com.cheng.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

/**
 * 签到状态 DTO（用于查询今日是否已签到）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckInStatus {
    
    /**
     * 用户 ID
     */
    private Long userId;
    
    /**
     * 今日是否已签到
     */
    private Boolean checkedInToday;
    
    /**
     * 当前连续签到天数
     */
    private Integer consecutiveDays;
    
    /**
     * 最后签到日期
     */
    private LocalDate lastCheckInDate;
    
    /**
     * 距离下次特殊奖励还差多少天
     */
    private Integer nextBonusDays;
}
