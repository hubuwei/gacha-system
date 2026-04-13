package com.cheng.mall.service;

import com.cheng.mall.entity.GameReview;
import com.cheng.mall.repository.GameRepository;
import com.cheng.mall.repository.GameReviewRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 游戏评论服务
 */
@Slf4j
@Service
public class GameReviewService {
    
    @Autowired
    private GameReviewRepository reviewRepository;
    
    @Autowired
    private GameRepository gameRepository;
    
    /**
     * 获取游戏评论列表(分页)
     */
    public Page<GameReview> getGameReviews(Long gameId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return reviewRepository.findByGameIdAndStatusOrderByCreatedAtDesc(gameId, 1, pageable);
    }
    
    /**
     * 创建评论
     */
    @Transactional
    public GameReview createReview(GameReview review) {
        // 验证评分范围
        if (review.getRating() < 1 || review.getRating() > 10) {
            throw new IllegalArgumentException("评分必须在1-10之间");
        }
        
        // 检查用户是否已经评论过
        List<GameReview> existingReviews = reviewRepository.findByUserIdAndGameId(
            review.getUserId(), review.getGameId()
        );
        
        if (!existingReviews.isEmpty()) {
            throw new IllegalArgumentException("您已经评价过这款游戏");
        }
        
        // 保存评论
        GameReview savedReview = reviewRepository.save(review);
        
        // 更新游戏的评分和评论数
        updateGameRating(review.getGameId());
        
        return savedReview;
    }
    
    /**
     * 点赞/点踩评论
     */
    @Transactional
    public void voteReview(Long reviewId, boolean isHelpful) {
        GameReview review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new RuntimeException("评论不存在"));
        
        if (isHelpful) {
            review.setHelpfulCount(review.getHelpfulCount() + 1);
        } else {
            review.setNotHelpfulCount(review.getNotHelpfulCount() + 1);
        }
        
        reviewRepository.save(review);
    }
    
    /**
     * 更新游戏评分
     */
    @Transactional
    public void updateGameRating(Long gameId) {
        Double avgRating = reviewRepository.getAverageRatingByGameId(gameId);
        long reviewCount = reviewRepository.countByGameIdAndStatus(gameId, 1);
        
        if (avgRating != null) {
            // 更新游戏的评分和评论数
            gameRepository.findById(gameId).ifPresent(game -> {
                game.setRating(java.math.BigDecimal.valueOf(avgRating));
                game.setTotalReviews((int) reviewCount);
                gameRepository.save(game);
            });
        }
    }
    
    /**
     * 获取评论统计信息
     */
    public Map<String, Object> getReviewStats(Long gameId) {
        Map<String, Object> stats = new HashMap<>();
        
        Double avgRating = reviewRepository.getAverageRatingByGameId(gameId);
        long reviewCount = reviewRepository.countByGameIdAndStatus(gameId, 1);
        
        stats.put("averageRating", avgRating != null ? String.format("%.2f", avgRating) : "0.00");
        stats.put("totalReviews", reviewCount);
        
        return stats;
    }
}
