package com.cheng.game.service.impl;

import com.cheng.common.dto.SeckillProductDTO;
import com.cheng.common.dto.SeckillRequest;
import com.cheng.common.dto.SeckillResult;
import com.cheng.common.entity.SeckillProduct;
import com.cheng.common.entity.SeckillRecord;
import com.cheng.common.entity.Wallet;
import com.cheng.common.repository.SeckillProductRepository;
import com.cheng.common.repository.SeckillRecordRepository;
import com.cheng.common.repository.WalletRepository;
import com.cheng.game.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 抢购服务实现类
 */
@Service
public class SeckillServiceImpl implements SeckillService {
    
    @Autowired
    private SeckillProductRepository seckillProductRepository;
    
    @Autowired
    private SeckillRecordRepository seckillRecordRepository;
    
    @Autowired
    private WalletRepository walletRepository;
    
    @Override
    public List<SeckillProductDTO> getActiveProducts() {
        List<SeckillProduct> products = seckillProductRepository.findActiveProducts();
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public SeckillProductDTO getProductDetail(Long productId) {
        SeckillProduct product = seckillProductRepository.findActiveById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在或已下架");
        }
        return convertToDTO(product);
    }
    
    @Override
    @Transactional
    public SeckillResult participate(SeckillRequest request) {
        try {
            // 1. 验证商品
            SeckillProduct product = seckillProductRepository.findActiveById(request.getProductId());
            if (product == null) {
                return new SeckillResult(false, "商品不存在或已下架", 0, null, false);
            }
            
            // 2. 检查库存
            if (product.getRemainingStock() <= 0) {
                return new SeckillResult(false, "库存已售罄", 0, null, false);
            }
            
            // 3. 检查用户是否已参与本轮抢购
            LocalDateTime currentRoundStart = calculateCurrentRoundStart(product.getIntervalHours());
            Integer participatedCount = seckillRecordRepository.countUserSeckillInRound(
                    request.getUserId(), request.getProductId(), currentRoundStart);
            
            if (participatedCount >= product.getMaxPerUser()) {
                SeckillRecord lastRecord = seckillRecordRepository
                        .findFirstByUserIdAndProductIdOrderBySeckillTimeDesc(
                                request.getUserId(), request.getProductId())
                        .orElse(null);
                
                LocalDateTime nextTime = null;
                if (lastRecord != null) {
                    nextTime = calculateNextSeckillTime(product.getIntervalHours());
                }
                
                return new SeckillResult(false, "您已参与过本轮抢购", 
                        product.getRemainingStock(), nextTime, true);
            }
            
            // 4. 检查用户积分
            Wallet wallet = walletRepository.findByUserId(request.getUserId());
            if (wallet == null || wallet.getPoints() < product.getSeckillPoints()) {
                return new SeckillResult(false, "积分不足", product.getRemainingStock(), null, false);
            }
            
            // 5. 再次检查库存（防止并发超卖）
            if (product.getRemainingStock() <= 0) {
                return new SeckillResult(false, "库存已售罄", 0, null, false);
            }
            
            // 6. 直接抢购成功（先到先得，无概率）
            // 扣除积分
            wallet.setPoints(wallet.getPoints() - product.getSeckillPoints());
            walletRepository.save(wallet);
            
            // 减少库存
            product.setRemainingStock(product.getRemainingStock() - 1);
            seckillProductRepository.save(product);
            
            // 创建抢购记录
            SeckillRecord record = new SeckillRecord();
            record.setUserId(request.getUserId());
            record.setProductId(request.getProductId());
            record.setPoints(product.getSeckillPoints());
            record.setStatus("SUCCESS");
            record.setSeckillTime(LocalDateTime.now());
            record.setRemark("抢购成功");
            seckillRecordRepository.save(record);
            
            LocalDateTime nextSeckillTime = calculateNextSeckillTime(product.getIntervalHours());
            return new SeckillResult(true, "恭喜抢购成功！", 
                    product.getRemainingStock(), nextSeckillTime, true);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new SeckillResult(false, "抢购异常：" + e.getMessage(), 0, null, false);
        }
    }
    
    @Override
    public LocalDateTime calculateNextSeckillTime(Integer intervalHours) {
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        
        // 计算下一个整点时刻
        int nextHour = (hour / intervalHours) * intervalHours + intervalHours;
        if (nextHour >= 24) {
            nextHour = 0;
        }
        
        return now.withHour(nextHour).withMinute(0).withSecond(0).withNano(0);
    }
    
    /**
     * 计算当前轮次的开始时间
     */
    private LocalDateTime calculateCurrentRoundStart(Integer intervalHours) {
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        int roundStartHour = (hour / intervalHours) * intervalHours;
        return now.withHour(roundStartHour).withMinute(0).withSecond(0).withNano(0);
    }
    
    @Override
    public Boolean hasUserParticipated(Long userId, Long productId, Integer intervalHours) {
        LocalDateTime currentRoundStart = calculateCurrentRoundStart(intervalHours);
        Integer count = seckillRecordRepository.countUserSeckillInRound(userId, productId, currentRoundStart);
        return count > 0;
    }
    
    private SeckillProductDTO convertToDTO(SeckillProduct product) {
        SeckillProductDTO dto = new SeckillProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setOriginalPrice(product.getOriginalPrice());
        dto.setSeckillPoints(product.getSeckillPoints());
        dto.setTotalStock(product.getTotalStock());
        dto.setRemainingStock(product.getRemainingStock());
        dto.setMaxPerUser(product.getMaxPerUser());
        dto.setIntervalHours(product.getIntervalHours());
        dto.setStartTime(product.getStartTime());
        dto.setEndTime(product.getEndTime());
        dto.setIsActive(product.getIsActive());
        return dto;
    }
    
    /**
     * 定时任务：每 2 小时重置库存
     */
    @Scheduled(cron = "0 0 */2 * * ?") // 每 2 小时执行一次
    @Transactional
    public void resetSeckillStock() {
        try {
            List<SeckillProduct> products = seckillProductRepository.findActiveProducts();
            for (SeckillProduct product : products) {
                // 重置库存为总库存
                product.setRemainingStock(product.getTotalStock());
                seckillProductRepository.save(product);
                System.out.println("已重置商品 " + product.getName() + " 的库存为 " + product.getTotalStock());
            }
        } catch (Exception e) {
            System.err.println("重置库存失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
}
