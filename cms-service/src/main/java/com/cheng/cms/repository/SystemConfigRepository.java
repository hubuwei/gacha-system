package com.cheng.cms.repository;

import com.cheng.cms.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Integer> {
    Optional<SystemConfig> findByConfigKey(String configKey);
}
