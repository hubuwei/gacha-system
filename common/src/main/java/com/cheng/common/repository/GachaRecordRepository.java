package com.cheng.common.repository;

import com.cheng.common.entity.GachaRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 抽奖记录数据访问接口
 */
@Repository
public interface GachaRecordRepository extends JpaRepository<GachaRecord, Long> {
    
    List<GachaRecord> findByUserIdOrderByCreatedAtDesc(Long userId);
}
