package com.cheng.common.repository;

import com.cheng.common.entity.RechargeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 充值记录数据访问接口
 */
@Repository
public interface RechargeRecordRepository extends JpaRepository<RechargeRecord, Long> {
    
    List<RechargeRecord> findByUserIdOrderByCreateTimeDesc(Long userId);
}
