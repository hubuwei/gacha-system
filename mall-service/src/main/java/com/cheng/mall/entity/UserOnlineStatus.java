package com.cheng.mall.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户在线状态实体
 */
@Data
@Entity
@Table(name = "user_online_status")
@EntityListeners(AuditingEntityListener.class)
public class UserOnlineStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long uid;

    /**
     * 在线状态：0离线 1在线 2离开 3游戏中
     */
    @Column(nullable = false)
    private Integer status = 0;

    /**
     * 当前游玩游戏ID（status=3时有效）
     */
    @Column(name = "game_id")
    private Long gameId;

    /**
     * 最后心跳时间
     */
    @Column(name = "update_time")
    @CreatedDate
    private LocalDateTime updateTime;
}
