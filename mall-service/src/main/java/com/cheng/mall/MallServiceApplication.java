package com.cheng.mall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {
        RedisAutoConfiguration.class,
        RedisRepositoriesAutoConfiguration.class,
        RabbitAutoConfiguration.class
})
@EntityScan(basePackages = {
        "com.cheng.mall.entity",
        "com.cheng.common.entity"
})
@EnableJpaRepositories(basePackages = {
        "com.cheng.mall.repository",
        "com.cheng.common.repository"
})
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableJpaAuditing
public class MallServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MallServiceApplication.class, args);
        System.out.println("====================================");
        System.out.println("    游戏商城服务启动成功！");
        System.out.println("    访问地址：http://localhost:20222/api");
        System.out.println("====================================");
    }
}