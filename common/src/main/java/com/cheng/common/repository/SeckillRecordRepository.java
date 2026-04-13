package com.cheng.common.repository;

import com.cheng.common.entity.SeckillRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 抢购记录 Repository
 */
public interface SeckillRecordRepository extends JpaRepository<SeckillRecord, Long> {
    
    @Query("SELECT COUNT(sr) FROM SeckillRecord sr WHERE sr.userId = :userId AND sr.productId = :productId AND sr.seckillTime >= :startTime")
    Integer countUserSeckillInRound(@Param("userId") Long userId, @Param("productId") Long productId, @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT sr FROM SeckillRecord sr WHERE sr.userId = :userId AND sr.productId = :productId ORDER BY sr.seckillTime DESC")
    List<SeckillRecord> findByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);
    
    Optional<SeckillRecord> findFirstByUserIdAndProductIdOrderBySeckillTimeDesc(@Param("userId") Long userId, @Param("productId") Long productId);
}
