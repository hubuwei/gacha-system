package com.cheng.auth.service;

import com.cheng.common.entity.GameServer;
import com.cheng.common.repository.GameServerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 大区服务器服务
 */
@Service
public class GameServerService {
    
    @Autowired
    private GameServerRepository gameServerRepository;
    
    /**
     * 获取所有可用大区
     */
    @Transactional(readOnly = true)
    public List<GameServer> getAllServers() {
        return gameServerRepository.findByStatus(1);
    }
}
