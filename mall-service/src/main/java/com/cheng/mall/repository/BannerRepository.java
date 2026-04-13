package com.cheng.mall.repository;

import com.cheng.mall.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 轮播图 Repository
 */
@Repository
public interface BannerRepository extends JpaRepository<Banner, Integer> {
    
    /**
     * 查询所有启用的轮播图，按排序字段升序
     */
    List<Banner> findByIsActiveTrueOrderBySortOrderAsc();
    
    /**
     * 查询在有效期内的启用轮播图
     */
    @Query("SELECT b FROM Banner b WHERE b.isActive = true AND (b.startTime IS NULL OR b.startTime <= :now) AND (b.endTime IS NULL OR b.endTime >= :now) ORDER BY b.sortOrder ASC")
    List<Banner> findActiveBanners(LocalDateTime now);
}
