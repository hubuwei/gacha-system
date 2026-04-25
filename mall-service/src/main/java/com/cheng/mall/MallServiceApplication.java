package com.cheng.mall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {
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
// @EnableElasticsearchRepositories(basePackages = {
//         "com.cheng.mall.es.repository"
// })  // 本地开发时禁用ES，使用服务器ES时取消注释
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableJpaAuditing
public class MallServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MallServiceApplication.class, args);
        System.out.println("====================================");
        System.out.println("    游戏商城服务启动成功！");
        System.out.println("    访问地址：http://localhost:8081/api");
        System.out.println("====================================");
    }
}