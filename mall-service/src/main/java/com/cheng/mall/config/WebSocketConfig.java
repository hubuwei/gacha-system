package com.cheng.mall.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket配置类
 * 用于实现实时消息推送（好友通知、在线状态等）
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 配置消息代理
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用简单消息代理，用于向客户端推送消息
        config.enableSimpleBroker("/topic", "/queue");
        
        // 设置应用目标前缀，客户端发送消息时使用
        config.setApplicationDestinationPrefixes("/app");
        
        // 设置用户目的地前缀，用于点对点消息推送
        config.setUserDestinationPrefix("/user");
    }

    /**
     * 注册STOMP端点
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册WebSocket端点，前端通过这个端点连接
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*")  // 允许跨域
                .withSockJS();  // 启用SockJS fallback
        
        // 原生WebSocket端点（不使用SockJS）
        registry.addEndpoint("/ws-native")
                .setAllowedOrigins("*");
    }
}
