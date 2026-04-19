package com.cheng.mall.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置类
 * 只有在 spring.rabbitmq.enabled=true 时才启用
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class RabbitMQConfig {
    
    // ==================== 队列名称常量 ====================
    public static final String NOTIFICATION_QUEUE = "notification.queue";
    public static final String ORDER_CREATE_QUEUE = "order.create.queue";
    public static final String ORDER_EMAIL_QUEUE = "order.email.queue";
    public static final String GAME_ES_QUEUE = "game.es.queue";
    public static final String GAME_CACHE_QUEUE = "game.cache.queue";
    public static final String LOG_AUDIT_QUEUE = "log.audit.queue";
    
    // ==================== 交换机名称常量 ====================
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String GAME_EXCHANGE = "game.event.exchange";  // Fanout 交换机
    public static final String GAME_EVENT_EXCHANGE = "game.event.exchange";
    public static final String LOG_EXCHANGE = "log.exchange";
    
    // ==================== 路由键常量 ====================
    public static final String NOTIFICATION_ROUTING_KEY = "notification.send";
    public static final String ORDER_CREATE_ROUTING_KEY = "order.create";
    public static final String ORDER_EMAIL_ROUTING_KEY = "order.email";
    public static final String GAME_ES_ROUTING_KEY = "game.es.update";
    public static final String GAME_CACHE_ROUTING_KEY = "game.cache.clear";
    public static final String LOG_AUDIT_ROUTING_KEY = "log.audit";
    
    @Value("${spring.rabbitmq.host:111.228.12.167}")
    private String host;
    
    @Value("${spring.rabbitmq.port:5672}")
    private int port;
    
    @Value("${spring.rabbitmq.username:admin}")
    private String username;
    
    @Value("${spring.rabbitmq.password:Xc037417!}")
    private String password;
    
    @Value("${spring.rabbitmq.virtual-host:/}")
    private String virtualHost;
    
    /**
     * 配置连接工厂
     */
    @Bean
    public ConnectionFactory connectionFactory() {
        try {
            CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
            connectionFactory.setHost(host);
            connectionFactory.setPort(port);
            connectionFactory.setUsername(username);
            connectionFactory.setPassword(password);
            connectionFactory.setVirtualHost(virtualHost);
            
            // 设置连接超时和重试策略
            connectionFactory.setConnectionTimeout(5000);
            connectionFactory.setRequestedHeartBeat(30);
            
            log.info("RabbitMQ 连接工厂配置完成: {}:{}", host, port);
            return connectionFactory;
        } catch (Exception e) {
            log.error("RabbitMQ 连接工厂配置失败", e);
            // 返回一个默认的连接工厂，避免启动失败
            return new CachingConnectionFactory();
        }
    }
    
    /**
     * 配置 RabbitTemplate
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        try {
            RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
            
            // 设置消息确认模式
            rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
                if (!ack) {
                    log.error("消息发送失败: {}", cause);
                }
            });
            
            log.info("RabbitTemplate 配置完成");
            return rabbitTemplate;
        } catch (Exception e) {
            log.error("RabbitTemplate 配置失败", e);
            // 返回一个默认的 RabbitTemplate，避免启动失败
            return new RabbitTemplate(connectionFactory);
        }
    }
}
