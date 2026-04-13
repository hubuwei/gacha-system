package com.cheng.common.repository;

import com.cheng.common.entity.ExchangeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 兑换记录 Repository
 */
@Repository
public interface ExchangeRecordRepository extends JpaRepository<ExchangeRecord, Long> {
    
    /**
     * 根据用户 ID 查询兑换记录
     */
    List<ExchangeRecord> findByUserIdOrderByExchangeDateDescCreatedAtDesc(Long userId);
    
    /**
     * 根据用户 ID 和状态查询兑换记录
     */
    List<ExchangeRecord> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, Integer status);
    
    /**
     * 查询指定日期的兑换记录
     */
    List<ExchangeRecord> findByExchangeDate(LocalDate exchangeDate);
    
    /**
     * 统计用户当日兑换次数
     */
    long countByUserIdAndExchangeDate(Long userId, LocalDate exchangeDate);
}
