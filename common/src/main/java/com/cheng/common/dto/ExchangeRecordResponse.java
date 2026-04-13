package com.cheng.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 兑换记录响应 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRecordResponse {
    
    /**
     * 兑换记录 ID
     */
    private Long id;
    
    /**
     * 用户 ID
     */
    private Long userId;
    
    /**
     * 物品 ID
     */
    private Long itemId;
    
    /**
     * 物品名称
     */
    private String itemName;
    
    /**
     * 物品图标 URL
     */
    private String itemIconUrl;
    
    /**
     * 消耗积分
     */
    private Integer pointsCost;
    
    /**
     * 兑换状态：0-待填写地址，1-已填写地址，2-已发货，3-已完成，-1-已取消
     */
    private Integer status;
    
    /**
     * 兑换日期
     */
    private LocalDate exchangeDate;
    
    /**
     * 收货人姓名
     */
    private String recipientName;
    
    /**
     * 联系电话
     */
    private String phoneNumber;
    
    /**
     * 完整地址
     */
    private String fullAddress;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
