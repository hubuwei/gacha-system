package com.cheng.mall.repository;

import com.cheng.mall.entity.GameCategoryMapping;
import com.cheng.mall.entity.GameCategoryMapping.GameCategoryMappingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 游戏分类关联 Repository
 */
@Repository
public interface GameCategoryMappingRepository extends JpaRepository<GameCategoryMapping, GameCategoryMappingId> {
    
    /**
     * 根据游戏ID查询所有分类ID
     */
    @Query("SELECT gcm.id.categoryId FROM GameCategoryMapping gcm WHERE gcm.id.gameId = :gameId")
    List<Integer> findCategoryIdsByGameId(@Param("gameId") Long gameId);
    
    /**
     * 根据分类ID查询所有游戏ID
     */
    @Query("SELECT gcm.id.gameId FROM GameCategoryMapping gcm WHERE gcm.id.categoryId = :categoryId")
    List<Long> findGameIdsByCategoryId(@Param("categoryId") Integer categoryId);
}
