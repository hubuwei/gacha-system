package com.cheng.cms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {"com.cheng.common.entity", "com.cheng.cms.entity"})
@EnableJpaRepositories(basePackages = {"com.cheng.common.repository", "com.cheng.cms.repository"})
public class CmsServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(CmsServiceApplication.class, args);
    }
}
