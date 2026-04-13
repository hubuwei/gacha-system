package com.cheng.mall.repository;

import com.cheng.mall.entity.UserWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户钱包 Repository
 */
@Repository
public interface UserWalletRepository extends JpaRepository<UserWallet, Long> {
    
    /**
     * 根据用户ID查询钱包
     */
    Optional<UserWallet> findByUserId(Long userId);
    
    /**
     * 删除用户钱包
     */
    void deleteByUserId(Long userId);
}
