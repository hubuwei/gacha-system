package com.cheng.mall.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 愿望单实体
 */
@Data
@Entity
@Table(name = "wishlists")
public class Wishlist {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "game_id", nullable = false)
    private Long gameId;
    
    @Column(name = "priority")
    private Integer priority = 1; // 1-普通 2-重要 3-非常想要
    
    @Column(name = "notify_discount")
    private Boolean notifyDiscount = true; // 是否通知折扣
    
    @Column(name = "notify_release")
    private Boolean notifyRelease = true; // 是否通知发售
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
