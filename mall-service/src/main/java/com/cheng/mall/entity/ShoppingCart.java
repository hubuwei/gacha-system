package com.cheng.mall.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 购物车实体
 */
@Data
@Entity
@Table(name = "shopping_cart")
public class ShoppingCart {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "game_id", nullable = false)
    private Long gameId;
    
    @Column(name = "quantity")
    private Integer quantity = 1;
    
    @Column(name = "checked")
    private Boolean checked = true;
    
    @CreationTimestamp
    @Column(name = "added_at", updatable = false)
    private LocalDateTime addedAt;
}
