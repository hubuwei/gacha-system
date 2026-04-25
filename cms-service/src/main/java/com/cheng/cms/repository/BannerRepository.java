package com.cheng.cms.repository;

import com.cheng.cms.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Banner Repository
 */
@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {
    
    /**
     * 查询所有启用的Banner（按排序）
     */
    List<Banner> findByIsActiveTrueOrderBySortOrderAsc();
    
    /**
     * 查询所有Banner（按排序）
     */
    List<Banner> findAllByOrderBySortOrderAsc();
}
