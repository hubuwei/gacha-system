package com.cheng.mall.repository;

import com.cheng.mall.entity.UserNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户通知Repository
 */
@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
    
    /**
     * 查询用户的未读通知数量
     */
    long countByUidAndIsRead(Long uid, Integer isRead);
    
    /**
     * 分页查询用户的通知
     */
    Page<UserNotification> findByUidOrderByCreatedAtDesc(Long uid, Pageable pageable);
    
    /**
     * 查询用户的未读通知列表
     */
    List<UserNotification> findByUidAndIsReadOrderByCreatedAtDesc(Long uid, Integer isRead);
    
    /**
     * 标记通知为已读
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserNotification n SET n.isRead = 1 WHERE n.id = ?1")
    void markAsRead(Long id);
}
