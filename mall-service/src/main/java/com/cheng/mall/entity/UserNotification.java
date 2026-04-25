package com.cheng.mall.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户通知实体类
 */
@Data
@Entity
@Table(name = "user_notification")
public class UserNotification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 接收人ID
     */
    @Column(nullable = false)
    private Long uid;
    
    /**
     * 通知类型
     * FRIEND_REQUEST - 好友申请
     * FRIEND_ACCEPTED - 好友申请被同意
     * FRIEND_ONLINE - 好友上线
     * FRIEND_OFFLINE - 好友离线
     * FRIEND_IN_GAME - 好友游戏中
     * GAME_PURCHASED - 购买游戏
     * REVIEW_PUBLISHED - 发布评测
     */
    @Column(nullable = false, length = 50)
    private String type;
    
    /**
     * 标题
     */
    @Column(length = 200)
    private String title;
    
    /**
     * 内容
     */
    @Column(columnDefinition = "TEXT")
    private String content;
    
    /**
     * 是否已读：0未读，1已读
     */
    @Column(nullable = false)
    private Integer isRead = 0;
    
    /**
     * 关联ID（如申请ID、游戏ID等）
     */
    private Long relatedId;
    
    /**
     * 发送人ID（可选）
     */
    private Long fromUid;
    
    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
