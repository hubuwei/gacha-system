package com.cheng.common.repository;

import com.cheng.common.entity.DeliveryAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 收货地址 Repository
 */
@Repository
public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddress, Long> {
    
    /**
     * 根据兑换记录 ID 查询收货地址
     */
    Optional<DeliveryAddress> findByExchangeRecordId(Long exchangeRecordId);
    
    /**
     * 根据用户 ID 查询最新的收货地址
     */
    Optional<DeliveryAddress> findTopByUserIdOrderByCreatedAtDesc(Long userId);
}
