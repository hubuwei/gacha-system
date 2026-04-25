package com.cheng.mall.repository;

import com.cheng.mall.entity.FriendBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 好友黑名单Repository
 */
@Repository
public interface FriendBlacklistRepository extends JpaRepository<FriendBlacklist, Long> {
    
    /**
     * 查询用户的黑名单列表
     */
    List<FriendBlacklist> findByUidOrderByCreateTimeDesc(Long uid);
    
    /**
     * 检查是否已拉黑某用户
     */
    Optional<FriendBlacklist> findByUidAndBlockedUid(Long uid, Long blockedUid);
    
    /**
     * 统计用户的黑名单数量
     */
    long countByUid(Long uid);
}
