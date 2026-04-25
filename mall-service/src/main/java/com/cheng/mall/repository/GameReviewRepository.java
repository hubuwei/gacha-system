package com.cheng.mall.repository;

import com.cheng.mall.entity.GameReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 游戏评论 Repository
 */
@Repository
public interface GameReviewRepository extends JpaRepository<GameReview, Long> {
    
    /**
     * 根据游戏ID获取评论列表(分页)
     */
    Page<GameReview> findByGameIdAndStatusOrderByCreatedAtDesc(Long gameId, Integer status, Pageable pageable);
    
    /**
     * 根据游戏ID获取所有显示的评论
     */
    List<GameReview> findByGameIdAndStatusOrderByCreatedAtDesc(Long gameId, Integer status);
    
    /**
     * 统计游戏的评论数
     */
    long countByGameIdAndStatus(Long gameId, Integer status);
    
    /**
     * 计算游戏的平均评分
     */
    @Query("SELECT AVG(r.rating) FROM GameReview r WHERE r.gameId = :gameId AND r.status = 1")
    Double getAverageRatingByGameId(@Param("gameId") Long gameId);
    
    /**
     * 获取用户的评论
     */
    List<GameReview> findByUserIdAndGameId(Long userId, Long gameId);
    
    /**
     * 分页查询用户的评测列表
     */
    Page<GameReview> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
