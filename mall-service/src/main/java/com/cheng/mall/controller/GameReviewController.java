package com.cheng.mall.controller;

import com.cheng.common.dto.CommonResponse;
import com.cheng.mall.entity.GameReview;
import com.cheng.mall.service.GameReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 游戏评论控制器
 */
@Slf4j
@RestController
@RequestMapping("/games")
@CrossOrigin(origins = "*")
public class GameReviewController {
    
    @Autowired
    private GameReviewService reviewService;
    
    /**
     * 获取游戏评论列表
     */
    @GetMapping("/{gameId}/reviews")
    public CommonResponse<Map<String, Object>> getGameReviews(
            @PathVariable Long gameId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<GameReview> reviews = reviewService.getGameReviews(gameId, page, size);
            Map<String, Object> stats = reviewService.getReviewStats(gameId);
            
            Map<String, Object> result = Map.of(
                "reviews", reviews.getContent(),
                "totalElements", reviews.getTotalElements(),
                "totalPages", reviews.getTotalPages(),
                "currentPage", reviews.getNumber(),
                "stats", stats
            );
            
            return CommonResponse.success(result);
        } catch (Exception e) {
            log.error("获取评论列表失败", e);
            return CommonResponse.error("获取评论列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 创建评论
     */
    @PostMapping("/{gameId}/reviews")
    public CommonResponse<GameReview> createReview(
            @PathVariable Long gameId,
            @RequestBody GameReview review) {
        try {
            review.setGameId(gameId);
            GameReview savedReview = reviewService.createReview(review);
            return CommonResponse.success(savedReview);
        } catch (Exception e) {
            log.error("创建评论失败", e);
            return CommonResponse.error("创建评论失败：" + e.getMessage());
        }
    }
    
    /**
     * 点赞/点踩评论
     */
    @PostMapping("/reviews/{reviewId}/vote")
    public CommonResponse<Void> voteReview(
            @PathVariable Long reviewId,
            @RequestParam boolean isHelpful) {
        try {
            reviewService.voteReview(reviewId, isHelpful);
            return CommonResponse.success(null);
        } catch (Exception e) {
            log.error("投票失败", e);
            return CommonResponse.error("投票失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取评论统计
     */
    @GetMapping("/{gameId}/reviews/stats")
    public CommonResponse<Map<String, Object>> getReviewStats(@PathVariable Long gameId) {
        try {
            Map<String, Object> stats = reviewService.getReviewStats(gameId);
            return CommonResponse.success(stats);
        } catch (Exception e) {
            log.error("获取评论统计失败", e);
            return CommonResponse.error("获取评论统计失败：" + e.getMessage());
        }
    }
}
