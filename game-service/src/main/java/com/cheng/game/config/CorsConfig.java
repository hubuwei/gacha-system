package com.cheng.game.config;

import org.springframework.context.annotation.Configuration;

/**
 * 全局跨域配置 - 已移至 SecurityConfig 中统一配置
 */
@Configuration
public class CorsConfig {
    // CORS 配置已移至 SecurityConfig
    // 使用 allowedOriginPatterns 替代 allowedOrigins 以支持 allowCredentials(true)
}
