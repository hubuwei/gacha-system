package com.cheng.mall.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易记录实体
 */
@Data
@Entity
@Table(name = "transactions")
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "transaction_type", nullable = false, length = 20)
    private String transactionType; // recharge, purchase, refund, withdraw
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "balance_before", precision = 10, scale = 2)
    private BigDecimal balanceBefore;
    
    @Column(name = "balance_after", precision = 10, scale = 2)
    private BigDecimal balanceAfter;
    
    @Column(name = "related_order_id")
    private Long relatedOrderId;
    
    @Column(name = "related_order_no", length = 50)
    private String relatedOrderNo;
    
    @Column(length = 500)
    private String description;
    
    @Column(name = "payment_method", length = 20)
    private String paymentMethod;
    
    @Column(name = "transaction_status", length = 20)
    private String transactionStatus = "completed";
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
