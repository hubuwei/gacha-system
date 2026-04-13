package com.cheng.game.controller;

import com.cheng.common.dto.CommonResponse;
import com.cheng.common.dto.RechargeRequest;
import com.cheng.common.entity.RechargeRecord;
import com.cheng.common.entity.User;
import com.cheng.common.entity.Wallet;
import com.cheng.common.repository.UserRepository;
import com.cheng.common.repository.WalletRepository;
import com.cheng.common.util.JwtUtil;
import com.cheng.game.service.RechargeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 充值控制器
 */
@RestController
@RequestMapping("/api/recharge")
public class RechargeController {
    
    @Autowired
    private RechargeService rechargeService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private WalletRepository walletRepository;
    
    /**
     * 充值
     */
    @PostMapping
    public CommonResponse<Map<String, Object>> recharge(
            @RequestBody RechargeRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            // 从 Token 中获取用户 ID
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return CommonResponse.error(401, "未授权访问");
            }
            
            String token = authorization.substring(7);
            
            // 验证 Token
            if (!JwtUtil.validateToken(token)) {
                return CommonResponse.error(401, "Token 无效或已过期");
            }
            
            // 获取用户 ID
            Long userId = JwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                return CommonResponse.error(401, "Token 无效或已过期");
            }
            
            RechargeRecord record = rechargeService.recharge(userId, request.getAmount());
            
            // 返回充值记录和更新后的余额
            Map<String, Object> result = new HashMap<>();
            result.put("record", record);
            result.put("balance", record.getAmount()); // 这里需要重新查询钱包
            
            // 重新查询钱包余额
            Wallet wallet = walletRepository.findById(userId).orElse(null);
            if (wallet != null) {
                result.put("balance", wallet.getBalance());
                result.put("points", wallet.getPoints());
            }
            
            return CommonResponse.success(result);
        } catch (Exception e) {
            return CommonResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取充值记录
     */
    @GetMapping("/records")
    public CommonResponse<List<RechargeRecord>> getRechargeRecords(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            // 从 Token 中获取用户 ID
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return CommonResponse.error(401, "未授权访问");
            }
            
            String token = authorization.substring(7);
            
            // 验证 Token
            if (!JwtUtil.validateToken(token)) {
                return CommonResponse.error(401, "Token 无效或已过期");
            }
            
            // 获取用户 ID
            Long userId = JwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                return CommonResponse.error(401, "Token 无效或已过期");
            }
            
            List<RechargeRecord> records = rechargeService.getRechargeRecords(userId);
            return CommonResponse.success(records);
        } catch (Exception e) {
            return CommonResponse.error(e.getMessage());
        }
    }
}
