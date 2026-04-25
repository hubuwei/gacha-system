package com.cheng.mall.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 好友申请实体
 */
@Data
@Entity
@Table(name = "friend_apply")
public class FriendApply {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "from_uid", nullable = false)
    private Long fromUid; // 申请人
    
    @Column(name = "to_uid", nullable = false)
    private Long toUid; // 被申请人
    
    @Column(length = 200)
    private String message; // 申请消息
    
    @Column(nullable = false)
    private Integer status = 0; // 0:待处理, 1:已同意, 2:已拒绝
    
    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;
    
    @Column(name = "handle_time")
    private LocalDateTime handleTime; // 处理时间
}
