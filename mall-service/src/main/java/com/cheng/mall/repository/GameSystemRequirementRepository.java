package com.cheng.mall.repository;

import com.cheng.mall.entity.GameSystemRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 游戏系统配置要求 Repository
 */
@Repository
public interface GameSystemRequirementRepository extends JpaRepository<GameSystemRequirement, Long> {
    
    /**
     * 根据游戏ID查询配置要求
     */
    Optional<GameSystemRequirement> findByGameId(Long gameId);
}
