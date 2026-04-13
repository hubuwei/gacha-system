package com.cheng.game.service;

import com.cheng.common.dto.SeckillProductDTO;
import com.cheng.common.dto.SeckillRequest;
import com.cheng.common.dto.SeckillResult;
import java.util.List;

/**
 * 抢购服务接口
 */
public interface SeckillService {
    
    /**
     * 获取所有活跃的抢购商品
     */
    List<SeckillProductDTO> getActiveProducts();
    
    /**
     * 获取单个抢购商品详情
     */
    SeckillProductDTO getProductDetail(Long productId);
    
    /**
     * 参与抢购
     */
    SeckillResult participate(SeckillRequest request);
    
    /**
     * 计算下次抢购时间
     */
    java.time.LocalDateTime calculateNextSeckillTime(Integer intervalHours);
    
    /**
     * 检查用户是否已参与本轮抢购
     */
    Boolean hasUserParticipated(Long userId, Long productId, Integer intervalHours);
}
