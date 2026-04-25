package com.cheng.mall.repository;

import com.cheng.mall.entity.UserOnlineStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 在线状态Repository
 */
@Repository
public interface UserOnlineStatusRepository extends JpaRepository<UserOnlineStatus, Long> {

    /**
     * 根据用户ID查询在线状态
     */
    Optional<UserOnlineStatus> findByUid(Long uid);

    /**
     * 删除用户在线状态
     */
    void deleteByUid(Long uid);
}
