package com.cheng.mall.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Friend Game Invitation Entity
 */
@Data
@Entity
@Table(name = "friend_invitation")
public class FriendInvitation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Inviter user ID
     */
    @Column(name = "inviter_uid", nullable = false)
    private Long inviterUid;
    
    /**
     * Invitee user ID
     */
    @Column(name = "invitee_uid", nullable = false)
    private Long inviteeUid;
    
    /**
     * Game ID to play together
     */
    @Column(name = "game_id", nullable = false)
    private Long gameId;
    
    /**
     * Status: 0-pending, 1-accepted, 2-rejected, 3-expired
     */
    @Column(nullable = false)
    private Integer status = 0;
    
    /**
     * Invitation message
     */
    @Column(length = 500)
    private String message;
    
    /**
     * Virtual room code for simulation
     */
    @Column(name = "room_code", length = 50)
    private String roomCode;
    
    /**
     * Invitation time
     */
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Response time
     */
    @Column(name = "responded_at")
    private LocalDateTime respondedAt;
    
    /**
     * Expiration time
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
}
