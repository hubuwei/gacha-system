package com.cheng.game.service;

import com.cheng.common.dto.ExchangeRequest;
import com.cheng.common.entity.ExchangeItem;
import com.cheng.common.entity.ExchangeRecord;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 兑换服务接口
 */
@Service
public interface ExchangeService {
    
    /**
     * 获取所有可兑换物品
     */
    List<ExchangeItem> getAllExchangeItems();
    
    /**
     * 根据 ID 获取物品
     */
    ExchangeItem getExchangeItemById(Long itemId);
    
    /**
     * 执行兑换
     */
    ExchangeRecord exchange(ExchangeRequest request);
    
    /**
     * 查询用户兑换记录
     */
    List<ExchangeRecord> getUserExchangeRecords(Long userId);
    
    /**
     * 每日 0 点重置库存
     */
    void resetDailyStock();
}
