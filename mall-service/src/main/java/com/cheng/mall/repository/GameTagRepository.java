package com.cheng.mall.repository;

import com.cheng.mall.entity.GameTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 游戏标签 Repository
 */
@Repository
public interface GameTagRepository extends JpaRepository<GameTag, Integer> {
    
    /**
     * 查询所有标签
     */
    List<GameTag> findAllByOrderBySortOrderAsc();
}
