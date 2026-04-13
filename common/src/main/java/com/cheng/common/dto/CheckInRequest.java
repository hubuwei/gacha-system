package com.cheng.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

/**
 * 签到请求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckInRequest {
    
    /**
     * 用户 ID
     */
    private Long userId;
}
