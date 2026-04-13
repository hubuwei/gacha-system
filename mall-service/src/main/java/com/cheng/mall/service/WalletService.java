package com.cheng.mall.service;

import com.cheng.mall.entity.Transaction;
import com.cheng.mall.entity.UserWallet;
import com.cheng.mall.repository.TransactionRepository;
import com.cheng.mall.repository.UserWalletRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 钱包服务
 */
@Slf4j
@Service
public class WalletService {
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private UserWalletRepository userWalletRepository;
    
    /**
     * 获取用户钱包信息
     */
    public Map<String, Object> getUserWallet(Long userId) {
        UserWallet wallet = userWalletRepository.findByUserId(userId)
            .orElseGet(() -> {
                // 如果钱包不存在，创建一个新的
                UserWallet newWallet = new UserWallet();
                newWallet.setUserId(userId);
                return userWalletRepository.save(newWallet);
            });
        
        Map<String, Object> result = new HashMap<>();
        result.put("userId", wallet.getUserId());
        result.put("balance", wallet.getBalance());
        result.put("frozenBalance", wallet.getFrozenBalance());
        result.put("totalRecharge", wallet.getTotalRecharge());
        result.put("totalConsumed", wallet.getTotalConsumed());
        
        return result;
    }
    
    /**
     * 用户充值
     */
    @Transactional
    public Map<String, Object> recharge(Long userId, BigDecimal amount, String paymentMethod) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("充值金额必须大于0");
        }
        
        // 获取或创建钱包
        UserWallet wallet = userWalletRepository.findByUserId(userId)
            .orElseGet(() -> {
                UserWallet newWallet = new UserWallet();
                newWallet.setUserId(userId);
                return userWalletRepository.save(newWallet);
            });
        
        BigDecimal balanceBefore = wallet.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(amount);
        
        // 更新钱包余额
        wallet.setBalance(balanceAfter);
        wallet.setTotalRecharge(wallet.getTotalRecharge().add(amount));
        userWalletRepository.save(wallet);
        
        // 生成充值单号
        String rechargeNo = "RCH" + System.currentTimeMillis() + String.format("%04d", (int)(Math.random() * 10000));
        
        // 创建交易记录
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setTransactionType("recharge");
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setRelatedOrderNo(rechargeNo);  // 设置充值单号
        transaction.setDescription("账户充值 - 单号: " + rechargeNo);
        transaction.setPaymentMethod(paymentMethod);
        transaction.setTransactionStatus("completed");
        transactionRepository.save(transaction);
        
        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("transactionId", transaction.getId());
        result.put("rechargeNo", rechargeNo);  // 返回充值单号
        result.put("amount", amount);
        result.put("balanceBefore", balanceBefore);
        result.put("balanceAfter", balanceAfter);
        result.put("createdAt", transaction.getCreatedAt());
        
        return result;
    }
    
    /**
     * 扣除余额（用于订单支付）
     */
    @Transactional
    public void deductBalance(Long userId, BigDecimal amount, Long orderId, String orderNo) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("扣款金额必须大于0");
        }
        
        UserWallet wallet = userWalletRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("用户钱包不存在"));
        
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("余额不足");
        }
        
        BigDecimal balanceBefore = wallet.getBalance();
        BigDecimal balanceAfter = balanceBefore.subtract(amount);
        
        // 更新钱包余额
        wallet.setBalance(balanceAfter);
        wallet.setTotalConsumed(wallet.getTotalConsumed().add(amount));
        userWalletRepository.save(wallet);
        
        // 创建交易记录
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setTransactionType("purchase");
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setRelatedOrderId(orderId);
        transaction.setRelatedOrderNo(orderNo);
        transaction.setDescription("购买游戏 - 订单号: " + orderNo);
        transaction.setTransactionStatus("completed");
        transactionRepository.save(transaction);
    }
    
    /**
     * 退款到余额
     */
    @Transactional
    public void refundToBalance(Long userId, BigDecimal amount, Long orderId, String orderNo, String reason) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("退款金额必须大于0");
        }
        
        UserWallet wallet = userWalletRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("用户钱包不存在"));
        
        BigDecimal balanceBefore = wallet.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(amount);
        
        // 更新钱包余额
        wallet.setBalance(balanceAfter);
        wallet.setTotalConsumed(wallet.getTotalConsumed().subtract(amount));
        userWalletRepository.save(wallet);
        
        // 创建交易记录
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setTransactionType("refund");
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setRelatedOrderId(orderId);
        transaction.setRelatedOrderNo(orderNo);
        transaction.setDescription("订单退款 - " + reason);
        transaction.setTransactionStatus("completed");
        transactionRepository.save(transaction);
    }
    
    /**
     * 获取用户交易记录
     */
    public List<Map<String, Object>> getTransactions(Long userId, String type) {
        List<Transaction> transactions;
        
        if (type != null && !type.equals("all")) {
            transactions = transactionRepository.findByUserIdAndTransactionTypeOrderByCreatedAtDesc(
                userId, type
            );
        } else {
            transactions = transactionRepository.findByUserIdOrderByCreatedAtDesc(
                userId,
                PageRequest.of(0, 100)
            ).getContent();
        }
        
        return transactions.stream()
            .map(this::convertToMap)
            .collect(Collectors.toList());
    }
    
    /**
     * 创建交易记录
     */
    public Transaction createTransaction(Long userId, String type, Double amount, 
                                         Double balanceBefore, Double balanceAfter,
                                         String description, String paymentMethod) {
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setTransactionType(type);
        transaction.setAmount(new BigDecimal(amount));
        transaction.setBalanceBefore(new BigDecimal(balanceBefore));
        transaction.setBalanceAfter(new BigDecimal(balanceAfter));
        transaction.setDescription(description);
        transaction.setPaymentMethod(paymentMethod);
        transaction.setTransactionStatus("completed");
        
        return transactionRepository.save(transaction);
    }
    
    /**
     * 转换为 Map
     */
    private Map<String, Object> convertToMap(Transaction transaction) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", transaction.getId());
        map.put("userId", transaction.getUserId());
        map.put("transactionType", transaction.getTransactionType());
        map.put("amount", transaction.getAmount());
        map.put("balanceBefore", transaction.getBalanceBefore());
        map.put("balanceAfter", transaction.getBalanceAfter());
        map.put("relatedOrderId", transaction.getRelatedOrderId());
        map.put("relatedOrderNo", transaction.getRelatedOrderNo());
        map.put("description", transaction.getDescription());
        map.put("paymentMethod", transaction.getPaymentMethod());
        map.put("transactionStatus", transaction.getTransactionStatus());
        map.put("createdAt", transaction.getCreatedAt());
        return map;
    }
}
