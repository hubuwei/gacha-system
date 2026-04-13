package com.cheng.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 抽奖请求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GachaRequest {
    
    private Long userId;
    private String poolCode;
    private Integer drawCount = 1;  // 默认抽 1 次
}
