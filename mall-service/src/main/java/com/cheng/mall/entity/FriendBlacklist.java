package com.cheng.mall.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 好友黑名单实体类
 */
@Data
@Entity
@Table(name = "friend_blacklist")
public class FriendBlacklist {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 用户ID
     */
    @Column(nullable = false)
    private Long uid;
    
    /**
     * 被拉黑的用户ID
     */
    @Column(name = "blocked_uid", nullable = false)
    private Long blockedUid;
    
    /**
     * 拉黑时间
     */
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createTime;
}
