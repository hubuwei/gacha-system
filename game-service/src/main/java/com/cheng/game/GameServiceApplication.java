package com.cheng.game;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EntityScan(basePackages = "com.cheng.common.entity")
@EnableJpaRepositories(basePackages = "com.cheng.common.repository")
@EnableScheduling // 启用定时任务
public class GameServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(GameServiceApplication.class, args);
    }
}
