package com.cheng.game.service;

import com.cheng.common.entity.RechargeRecord;
import com.cheng.common.entity.User;
import com.cheng.common.entity.Wallet;
import com.cheng.common.repository.RechargeRecordRepository;
import com.cheng.common.repository.UserRepository;
import com.cheng.common.repository.WalletRepository;
import com.cheng.common.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 充值服务
 */
@Service
public class RechargeService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private WalletRepository walletRepository;
    
    @Autowired
    private RechargeRecordRepository rechargeRecordRepository;
    
    /**
     * 充值
     */
    @Transactional
    public RechargeRecord recharge(Long userId, Double amount) {
        System.out.println("=== RechargeService.recharge ===");
        System.out.println("用户 ID: " + userId);
        System.out.println("充值金额：" + amount);
        
        // 查找用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 获取或创建钱包
        Wallet wallet = walletRepository.findById(userId).orElse(null);
        if (wallet == null) {
            System.out.println("钱包不存在，创建新钱包");
            wallet = new Wallet();
            wallet.setUserId(userId);
            wallet.setPoints(0);
            wallet.setBalance(0.0);
            walletRepository.save(wallet);
            System.out.println("新钱包已保存");
        } else {
            System.out.println("钱包已存在，当前余额：" + wallet.getBalance() + ", 当前积分：" + wallet.getPoints());
        }
        
        // 计算获得的积分（1 元 = 10 积分）
        int points = (int) (amount * Constants.RECHARGE_RATE);
        
        // 更新钱包余额和积分
        wallet.setBalance(wallet.getBalance() + amount);
        wallet.setPoints(wallet.getPoints() + points);
        wallet = walletRepository.save(wallet);
        
        System.out.println("更新后余额：" + wallet.getBalance() + ", 更新后积分：" + wallet.getPoints());
        
        // 创建充值记录
        RechargeRecord record = new RechargeRecord();
        record.setUserId(userId);
        record.setAmount(amount);
        record.setPoints(points);
        record.setRemark("充值");
        
        System.out.println("=== 充值完成 ===");
        
        return rechargeRecordRepository.save(record);
    }
    
    /**
     * 获取充值记录
     */
    public List<RechargeRecord> getRechargeRecords(Long userId) {
        return rechargeRecordRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }
}
