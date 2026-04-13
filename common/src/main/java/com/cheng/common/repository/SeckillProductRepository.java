package com.cheng.common.repository;

import com.cheng.common.entity.SeckillProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

/**
 * 抢购商品 Repository
 */
public interface SeckillProductRepository extends JpaRepository<SeckillProduct, Long> {
    
    @Query("SELECT sp FROM SeckillProduct sp WHERE sp.isActive = true AND sp.remainingStock > 0")
    List<SeckillProduct> findActiveProducts();
    
    @Query("SELECT sp FROM SeckillProduct sp WHERE sp.id = :id AND sp.isActive = true")
    SeckillProduct findActiveById(@Param("id") Long id);
}
