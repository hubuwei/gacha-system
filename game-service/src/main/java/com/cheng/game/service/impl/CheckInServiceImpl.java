package com.cheng.game.service.impl;

import com.cheng.common.dto.CheckInResult;
import com.cheng.common.dto.CheckInStatus;
import com.cheng.common.entity.CheckInRecord;
import com.cheng.common.entity.Wallet;
import com.cheng.common.repository.CheckInRecordRepository;
import com.cheng.common.repository.WalletRepository;
import com.cheng.game.service.CheckInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 签到服务实现类
 */
@Service
public class CheckInServiceImpl implements CheckInService {
    
    private final CheckInRecordRepository checkInRecordRepository;
    private final WalletRepository walletRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    
    // Redis Key 前缀
    private static final String CHECK_IN_KEY_PREFIX = "check_in:";
    private static final String CONSECUTIVE_DAYS_KEY_PREFIX = "consecutive_days:";
    
    /**
     * 构造函数注入
     */
    @Autowired
    public CheckInServiceImpl(
            CheckInRecordRepository checkInRecordRepository,
            WalletRepository walletRepository,
            RedisTemplate<String, Object> redisTemplate) {
        this.checkInRecordRepository = checkInRecordRepository;
        this.walletRepository = walletRepository;
        this.redisTemplate = redisTemplate;
    }
    
    // 基础奖励
    private static final int BASE_POINTS = 10;        // 每天签到送 10 积分
    private static final double BASE_BALANCE = 1.0;   // 每天签到送 1 元虚拟货币
    
    // 特殊奖励
    private static final int DAY_7_BONUS_POINTS = 88;      // 第 7 天额外送 88 积分
    private static final double DAY_7_BONUS_BALANCE = 10.0; // 第 7 天额外送 10 元
    private static final int DAY_30_BONUS_POINTS = 888;    // 第 30 天额外送 888 积分
    private static final double DAY_30_BONUS_BALANCE = 100.0; // 第 30 天额外送 100 元
    private static final String DAY_30_SPECIAL_PRIZE = "手机兑换券"; // 30 天特殊奖励
    
    @Override
    @Transactional
    public CheckInResult checkIn(Long userId) {
        LocalDate today = LocalDate.now();
        String checkInKey = CHECK_IN_KEY_PREFIX + userId + ":" + today;
        
        // 1. 检查今日是否已签到（使用 Redis）
        Boolean hasCheckedIn = redisTemplate.hasKey(checkInKey);
        if (Boolean.TRUE.equals(hasCheckedIn)) {
            return new CheckInResult(
                false,
                "今日已签到，明天再来哦！",
                today,
                getCurrentConsecutiveDays(userId),
                0,
                0.0,
                null,
                calculateNextBonusDays(userId)
            );
        }
        
        // 2. 计算连续签到天数
        int consecutiveDays = calculateConsecutiveDays(userId);
        
        // 3. 计算本次奖励
        int rewardPoints = BASE_POINTS;
        double rewardBalance = BASE_BALANCE;
        StringBuilder bonusMessage = new StringBuilder();
        
        // 第 7 天奖励
        if (consecutiveDays == 7) {
            rewardPoints += DAY_7_BONUS_POINTS;
            rewardBalance += DAY_7_BONUS_BALANCE;
            bonusMessage.append("恭喜！连续签到 7 天，额外获得 ").append(DAY_7_BONUS_POINTS)
                       .append(" 积分和 ").append(DAY_7_BONUS_BALANCE).append(" 元！");
        }
        
        // 第 30 天奖励
        if (consecutiveDays == 30) {
            rewardPoints += DAY_30_BONUS_POINTS;
            rewardBalance += DAY_30_BONUS_BALANCE;
            if (bonusMessage.length() > 0) {
                bonusMessage.append(" ");
            }
            bonusMessage.append("恭喜！连续签到 30 天，获得 ").append(DAY_30_BONUS_POINTS)
                       .append(" 积分、").append(DAY_30_BONUS_BALANCE).append(" 元和 ")
                       .append(DAY_30_SPECIAL_PRIZE).append("！");
        }
        
        // 4. 保存签到记录到数据库
        CheckInRecord record = new CheckInRecord();
        record.setUserId(userId);
        record.setCheckInDate(today);
        record.setRewardPoints(rewardPoints);
        record.setRewardBalance(rewardBalance);
        record.setConsecutiveDays(consecutiveDays);
        record.setCreatedAt(LocalDateTime.now());
        checkInRecordRepository.save(record);
        
        // 5. 更新用户钱包
        updateWallet(userId, rewardPoints, rewardBalance);
        
        // 6. 写入 Redis（设置过期时间，避免数据堆积）
        redisTemplate.opsForValue().set(checkInKey, "1", 30, TimeUnit.DAYS);
        redisTemplate.opsForValue().set(
            CONSECUTIVE_DAYS_KEY_PREFIX + userId, 
            consecutiveDays, 
            30, TimeUnit.DAYS
        );
        
        // 7. 返回结果
        String message = consecutiveDays == 1 ? 
            "签到成功！" : 
            "签到成功！已连续签到 " + consecutiveDays + " 天";
        
        return new CheckInResult(
            true,
            message,
            today,
            consecutiveDays,
            rewardPoints,
            rewardBalance,
            bonusMessage.length() > 0 ? bonusMessage.toString() : null,
            calculateNextBonusDays(userId)
        );
    }
    
