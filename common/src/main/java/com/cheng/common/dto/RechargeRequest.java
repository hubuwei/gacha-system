package com.cheng.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 充值请求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RechargeRequest {
    
    private Long userId;
    private Double amount;
}
