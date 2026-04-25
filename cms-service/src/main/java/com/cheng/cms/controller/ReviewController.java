package com.cheng.cms.controller;

import com.cheng.common.dto.CommonResponse;
import com.cheng.cms.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 评论审核Controller
 */
@RestController
@RequestMapping("/api/cms/reviews")
@RequiredArgsConstructor
public class ReviewController {
    
    private final ReviewService reviewService;
    
    /**
     * 获取评论列表（支持状态筛选）
     */
    @GetMapping
    public CommonResponse<Map<String, Object>> getReviews(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Map<String, Object> result = reviewService.getReviews(status, page, size);
            return CommonResponse.success(result);
        } catch (Exception e) {
            return CommonResponse.error("获取评论列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取评论详情
     */
    @GetMapping("/{id}")
    public CommonResponse<Object> getReviewDetail(@PathVariable Long id) {
        try {
            Object review = reviewService.getReviewDetail(id);
            return CommonResponse.success(review);
        } catch (Exception e) {
            return CommonResponse.error("获取评论详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 审核通过评论
     */
    @PutMapping("/{id}/approve")
    public CommonResponse<Void> approveReview(@PathVariable Long id) {
        try {
            reviewService.approveReview(id);
            return CommonResponse.success(null);
        } catch (Exception e) {
            return CommonResponse.error("审核失败: " + e.getMessage());
        }
    }
    
    /**
     * 拒绝评论
     */
    @PutMapping("/{id}/reject")
    public CommonResponse<Void> rejectReview(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            String reason = body.get("reason");
            reviewService.rejectReview(id, reason);
            return CommonResponse.success(null);
        } catch (Exception e) {
            return CommonResponse.error("拒绝失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除评论
     */
    @DeleteMapping("/{id}")
    public CommonResponse<Void> deleteReview(@PathVariable Long id) {
        try {
            reviewService.deleteReview(id);
            return CommonResponse.success(null);
        } catch (Exception e) {
            return CommonResponse.error("删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取评论统计
     */
    @GetMapping("/stats")
    public CommonResponse<Map<String, Long>> getReviewStats() {
        try {
            Map<String, Long> stats = reviewService.getReviewStats();
            return CommonResponse.success(stats);
        } catch (Exception e) {
            return CommonResponse.error("获取统计失败: " + e.getMessage());
        }
    }
}
