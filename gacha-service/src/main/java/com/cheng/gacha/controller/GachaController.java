package com.cheng.gacha.controller;

import com.cheng.common.dto.CommonResponse;
import com.cheng.common.dto.GachaBatchResult;
import com.cheng.common.dto.GachaRequest;
import com.cheng.gacha.service.GachaBusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 抽奖控制器
 */
@RestController
@RequestMapping("/api/gacha")
@CrossOrigin(origins = "*")
public class GachaController {
    
    @Autowired
    private GachaBusinessService gachaBusinessService;
    
    /**
     * 执行抽奖
     */
    @PostMapping("/draw")
    public CommonResponse<GachaBatchResult> draw(@RequestBody GachaRequest request) {
        try {
            GachaBatchResult result = gachaBusinessService.executeGacha(request);
            return CommonResponse.success(result);
        } catch (Exception e) {
            return CommonResponse.error(e.getMessage());
        }
    }
}
