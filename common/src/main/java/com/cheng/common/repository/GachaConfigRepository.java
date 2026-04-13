package com.cheng.common.repository;

import com.cheng.common.entity.GachaConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 抽奖配置数据访问接口
 */
@Repository
public interface GachaConfigRepository extends JpaRepository<GachaConfig, Integer> {
    
    List<GachaConfig> findByRarityOrderByBaseProbDesc(String rarity);
}
