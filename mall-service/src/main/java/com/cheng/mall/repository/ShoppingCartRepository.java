package com.cheng.mall.repository;

import com.cheng.mall.entity.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 购物车 Repository
 */
@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {
    
    /**
     * 根据用户ID获取购物车列表
     */
    List<ShoppingCart> findByUserIdOrderByAddedAtDesc(Long userId);
    
    /**
     * 检查用户购物车中是否已有该游戏
     */
    Optional<ShoppingCart> findByUserIdAndGameId(Long userId, Long gameId);
    
    /**
     * 删除用户的购物车项
     */
    void deleteByUserIdAndGameId(Long userId, Long gameId);
    
    /**
     * 统计用户购物车数量
     */
    long countByUserId(Long userId);
}
