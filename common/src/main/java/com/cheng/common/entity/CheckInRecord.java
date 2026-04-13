package com.cheng.common.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日签到记录实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "daily_check_in_records")
public class CheckInRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private LocalDate checkInDate;
    
    @Column(nullable = false)
    private Integer rewardPoints = 0;  // 奖励积分
    
    @Column(nullable = false, precision = 10, scale = 2)
    private Double rewardBalance = 0.0;  // 奖励虚拟货币
    
    @Column(nullable = false)
    private Integer consecutiveDays = 1;  // 连续签到天数
    
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
