package com.cheng.mall.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 游戏评论实体
 */
@Data
@Entity
@Table(name = "game_reviews")
public class GameReview {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "username", nullable = false, length = 50)
    private String username;
    
    @Column(name = "user_avatar", length = 500)
    private String userAvatar;
    
    @Column(name = "game_id", nullable = false)
    private Long gameId;
    
    @Column(name = "rating", nullable = false)
    private Integer rating; // 1-10
    
    @Column(name = "title", length = 200)
    private String title;
    
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "pros", columnDefinition = "TEXT")
    private String pros;
    
    @Column(name = "cons", columnDefinition = "TEXT")
    private String cons;
    
    @Column(name = "play_hours", precision = 10, scale = 2)
    private BigDecimal playHours;
    
    @Column(name = "is_verified_purchase")
    private Boolean isVerifiedPurchase = false;
    
    @Column(name = "helpful_count")
    private Integer helpfulCount = 0;
    
    @Column(name = "not_helpful_count")
    private Integer notHelpfulCount = 0;
    
    @Column(name = "parent_id")
    private Long parentId;
    
    @Column(name = "is_official")
    private Boolean isOfficial = false;
    
    @Column(name = "status")
    private Integer status = 1; // 0-待审核 1-显示 2-隐藏
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
