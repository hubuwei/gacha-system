package com.cheng.mall.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户好友关系实体
 */
@Data
@Entity
@Table(name = "user_friend")
public class UserFriend {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "uid", nullable = false)
    private Long uid;
    
    @Column(name = "friend_uid", nullable = false)
    private Long friendUid;
    
    @Column(nullable = false)
    private Integer status = 1; // 0:已删除, 1:正常
    
    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;
    
    @Column(length = 100)
    private String remark;
    
    @Column(name = "group_name", length = 50)
    private String groupName = "My Friends";
}
