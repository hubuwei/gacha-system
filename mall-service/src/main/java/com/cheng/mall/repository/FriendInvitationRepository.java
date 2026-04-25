package com.cheng.mall.repository;

import com.cheng.mall.entity.FriendInvitation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Friend Invitation Repository
 */
@Repository
public interface FriendInvitationRepository extends JpaRepository<FriendInvitation, Long> {
    
    /**
     * Get pending invitations for invitee
     */
    List<FriendInvitation> findByInviteeUidAndStatusOrderByCreatedAtDesc(Long inviteeUid, Integer status);
    
    /**
     * Get invitation history (paginated)
     */
    Page<FriendInvitation> findByInviteeUidOrderByCreatedAtDesc(Long inviteeUid, Pageable pageable);
    
    /**
     * Check if there's a pending invitation
     */
    Optional<FriendInvitation> findByInviterUidAndInviteeUidAndGameIdAndStatus(
        Long inviterUid, Long inviteeUid, Long gameId, Integer status);
}
