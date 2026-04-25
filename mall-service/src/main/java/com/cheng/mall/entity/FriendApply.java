package com.cheng.mall.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 好友申请实体
 */
@Data
@Entity
@Table(name = "friend_apply")
@EntityListeners(AuditingEntityListener.class)
public class FriendApply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 申请人ID
     */
    @Column(name = "apply_uid", nullable = false)
    private Long applyUid;

    /**
     * 接收人ID
     */
    @Column(name = "receive_uid", nullable = false)
    private Long receiveUid;

    /**
     * 申请留言
     */
    @Column(length = 500)
    private String message;

    /**
     * 状态：0待处理 1已同意 2已拒绝
     */
    @Column(nullable = false)
    private Integer status = 0;

    /**
     * 申请时间
     */
    @Column(name = "create_time")
    @CreatedDate
    private LocalDateTime createTime;
}
