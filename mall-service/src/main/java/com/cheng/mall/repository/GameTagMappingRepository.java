package com.cheng.mall.repository;

import com.cheng.mall.entity.GameTagMapping;
import com.cheng.mall.entity.GameTagMapping.GameTagMappingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 游戏标签关联 Repository
 */
@Repository
public interface GameTagMappingRepository extends JpaRepository<GameTagMapping, GameTagMappingId> {
    
    /**
     * 根据游戏ID查询所有标签ID
     */
    @Query("SELECT gtm.id.tagId FROM GameTagMapping gtm WHERE gtm.id.gameId = :gameId")
    List<Integer> findTagIdsByGameId(@Param("gameId") Long gameId);
    
    /**
     * 根据标签ID查询所有游戏ID
     */
    @Query("SELECT gtm.id.gameId FROM GameTagMapping gtm WHERE gtm.id.tagId = :tagId")
    List<Long> findGameIdsByTagId(@Param("tagId") Integer tagId);
}
