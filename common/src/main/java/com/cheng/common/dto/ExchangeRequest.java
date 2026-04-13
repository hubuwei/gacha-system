package com.cheng.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 兑换请求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRequest {
    
    /**
     * 用户 ID
     */
    private Long userId;
    
    /**
     * 物品 ID
     */
    private Long itemId;
}
