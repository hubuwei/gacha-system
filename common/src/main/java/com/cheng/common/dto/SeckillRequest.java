package com.cheng.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 抢购请求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeckillRequest {
    
    private Long productId;  // 商品 ID
    private Long userId;  // 用户 ID
}
