package com.cheng.game.service;

import com.cheng.common.entity.Wallet;
import com.cheng.common.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 积分服务
 */
@Service
public class PointsService {
    
    @Autowired
    private WalletRepository walletRepository;
    
    /**
     * 查询用户积分（返回钱包信息）
     */
    @Transactional(readOnly = true)
    public Wallet getUserPoints(Long userId) {
        return walletRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在或没有钱包"));
    }
}
