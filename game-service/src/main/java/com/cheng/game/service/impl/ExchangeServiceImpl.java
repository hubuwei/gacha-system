package com.cheng.game.service.impl;

import com.cheng.common.dto.ExchangeRequest;
import com.cheng.common.entity.ExchangeItem;
import com.cheng.common.entity.ExchangeRecord;
import com.cheng.common.entity.Wallet;
import com.cheng.common.repository.ExchangeItemRepository;
import com.cheng.common.repository.ExchangeRecordRepository;
import com.cheng.common.repository.WalletRepository;
import com.cheng.game.service.ExchangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 兑换服务实现类
 */
@Service
public class ExchangeServiceImpl implements ExchangeService {
    
    private final ExchangeItemRepository exchangeItemRepository;
    private final ExchangeRecordRepository exchangeRecordRepository;
    private final WalletRepository walletRepository;
    
    @Autowired
    public ExchangeServiceImpl(
            ExchangeItemRepository exchangeItemRepository,
            ExchangeRecordRepository exchangeRecordRepository,
            WalletRepository walletRepository) {
        this.exchangeItemRepository = exchangeItemRepository;
        this.exchangeRecordRepository = exchangeRecordRepository;
        this.walletRepository = walletRepository;
    }
    
    @Override
    public List<ExchangeItem> getAllExchangeItems() {
        return exchangeItemRepository.findByEnabledTrueOrderBySortWeightDescIdAsc();
    }
    
    @Override
    public ExchangeItem getExchangeItemById(Long itemId) {
        return exchangeItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("物品不存在"));
    }
    
    @Override
    @Transactional
    public ExchangeRecord exchange(ExchangeRequest request) {
        Long userId = request.getUserId();
        Long itemId = request.getItemId();
        
        // 1. 验证物品是否存在
        ExchangeItem item = getExchangeItemById(itemId);
        
        // 2. 检查物品是否上架
        if (!item.getEnabled()) {
            throw new RuntimeException("物品已下架");
        }
        
        // 3. 检查库存是否充足
        if (item.getCurrentStock() <= 0) {
            throw new RuntimeException("库存不足");
        }
        
        // 4. 检查用户积分是否足够
        Wallet wallet = walletRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户钱包不存在"));
        
        if (wallet.getPoints() < item.getRequiredPoints()) {
            throw new RuntimeException("积分不足");
        }
        
        // 5. 检查用户今日是否已兑换该物品（可选限制）
        long todayExchangeCount = exchangeRecordRepository.countByUserIdAndExchangeDate(userId, LocalDate.now());
        if (todayExchangeCount >= 3) { // 每日最多兑换 3 次
            throw new RuntimeException("今日兑换次数已达上限");
        }
        
        // 6. 扣减库存
        item.setCurrentStock(item.getCurrentStock() - 1);
        exchangeItemRepository.save(item);
        
        // 7. 扣除用户积分
        wallet.setPoints(wallet.getPoints() - item.getRequiredPoints());
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);
        
        // 8. 创建兑换记录
        ExchangeRecord record = new ExchangeRecord();
        record.setUserId(userId);
        record.setItemId(itemId);
        record.setUsedPoints(item.getRequiredPoints());
        record.setStatus(0); // 待填写地址
        record.setExchangeDate(LocalDate.now());
        
        exchangeRecordRepository.save(record);
        
        return record;
    }
    
    @Override
    public List<ExchangeRecord> getUserExchangeRecords(Long userId) {
        return exchangeRecordRepository.findByUserIdOrderByExchangeDateDescCreatedAtDesc(userId);
    }
    
    /**
     * 每日 0 点重置库存
     * 使用 Spring Schedule 定时任务
     */
    @Scheduled(cron = "0 0 0 * * ?") // 每天 0 点执行
    @Transactional
    public void resetDailyStock() {
        System.out.println("=== 开始执行每日库存重置 ===");
        
        List<ExchangeItem> items = exchangeItemRepository.findAll();
        
        int count = 0;
        for (ExchangeItem item : items) {
            // 重置为总库存
            item.setCurrentStock(item.getTotalStock());
            item.setUpdatedAt(LocalDateTime.now());
            exchangeItemRepository.save(item);
            count++;
        }
        
        System.out.println("=== 库存重置完成，共重置 " + count + " 个物品 ===");
    }
}
