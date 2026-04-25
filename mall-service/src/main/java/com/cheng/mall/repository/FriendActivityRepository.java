package com.cheng.mall.repository;

import com.cheng.mall.entity.FriendActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Friend Activity Repository
 */
@Repository
public interface FriendActivityRepository extends JpaRepository<FriendActivity, Long> {
    
    /**
     * Get friend activities (paginated, ordered by time)
     */
    Page<FriendActivity> findByUidOrderByCreatedAtDesc(Long uid, Pageable pageable);
}
