package com.cheng.mall.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置类
 * 
 * 交换机和队列设计：
 * 1. order.exchange (Direct) - 订单相关消息
 *    - order.queue.create - 订单创建队列
 *    - order.queue.email - 邮件通知队列
 * 
 * 2. game.exchange (Fanout) - 游戏事件广播
 *    - game.queue.es - ES索引更新队列
 *    - game.queue.cache - 缓存清除队列
 * 
 * 3. log.exchange (Topic) - 日志收集
 *    - log.queue.audit - 审计日志队列
 */
@Configuration
public class RabbitMQConfig {
    
    // ==================== 订单相关 ====================
    
    /**
     * 订单交换机（Direct）
     */
    public static final String ORDER_EXCHANGE = "order.exchange";
    
    /**
     * 订单创建队列
     */
    public static final String ORDER_CREATE_QUEUE = "order.queue.create";
    public static final String ORDER_CREATE_ROUTING_KEY = "order.create";
    
    /**
     * 邮件通知队列
     */
    public static final String ORDER_EMAIL_QUEUE = "order.queue.email";
    public static final String ORDER_EMAIL_ROUTING_KEY = "order.email";
    
    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE, true, false);
    }
    
    @Bean
    public Queue orderCreateQueue() {
        return QueueBuilder.durable(ORDER_CREATE_QUEUE).build();
    }
    
    @Bean
    public Binding orderCreateBinding(Queue orderCreateQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(orderCreateQueue)
            .to(orderExchange)
            .with(ORDER_CREATE_ROUTING_KEY);
    }
    
    @Bean
    public Queue orderEmailQueue() {
        return QueueBuilder.durable(ORDER_EMAIL_QUEUE).build();
    }
    
    @Bean
    public Binding orderEmailBinding(Queue orderEmailQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(orderEmailQueue)
            .to(orderExchange)
            .with(ORDER_EMAIL_ROUTING_KEY);
    }
    
    // ==================== 游戏事件相关 ====================
    
    /**
     * 游戏事件交换机（Fanout - 广播）
     */
    public static final String GAME_EXCHANGE = "game.exchange";
    
    /**
     * ES 索引更新队列
     */
    public static final String GAME_ES_QUEUE = "game.queue.es";
    
    /**
     * 缓存清除队列
     */
    public static final String GAME_CACHE_QUEUE = "game.queue.cache";
    
    @Bean
    public FanoutExchange gameExchange() {
        return new FanoutExchange(GAME_EXCHANGE, true, false);
    }
    
    @Bean
    public Queue gameEsQueue() {
        return QueueBuilder.durable(GAME_ES_QUEUE).build();
    }
    
    @Bean
    public Binding gameEsBinding(Queue gameEsQueue, FanoutExchange gameExchange) {
        return BindingBuilder.bind(gameEsQueue).to(gameExchange);
    }
    
    @Bean
    public Queue gameCacheQueue() {
        return QueueBuilder.durable(GAME_CACHE_QUEUE).build();
    }
    
    @Bean
    public Binding gameCacheBinding(Queue gameCacheQueue, FanoutExchange gameExchange) {
        return BindingBuilder.bind(gameCacheQueue).to(gameExchange);
    }
    
    // ==================== 日志相关 ====================
    
    /**
     * 日志交换机（Topic）
     */
    public static final String LOG_EXCHANGE = "log.exchange";
    
    /**
     * 审计日志队列
     */
    public static final String LOG_AUDIT_QUEUE = "log.queue.audit";
    public static final String LOG_AUDIT_ROUTING_KEY = "log.audit.#";
    
    @Bean
    public TopicExchange logExchange() {
        return new TopicExchange(LOG_EXCHANGE, true, false);
    }
    
    @Bean
    public Queue logAuditQueue() {
        return QueueBuilder.durable(LOG_AUDIT_QUEUE).build();
    }
    
    @Bean
    public Binding logAuditBinding(Queue logAuditQueue, TopicExchange logExchange) {
        return BindingBuilder.bind(logAuditQueue)
            .to(logExchange)
            .with(LOG_AUDIT_ROUTING_KEY);
    }
}
