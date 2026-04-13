package com.cheng.common.repository;

import com.cheng.common.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 钱包数据访问接口
 */
@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    
    Wallet findByUserId(Long userId);
}
