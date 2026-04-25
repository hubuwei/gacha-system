package com.cheng.mall.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 好友关系实体
 */
@Data
@Entity
@Table(name = "user_friend")
@EntityListeners(AuditingEntityListener.class)
public class UserFriend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 当前用户ID
     */
    @Column(nullable = false)
    private Long uid;

    /**
     * 好友用户ID
     */
    @Column(name = "friend_uid", nullable = false)
    private Long friendUid;

    /**
     * 状态：0待同意 1已同意 2拒绝 3拉黑
     */
    @Column(nullable = false)
    private Integer status = 1;

    /**
     * 申请时间
     */
    @Column(name = "create_time")
    @CreatedDate
    private LocalDateTime createTime;

    /**
     * 好友备注
     */
    @Column(length = 100)
    private String remark;
}
