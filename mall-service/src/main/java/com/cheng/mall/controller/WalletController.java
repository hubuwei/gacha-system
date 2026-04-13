package com.cheng.mall.controller;

import com.cheng.common.dto.CommonResponse;
import com.cheng.mall.entity.Transaction;
import com.cheng.mall.service.WalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 钱包控制器
 */
@Slf4j
@RestController
@RequestMapping("/wallet")
@CrossOrigin(origins = "*")
public class WalletController {
    
    @Autowired
    private WalletService walletService;
    
    /**
     * 获取用户钱包信息
     */
    @GetMapping("/balance")
    public CommonResponse<Map<String, Object>> getBalance(@RequestParam Long userId) {
        try {
            Map<String, Object> wallet = walletService.getUserWallet(userId);
            return CommonResponse.success(wallet);
        } catch (Exception e) {
            log.error("获取钱包信息失败", e);
            return CommonResponse.error("获取钱包信息失败：" + e.getMessage());
        }
    }
    
    /**
     * 用户充值
     */
    @PostMapping("/recharge")
    public CommonResponse<Map<String, Object>> recharge(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String paymentMethod = (String) request.getOrDefault("paymentMethod", "balance");
            
            Map<String, Object> result = walletService.recharge(userId, amount, paymentMethod);
            return CommonResponse.success(result);
        } catch (IllegalArgumentException e) {
            return CommonResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("充值失败", e);
            return CommonResponse.error("充值失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取用户交易记录
     */
    @GetMapping("/transactions")
    public CommonResponse<List<Map<String, Object>>> getTransactions(
            @RequestParam Long userId,
            @RequestParam(required = false) String type) {
        try {
            List<Map<String, Object>> transactions = walletService.getTransactions(userId, type);
            return CommonResponse.success(transactions);
        } catch (Exception e) {
            log.error("获取交易记录失败", e);
            return CommonResponse.error("获取交易记录失败：" + e.getMessage());
        }
    }
    
    /**
     * 创建充值记录（已废弃，使用 /recharge 接口）
     * @deprecated 使用 POST /wallet/recharge 代替
     */
    @Deprecated
    @PostMapping("/recharge-old")
    public CommonResponse<Map<String, Object>> createRechargeOld(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            Double amount = Double.valueOf(request.get("amount").toString());
            String paymentMethod = (String) request.get("paymentMethod");
            
            // TODO: 这里应该调用 auth-service 更新用户余额
            // 目前只是创建交易记录
            
            Transaction transaction = walletService.createTransaction(
                userId,
                "recharge",
                amount,
                0.0, // TODO: 获取充值前余额
                amount, // TODO: 计算充值后余额
                "充值 - " + paymentMethod,
                paymentMethod
            );
            
            Map<String, Object> result = new HashMap<>();
            result.put("transactionId", transaction.getId());
            result.put("amount", transaction.getAmount());
            
            return CommonResponse.success(result);
        } catch (Exception e) {
            log.error("创建充值记录失败", e);
            return CommonResponse.error("创建充值记录失败：" + e.getMessage());
        }
    }
}
