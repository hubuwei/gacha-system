package com.cheng.common.repository;

import com.cheng.common.entity.GameServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 大区服务器数据访问接口
 */
@Repository
public interface GameServerRepository extends JpaRepository<GameServer, Long> {
    
    List<GameServer> findByStatus(Integer status);
}
