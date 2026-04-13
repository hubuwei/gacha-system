package com.cheng.game.controller;

import com.cheng.common.dto.CommonResponse;
import com.cheng.common.entity.Wallet;
import com.cheng.game.service.PointsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 积分控制器
 */
@RestController
@RequestMapping("/api/points")
@CrossOrigin(origins = "*")
public class PointsController {
    
    @Autowired
    private PointsService pointsService;
    
    /**
     * 查询用户积分（返回钱包信息）
     */
    @GetMapping("/{userId}")
    public CommonResponse<Wallet> getPoints(@PathVariable Long userId) {
        try {
            Wallet wallet = pointsService.getUserPoints(userId);
            return CommonResponse.success(wallet);
        } catch (Exception e) {
            return CommonResponse.error(e.getMessage());
        }
    }
}
