package com.cheng.cms.service;

import com.cheng.cms.entity.GameReview;
import com.cheng.cms.repository.GameReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 评论审核Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {
    
    private final GameReviewRepository reviewRepository;
    
    /**
     * 分页查询评论列表
     */
    public Map<String, Object> getReviews(Integer status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<GameReview> reviews;
        
        if (status != null) {
            reviews = reviewRepository.findAllByStatus(status, pageable);
        } else {
            reviews = reviewRepository.findAll(pageable);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", reviews.getContent());
        result.put("total", reviews.getTotalElements());
        result.put("page", page);
        result.put("size", size);
        
        return result;
    }
    
    /**
     * 审核通过评论
     */
    @Transactional
    public void approveReview(Long id) {
        GameReview review = reviewRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("评论不存在"));
        
        review.setStatus(1); // 已通过
        reviewRepository.save(review);
        
        log.info("评论 {} 审核通过", id);
    }
    
    /**
     * 拒绝评论
     */
    @Transactional
    public void rejectReview(Long id, String reason) {
        GameReview review = reviewRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("评论不存在"));
        
        review.setStatus(2); // 已拒绝
        reviewRepository.save(review);
        
        log.info("评论 {} 审核拒绝，原因: {}", id, reason);
    }
    
    /**
     * 删除评论
     */
    @Transactional
    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);
        log.info("评论 {} 已删除", id);
    }
    
    /**
     * 获取评论详情
     */
    public GameReview getReviewDetail(Long id) {
        return reviewRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("评论不存在"));
    }
    
    /**
     * 统计各状态评论数量
     */
    public Map<String, Long> getReviewStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("pending", reviewRepository.countByStatus(0));
        stats.put("approved", reviewRepository.countByStatus(1));
        stats.put("rejected", reviewRepository.countByStatus(2));
        return stats;
    }
}
