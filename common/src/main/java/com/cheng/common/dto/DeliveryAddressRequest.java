package com.cheng.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 收货地址请求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAddressRequest {
    
    /**
     * 兑换记录 ID
     */
    private Long exchangeRecordId;
    
    /**
     * 收货人姓名
     */
    private String recipientName;
    
    /**
     * 联系电话
     */
    private String phoneNumber;
    
    /**
     * 省份
     */
    private String province;
    
    /**
     * 城市
     */
    private String city;
    
    /**
     * 区县
     */
    private String district;
    
    /**
     * 详细地址
     */
    private String detailAddress;
}
