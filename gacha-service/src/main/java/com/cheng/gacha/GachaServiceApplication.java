package com.cheng.gacha;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.cheng.common.entity")
@EnableJpaRepositories(basePackages = "com.cheng.common.repository")
public class GachaServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(GachaServiceApplication.class, args);
    }
}
