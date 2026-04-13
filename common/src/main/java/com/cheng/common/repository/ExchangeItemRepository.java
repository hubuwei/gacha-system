package com.cheng.common.repository;

import com.cheng.common.entity.ExchangeItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 兑换物品 Repository
 */
@Repository
public interface ExchangeItemRepository extends JpaRepository<ExchangeItem, Long> {
    
    /**
     * 查询所有上架的物品
     */
    List<ExchangeItem> findByEnabledTrueOrderBySortWeightDescIdAsc();
    
    /**
     * 根据名称模糊查询
     */
    List<ExchangeItem> findByNameContainingAndEnabledTrue(String keyword);
}
