package com.cheng.mall.repository;

import com.cheng.mall.entity.FriendApply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 好友申请Repository
 */
@Repository
public interface FriendApplyRepository extends JpaRepository<FriendApply, Long> {

    /**
     * 查询接收人的申请列表
     */
    Page<FriendApply> findByReceiveUidAndStatus(Long receiveUid, Integer status, Pageable pageable);

    /**
     * 查询申请人的申请记录
     */
    Page<FriendApply> findByApplyUid(Long applyUid, Pageable pageable);

    /**
     * 检查是否已有申请
     */
    Optional<FriendApply> findByApplyUidAndReceiveUidAndStatus(
        Long applyUid, Long receiveUid, Integer status);

    /**
     * 统计待处理申请数量
     */
    long countByReceiveUidAndStatus(Long receiveUid, Integer status);
}
