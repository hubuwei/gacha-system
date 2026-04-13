package com.cheng.mall.repository;

import com.cheng.mall.entity.Game;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 游戏 Repository
 */
@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    
    /**
     * 分页查询所有在售游戏
     */
    Page<Game> findByIsOnSaleTrue(Pageable pageable);
    
    /**
     * 查询精选游戏
     */
    List<Game> findByIsFeaturedTrueAndIsOnSaleTrue();
    
    /**
     * 根据分类查询游戏
     */
    @Query("SELECT g FROM Game g JOIN GameCategoryMapping gcm ON g.id = gcm.id.gameId WHERE gcm.id.categoryId = :categoryId AND g.isOnSale = true")
    List<Game> findByCategoryId(@Param("categoryId") Integer categoryId);
    
    /**
     * 搜索游戏(标题模糊查询)
     */
    @Query("SELECT g FROM Game g WHERE g.title LIKE %:keyword% AND g.isOnSale = true")
    List<Game> searchByTitle(@Param("keyword") String keyword);
    
    /**
     * 按价格排序
     */
    Page<Game> findByIsOnSaleTrueOrderByCurrentPriceAsc(Pageable pageable);
    
    Page<Game> findByIsOnSaleTrueOrderByCurrentPriceDesc(Pageable pageable);
    
    /**
     * 按评分排序
     */
    Page<Game> findByIsOnSaleTrueOrderByRatingDesc(Pageable pageable);
    
    /**
     * 按销量排序
     */
    Page<Game> findByIsOnSaleTrueOrderByTotalSalesDesc(Pageable pageable);
}
