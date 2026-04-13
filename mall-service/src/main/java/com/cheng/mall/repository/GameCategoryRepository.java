package com.cheng.mall.repository;

import com.cheng.mall.entity.GameCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 游戏分类 Repository
 */
@Repository
public interface GameCategoryRepository extends JpaRepository<GameCategory, Integer> {
    
    /**
     * 查询所有启用的分类
     */
    List<GameCategory> findByIsActiveTrueOrderBySortOrderAsc();
    
    /**
     * 查询子分类
     */
    List<GameCategory> findByParentIdOrderBySortOrderAsc(Integer parentId);
}