    @Override
    public CheckInStatus getCheckInStatus(Long userId) {
        LocalDate today = LocalDate.now();
        String checkInKey = CHECK_IN_KEY_PREFIX + userId + ":" + today;
        
        // 检查今日是否已签到
        Boolean hasCheckedIn = redisTemplate.hasKey(checkInKey);
        boolean checkedInToday = Boolean.TRUE.equals(hasCheckedIn);
        
        // 获取连续签到天数
        int consecutiveDays = getCurrentConsecutiveDays(userId);
        
        // 获取最后签到日期
        LocalDate lastCheckInDate = getLastCheckInDate(userId);
        
        // 计算距离下次特殊奖励的天数
        int nextBonusDays = calculateNextBonusDays(userId);
        
        return new CheckInStatus(
            userId,
            checkedInToday,
            consecutiveDays,
            lastCheckInDate,
            nextBonusDays
        );
    }
    
    /**
     * 计算连续签到天数
     */
    private int calculateConsecutiveDays(Long userId) {
        // 从数据库查询最近的签到记录
        var lastRecord = checkInRecordRepository.findTopByUserIdOrderByCheckInDateDesc(userId);
        
        if (lastRecord.isEmpty()) {
            // 首次签到
            return 1;
        }
        
        LocalDate lastDate = lastRecord.get().getCheckInDate();
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        
        // 如果上次签到是昨天或今天，继续累加
        if (lastDate.isEqual(yesterday) || lastDate.isEqual(today)) {
            return lastRecord.get().getConsecutiveDays() + 1;
        }
        
        // 断签了，重新从 1 开始
        return 1;
    }
    
    /**
     * 获取当前连续签到天数（从 Redis）
     */
    private int getCurrentConsecutiveDays(Long userId) {
        Object value = redisTemplate.opsForValue().get(CONSECUTIVE_DAYS_KEY_PREFIX + userId);
        if (value != null) {
            return Integer.parseInt(value.toString());
        }
        
        // Redis 中没有，从数据库获取
        var lastRecord = checkInRecordRepository.findTopByUserIdOrderByCheckInDateDesc(userId);
        return lastRecord.map(CheckInRecord::getConsecutiveDays).orElse(0);
    }
    
    /**
     * 获取最后签到日期
     */
    private LocalDate getLastCheckInDate(Long userId) {
        var lastRecord = checkInRecordRepository.findTopByUserIdOrderByCheckInDateDesc(userId);
        return lastRecord.map(CheckInRecord::getCheckInDate).orElse(null);
    }
    
    /**
     * 计算距离下次特殊奖励还差多少天
     */
    private int calculateNextBonusDays(Long userId) {
        int consecutiveDays = getCurrentConsecutiveDays(userId);
        
        // 如果已经签到 30 天，重置为下一个 30 天周期
        if (consecutiveDays >= 30) {
            consecutiveDays = consecutiveDays % 30;
        }
        
        // 下一个目标是 7 天或 30 天
        if (consecutiveDays < 7) {
            return 7 - consecutiveDays;
        } else if (consecutiveDays < 30) {
            return 30 - consecutiveDays;
        } else {
            return 30 - (consecutiveDays % 30);
        }
    }
    
    /**
     * 更新用户钱包
     */
    private void updateWallet(Long userId, int points, double balance) {
        Wallet wallet = walletRepository.findById(userId).orElse(null);
        
        if (wallet == null) {
            // 创建新钱包
            wallet = new Wallet();
            wallet.setUserId(userId);
            wallet.setPoints(points);
            wallet.setBalance(balance);
            wallet.setUpdatedAt(LocalDateTime.now());
        } else {
            // 更新现有钱包
            wallet.setPoints(wallet.getPoints() + points);
            wallet.setBalance(wallet.getBalance() + balance);
            wallet.setUpdatedAt(LocalDateTime.now());
        }
        
        walletRepository.save(wallet);
    }
}
