package com.cheng.mall.repository;

import com.cheng.mall.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 愿望单 Repository
 */
@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    
    /**
     * 根据用户ID获取愿望单列表
     */
    List<Wishlist> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * 检查用户愿望单中是否已有该游戏
     */
    Optional<Wishlist> findByUserIdAndGameId(Long userId, Long gameId);
    
    /**
     * 删除用户的愿望单项
     */
    void deleteByUserIdAndGameId(Long userId, Long gameId);
    
    /**
     * 统计用户愿望单数量
     */
    long countByUserId(Long userId);
    
    /**
     * 获取需要折扣通知的愿望单
     */
    List<Wishlist> findByNotifyDiscountTrue();
    
    /**
     * 根据游戏ID获取开启折扣通知的愿望单
     */
    List<Wishlist> findByGameIdAndNotifyDiscountTrue(Long gameId);
}
