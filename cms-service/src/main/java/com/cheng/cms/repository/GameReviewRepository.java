package com.cheng.cms.repository;

import com.cheng.cms.entity.GameReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 游戏评论Repository
 */
@Repository
public interface GameReviewRepository extends JpaRepository<GameReview, Long> {
    
    /**
     * 分页查询评论（可按状态筛选）
     */
    Page<GameReview> findAllByStatus(Integer status, Pageable pageable);
    
    /**
     * 按游戏ID查询评论
     */
    Page<GameReview> findByGameIdAndStatus(Long gameId, Integer status, Pageable pageable);
    
    /**
     * 统计各状态评论数量
     */
    long countByStatus(Integer status);
}
