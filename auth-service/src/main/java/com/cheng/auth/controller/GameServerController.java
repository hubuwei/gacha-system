package com.cheng.auth.controller;

import com.cheng.auth.service.GameServerService;
import com.cheng.common.dto.CommonResponse;
import com.cheng.common.entity.GameServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 大区服务器控制器
 */
@RestController
@RequestMapping("/api/servers")
@CrossOrigin(origins = "*")
public class GameServerController {
    
    @Autowired
    private GameServerService gameServerService;
    
    /**
     * 获取所有可用大区
     */
    @GetMapping
    public CommonResponse<List<GameServer>> getServers() {
        try {
            List<GameServer> servers = gameServerService.getAllServers();
            return CommonResponse.success(servers);
        } catch (Exception e) {
            return CommonResponse.error(e.getMessage());
        }
    }
}
