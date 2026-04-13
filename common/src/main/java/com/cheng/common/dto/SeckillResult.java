package com.cheng.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 抢购结果 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeckillResult {
    
    private Boolean success;  // 是否成功
    private String message;  // 消息
    private Integer remainingStock;  // 剩余库存
    private LocalDateTime nextSeckillTime;  // 下次抢购时间
    private Boolean hasParticipated;  // 是否已参与
}
