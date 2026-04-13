package com.cheng.mall.es.controller;

import com.cheng.common.dto.CommonResponse;
import com.cheng.mall.es.service.GameSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 数据同步控制器
 */
@Slf4j
@RestController
@RequestMapping("/sync")
@CrossOrigin(origins = "*")
public class DataSyncController {
    
    @Autowired
    private GameSyncService syncService;
    
    /**
     * 全量同步游戏数据到 Elasticsearch
     * 
     * POST /api/sync/games
     */
    @PostMapping("/games")
    public CommonResponse<String> syncAllGames() {
        try {
            syncService.syncAllGames();
            return CommonResponse.success("游戏数据同步成功");
        } catch (Exception e) {
            log.error("同步游戏数据失败", e);
            return CommonResponse.error("同步失败：" + e.getMessage());
        }
    }
    
    /**
     * 增量同步单个游戏
     * 
     * POST /api/sync/game/{gameId}
     */
    @PostMapping("/game/{gameId}")
    public CommonResponse<String> syncGame(@PathVariable Long gameId) {
        try {
            syncService.syncGame(gameId);
            return CommonResponse.success("游戏数据同步请求已提交（异步执行）");
        } catch (Exception e) {
            log.error("同步游戏数据失败", e);
            return CommonResponse.error("同步失败：" + e.getMessage());
        }
    }
}
