package com.cheng.common.repository;

import com.cheng.common.entity.CheckInRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 签到记录数据访问接口
 */
@Repository
public interface CheckInRecordRepository extends JpaRepository<CheckInRecord, Long> {
    
    /**
     * 查询用户指定日期的签到记录
     */
    Optional<CheckInRecord> findByUserIdAndCheckInDate(Long userId, LocalDate checkInDate);
    
    /**
     * 查询用户最近的一次签到记录
     */
    Optional<CheckInRecord> findTopByUserIdOrderByCheckInDateDesc(Long userId);
    
    /**
     * 查询用户最近 N 天的签到记录（用于统计连续签到）
     */
    List<CheckInRecord> findByUserIdOrderByCheckInDateDesc(Long userId);
}
