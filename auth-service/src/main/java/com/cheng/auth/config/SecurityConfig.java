package com.cheng.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                // 允许访问静态资源
                .antMatchers("/", "/index.html", "/register.html", "/register-simple.html", "/css/**", "/js/**", "/images/**").permitAll()
                // 允许访问 API 接口
                .antMatchers("/api/**").permitAll()
                // 其他请求全部允许（简化配置）
                .anyRequest().permitAll()
            )
            .httpBasic().disable();
        return http.build();
    }
}
