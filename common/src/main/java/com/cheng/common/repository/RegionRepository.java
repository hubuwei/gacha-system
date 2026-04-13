package com.cheng.common.repository;

import com.cheng.common.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 大区数据访问接口
 */
@Repository
public interface RegionRepository extends JpaRepository<Region, Integer> {
    
    List<Region> findAll();
}
