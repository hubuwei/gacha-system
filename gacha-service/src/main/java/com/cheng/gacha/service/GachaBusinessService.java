package com.cheng.gacha.service;

import com.cheng.common.dto.GachaBatchResult;
import com.cheng.common.dto.GachaRequest;
import com.cheng.common.dto.GachaResult;
import com.cheng.common.entity.GachaConfig;
import com.cheng.common.entity.GachaRecord;
import com.cheng.common.entity.Wallet;
import com.cheng.common.repository.GachaConfigRepository;
import com.cheng.common.repository.GachaRecordRepository;
import com.cheng.common.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 抽奖业务服务
 */
@Service
public class GachaBusinessService {
    
    @Autowired
    private WalletRepository walletRepository;
    
    @Autowired
    private GachaRecordRepository gachaRecordRepository;
    
    @Autowired
    private GachaConfigRepository gachaConfigRepository;
    
    @Autowired
    private GachaService gachaService;
    
    /**
     * 执行抽奖
     */
    @Transactional
    public GachaBatchResult executeGacha(GachaRequest request) {
        // 获取用户钱包
        Wallet wallet = walletRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("用户不存在或没有钱包"));
        
        // 计算需要的金额（每次抽奖消耗 10 元，只扣余额）
        double totalCost = request.getDrawCount() * 10.0;
        
        // 检查余额是否足够
        if (wallet.getBalance() < totalCost) {
            throw new RuntimeException("余额不足");
        }
        
        // 扣除余额（不扣积分）
        wallet.setBalance(wallet.getBalance() - totalCost);
        
        // 执行抽奖
        List<GachaResult> results = new ArrayList<>();
        int consecutiveCount = 0;  // 连续未中 SSR 次数
        
        for (int i = 0; i < request.getDrawCount(); i++) {
            Integer itemId = gachaService.draw(consecutiveCount);
            
            // 获取道具信息
            GachaConfig config = gachaConfigRepository.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("道具不存在"));
            
            // 判断是否触发保底
            boolean isPity = config.getPityThreshold() != null && 
                            consecutiveCount >= config.getPityThreshold() - 1;
            
            // 创建结果
            GachaResult result = new GachaResult(
                config.getItemName(),    // itemName
                parseRarityToLevel(config.getRarity()),  // rarityLevel
                0,  // consumePoints: 不消耗积分，固定为 0
                isPity
            );
            results.add(result);
            
            // 更新连续计数
            if ("SSR".equals(config.getRarity())) {
                consecutiveCount = 0;
            } else {
                consecutiveCount++;
            }
            
            // 创建抽奖记录
            GachaRecord record = new GachaRecord();
            record.setUserId(request.getUserId());
            record.setRegionId(1);  // 默认大区 ID
            record.setItemId(itemId);
            record.setIsPity(isPity);
            record.setCost(10.0);
            gachaRecordRepository.save(record);
        }
        
        // 保存钱包数据
        walletRepository.save(wallet);
        
        // 返回结果
        GachaBatchResult batchResult = new GachaBatchResult();
        batchResult.setResults(results);
        batchResult.setTotalConsumePoints(0);  // 不消耗积分
        batchResult.setNewPointsBalance(wallet.getPoints());  // 返回当前积分（不变）
        batchResult.setNewBalance(wallet.getBalance());  // 返回剩余余额
        
        return batchResult;
    }
    
    /**
     * 解析稀有度到等级（前端显示用）
     * 注意：前端期望的值：5=SSR, 4=SR, 3=R, 2=N
     */
    private Integer parseRarityToLevel(String rarity) {
        switch (rarity) {
            case "SSR": return 5;  // 5 星
            case "SR": return 4;   // 4 星
            case "R": return 3;    // 3 星
            case "N": return 2;    // 2 星
            default: return 1;     // 1 星
        }
    }
}
