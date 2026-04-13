package com.cheng.common.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 抢购记录实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "seckill_records")
public class SeckillRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;  // 用户 ID
    
    @Column(nullable = false)
    private Long productId;  // 商品 ID
    
    @Column(nullable = false)
    private Integer points;  // 使用的积分
    
    @Column(nullable = false)
    private String status;  // 状态：SUCCESS-成功，FAILED-失败
    
    @Column(nullable = false)
    private LocalDateTime seckillTime;  // 抢购时间
    
    @Column(length = 500)
    private String remark;  // 备注
    
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
