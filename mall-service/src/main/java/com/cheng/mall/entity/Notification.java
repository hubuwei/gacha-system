package com.cheng.mall.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户通知实体
 */
@Data
@Entity
@Table(name = "notifications")
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    /**
     * 通知类型：promotion-促销, system-系统, order-订单
     */
    @Column(nullable = false, length = 20)
    private String type;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String message;
    
    /**
     * 是否已读
     */
    @Column(nullable = false)
    private Boolean isRead = false;
    
    /**
     * 关联的游戏ID
     */
    private Long relatedGameId;
    
    /**
     * 关联的订单ID
     */
    private Long relatedOrderId;
    
    /**
     * 关联类型：game, order
     */
    @Column(length = 20)
    private String relatedType;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
