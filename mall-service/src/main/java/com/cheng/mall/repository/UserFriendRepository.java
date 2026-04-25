package com.cheng.mall.repository;

import com.cheng.mall.entity.UserFriend;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 好友关系Repository
 */
@Repository
public interface UserFriendRepository extends JpaRepository<UserFriend, Long> {

    /**
     * 查询用户的好友列表（已同意）
     */
    Page<UserFriend> findByUidAndStatus(Long uid, Integer status, Pageable pageable);

    /**
     * 检查好友关系是否存在
     */
    Optional<UserFriend> findByUidAndFriendUid(Long uid, Long friendUid);

    /**
     * 统计用户好友数量
     */
    long countByUidAndStatus(Long uid, Integer status);

    /**
     * 批量查询好友信息（用于Redis缓存）
     */
    List<UserFriend> findByUidAndStatus(Long uid, Integer status);
}
