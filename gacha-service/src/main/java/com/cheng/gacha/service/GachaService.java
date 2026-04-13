package com.cheng.gacha.service;

import com.cheng.common.entity.GachaConfig;
import com.cheng.common.repository.GachaConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

/**
 * 抽奖服务（含概率和保底机制）
 */
@Service
public class GachaService {
    
    private final Random random = new Random();
    
    @Autowired
    private GachaConfigRepository gachaConfigRepository;
    
    /**
     * 单次抽奖
     * @param consecutiveCount 连续未中 SSR 次数（用于保底）
     * @return 抽中的道具 ID
     */
    public Integer draw(Integer consecutiveCount) {
        // 获取所有配置
        List<GachaConfig> allConfigs = gachaConfigRepository.findAll();
        
        // 检查是否触发保底（SSR 90 抽保底）
        if (consecutiveCount >= 89) {
            // 触发保底，从 SSR 中随机选一个
            List<GachaConfig> ssrConfigs = gachaConfigRepository.findByRarityOrderByBaseProbDesc("SSR");
            if (!ssrConfigs.isEmpty()) {
                GachaConfig config = ssrConfigs.get(random.nextInt(ssrConfigs.size()));
                return config.getId();
            }
        }
        
        // 随机抽奖
        double rand = random.nextDouble();
        double cumulativeProb = 0.0;
        
        // 按稀有度排序：SSR -> SR -> R -> N
        for (String rarity : new String[]{"SSR", "SR", "R", "N"}) {
            List<GachaConfig> configs = gachaConfigRepository.findByRarityOrderByBaseProbDesc(rarity);
            for (GachaConfig config : configs) {
                cumulativeProb += config.getBaseProb();
                if (rand <= cumulativeProb) {
                    return config.getId();
                }
            }
        }
        
        // 默认返回最后一个
        return allConfigs.get(allConfigs.size() - 1).getId();
    }
}
