package com.cheng.mall.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Friend Activity Entity
 */
@Data
@Entity
@Table(name = "friend_activity")
public class FriendActivity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * User ID who performed the action
     */
    @Column(nullable = false)
    private Long uid;
    
    /**
     * Activity type
     */
    @Column(nullable = false, length = 50)
    private String type;
    
    /**
     * Activity content description
     */
    @Column(columnDefinition = "TEXT")
    private String content;
    
    /**
     * Related game ID
     */
    @Column(name = "game_id")
    private Long gameId;
    
    /**
     * Additional metadata (JSON)
     */
    @Column(columnDefinition = "JSON")
    private String metadata;
    
    /**
     * Activity time
     */
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
