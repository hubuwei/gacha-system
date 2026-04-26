package com.cheng.game.controller;

import com.cheng.common.dto.CommonResponse;
import com.cheng.common.dto.SeckillProductDTO;
import com.cheng.common.dto.SeckillRequest;
import com.cheng.common.dto.SeckillResult;
import com.cheng.game.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 抢购控制器 - 已弱化（功能保留但不启用）
 */
//@RestController
//@RequestMapping("/api/seckill")
//@CrossOrigin(origins = "*")
public class SeckillController {
    
    @Autowired
    private SeckillService seckillService;
    
    /**
     * 获取所有活跃的抢购商品
     */
    @GetMapping("/products")
    public CommonResponse<List<SeckillProductDTO>> getActiveProducts() {
        try {
            List<SeckillProductDTO> products = seckillService.getActiveProducts();
            return CommonResponse.success(products);
        } catch (Exception e) {
            return CommonResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取单个抢购商品详情
     */
    @GetMapping("/product/{id}")
    public CommonResponse<SeckillProductDTO> getProductDetail(@PathVariable Long id) {
        try {
            SeckillProductDTO product = seckillService.getProductDetail(id);
            return CommonResponse.success(product);
        } catch (Exception e) {
            return CommonResponse.error(e.getMessage());
        }
    }
    
    /**
     * 参与抢购
     */
    @PostMapping("/participate")
    public CommonResponse<SeckillResult> participate(@RequestBody SeckillRequest request) {
        try {
            SeckillResult result = seckillService.participate(request);
            return CommonResponse.success(result);
        } catch (Exception e) {
            return CommonResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取倒计时和状态信息
     */
    @GetMapping("/countdown/{productId}")
    public CommonResponse<Map<String, Object>> getCountdownInfo(
            @PathVariable Long productId,
            @RequestParam Long userId) {
        try {
            SeckillProductDTO product = seckillService.getProductDetail(productId);
            
            // 计算下次抢购时间
            LocalDateTime nextSeckillTime = seckillService.calculateNextSeckillTime(
                    product.getIntervalHours());
            
            // 检查用户是否已参与
            Boolean hasParticipated = seckillService.hasUserParticipated(
                    userId, productId, product.getIntervalHours());
            
            // 计算剩余秒数
            Duration duration = Duration.between(LocalDateTime.now(), nextSeckillTime);
            long remainingSeconds = Math.max(0, duration.getSeconds());
            
            Map<String, Object> result = new HashMap<>();
            result.put("remainingStock", product.getRemainingStock());
            result.put("nextSeckillTime", nextSeckillTime);
            result.put("remainingSeconds", remainingSeconds);
            result.put("hasParticipated", hasParticipated);
            result.put("intervalHours", product.getIntervalHours());
            result.put("seckillPoints", product.getSeckillPoints());
            
            return CommonResponse.success(result);
        } catch (Exception e) {
            return CommonResponse.error(e.getMessage());
        }
    }
    
    /**
     * 检查用户参与状态
     */
    @GetMapping("/status")
    public CommonResponse<Map<String, Object>> checkStatus(
            @RequestParam Long productId,
            @RequestParam Long userId) {
        try {
            SeckillProductDTO product = seckillService.getProductDetail(productId);
            
            // 检查用户是否已参与
            Boolean hasParticipated = seckillService.hasUserParticipated(
                    userId, productId, product.getIntervalHours());
            
            Map<String, Object> result = new HashMap<>();
            result.put("hasParticipated", hasParticipated);
            result.put("remainingStock", product.getRemainingStock());
            result.put("isActive", product.getIsActive());
            
            return CommonResponse.success(result);
        } catch (Exception e) {
            return CommonResponse.error(e.getMessage());
        }
    }
}
