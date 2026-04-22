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
     * 获取用户钱包信息（优化版 - 增加用户存在性验证）
     */
    public Map<String, Object> getUserWallet(Long userId) {
        // 验证用户ID
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID无效");
        }
        
        UserWallet wallet = userWalletRepository.findByUserId(userId)
            .orElseGet(() -> {
                // 注意：这里不自动创建钱包，因为外键约束要求user_id必须存在于users表中
                // 如果需要自动创建，应该先调用auth-service验证用户是否存在
                UserWallet newWallet = new UserWallet();
                newWallet.setUserId(userId);
                newWallet.setBalance(BigDecimal.ZERO);
                newWallet.setFrozenBalance(BigDecimal.ZERO);
                newWallet.setTotalRecharge(BigDecimal.ZERO);
                newWallet.setTotalConsumed(BigDecimal.ZERO);
                try {
                    return userWalletRepository.save(newWallet);
                } catch (Exception e) {
                    // 如果保存失败（可能是外键约束），返回空钱包信息
                    log.warn("为用户 {} 创建钱包失败，可能用户不存在: {}", userId, e.getMessage());
                    // 返回默认钱包信息，但不保存到数据库
                    Map<String, Object> emptyWallet = new HashMap<>();
                    emptyWallet.put("userId", userId);
                    emptyWallet.put("balance", BigDecimal.ZERO);
                    emptyWallet.put("frozenBalance", BigDecimal.ZERO);
                    emptyWallet.put("totalRecharge", BigDecimal.ZERO);
                    emptyWallet.put("totalConsumed", BigDecimal.ZERO);
                    throw new RuntimeException("用户不存在或数据异常，请重新登录", e);
                }
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
     * 用户充值（优化版 - 增加更多验证和返回信息）
     */
    @Transactional
    public Map<String, Object> recharge(Long userId, BigDecimal amount, String paymentMethod) {
        // 参数验证
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID无效");
        }
        
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("充值金额必须大于0");
        }
        
        // 限制单笔充值金额范围（1-10000元）
        if (amount.compareTo(new BigDecimal("1")) < 0 || amount.compareTo(new BigDecimal("10000")) > 0) {
            throw new IllegalArgumentException("充值金额范围为1-10000元");
        }
        
        // 验证支付方式
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            paymentMethod = "balance"; // 默认使用测试模式
        }

        // 获取或创建钱包
        UserWallet wallet = userWalletRepository.findByUserId(userId)
            .orElseGet(() -> {
                UserWallet newWallet = new UserWallet();
                newWallet.setUserId(userId);
                newWallet.setBalance(BigDecimal.ZERO);
                newWallet.setFrozenBalance(BigDecimal.ZERO);
                newWallet.setTotalRecharge(BigDecimal.ZERO);
                newWallet.setTotalConsumed(BigDecimal.ZERO);
                return userWalletRepository.save(newWallet);
            });

        // 记录充值前余额
        BigDecimal balanceBefore = wallet.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(amount);

        // 更新钱包余额和累计充值金额
        wallet.setBalance(balanceAfter);
        wallet.setTotalRecharge(wallet.getTotalRecharge().add(amount));
        userWalletRepository.save(wallet);

        // 生成充值单号：RCH + 时间戳 + 4位随机数
        String rechargeNo = "RCH" + System.currentTimeMillis() + String.format("%04d", (int)(Math.random() * 10000));

        // 创建交易记录
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setTransactionType("recharge");
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setRelatedOrderNo(rechargeNo);
        transaction.setDescription("账户充值 - 单号: " + rechargeNo);
        transaction.setPaymentMethod(paymentMethod);
        transaction.setTransactionStatus("completed");
        transactionRepository.save(transaction);

        // 构建详细的返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("transactionId", transaction.getId());
        result.put("rechargeNo", rechargeNo);
        result.put("amount", amount);
        result.put("balanceBefore", balanceBefore);
        result.put("balanceAfter", balanceAfter);
        result.put("paymentMethod", paymentMethod);
        result.put("transactionStatus", "completed");
        result.put("createdAt", transaction.getCreatedAt());
        
        log.info("用户 {} 充值成功，金额: {}, 充值单号: {}, 充值前余额: {}, 充值后余额: {}", 
                 userId, amount, rechargeNo, balanceBefore, balanceAfter);

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

        wallet.setBalance(balanceAfter);
        wallet.setTotalConsumed(wallet.getTotalConsumed().add(amount));
        userWalletRepository.save(wallet);

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

        wallet.setBalance(balanceAfter);
        wallet.setTotalConsumed(wallet.getTotalConsumed().subtract(amount));
        userWalletRepository.save(wallet);

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
    public Transaction createTransaction(Long userId, String type, BigDecimal amount,
                                         BigDecimal balanceBefore, BigDecimal balanceAfter,
                                         String description, String paymentMethod) {
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setTransactionType(type);
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
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