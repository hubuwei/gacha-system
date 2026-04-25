package com.cheng.cms.controller;

import com.cheng.common.dto.CommonResponse;
import com.cheng.cms.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Game Controller - 游戏管理接口
 */
@RestController
@RequestMapping("/api/cms/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    /**
     * 获取游戏列表
     */
    @GetMapping
    public CommonResponse<List<Map<String, Object>>> getGames(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Map<String, Object> result = gameService.getGames(keyword, page, size);
            return CommonResponse.success((List<Map<String, Object>>) result.get("list"));
        } catch (Exception e) {
            return CommonResponse.error("获取游戏列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取游戏详情
     */
    @GetMapping("/{id}")
    public CommonResponse<Map<String, Object>> getGameById(@PathVariable Long id) {
        try {
            Map<String, Object> game = gameService.getGameById(id);
            if (game == null) {
                return CommonResponse.error("游戏不存在");
            }
            return CommonResponse.success(game);
        } catch (Exception e) {
            return CommonResponse.error("获取游戏详情失败: " + e.getMessage());
        }
    }

    /**
     * 新增游戏
     */
    @PostMapping
    public CommonResponse<Map<String, Object>> createGame(@RequestBody Map<String, Object> gameData) {
        try {
            Map<String, Object> game = gameService.createGame(gameData);
            return CommonResponse.success(game);
        } catch (Exception e) {
            return CommonResponse.error("创建游戏失败: " + e.getMessage());
        }
    }

    /**
     * 更新游戏
     */
    @PutMapping("/{id}")
    public CommonResponse<Map<String, Object>> updateGame(
            @PathVariable Long id,
            @RequestBody Map<String, Object> gameData) {
        try {
            Map<String, Object> game = gameService.updateGame(id, gameData);
            if (game == null) {
                return CommonResponse.error("游戏不存在");
            }
            return CommonResponse.success(game);
        } catch (Exception e) {
            return CommonResponse.error("更新游戏失败: " + e.getMessage());
        }
    }

    /**
     * 删除游戏
     */
    @DeleteMapping("/{id}")
    public CommonResponse<Void> deleteGame(@PathVariable Long id) {
        try {
            gameService.deleteGame(id);
            return CommonResponse.success(null);
        } catch (Exception e) {
            return CommonResponse.error("删除游戏失败: " + e.getMessage());
        }
    }

    /**
     * 上下架游戏
     */
    @PutMapping("/{id}/status")
    public CommonResponse<Void> updateGameStatus(
            @PathVariable Long id,
            @RequestParam Boolean isPublished) {
        try {
            gameService.updateGameStatus(id, isPublished);
            return CommonResponse.success(null);
        } catch (Exception e) {
            return CommonResponse.error("更新状态失败: " + e.getMessage());
        }
    }
}
