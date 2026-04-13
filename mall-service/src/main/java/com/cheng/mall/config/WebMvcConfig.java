package com.cheng.mall.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web MVC 配置类 - 用于配置静态资源访问
 */
@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取项目根目录(mall-service的父目录)
        String userDir = System.getProperty("user.dir");
        log.info("[WebMvcConfig] 当前工作目录: {}", userDir);
        
        // GamePapers 在项目根目录下,需要向上一级
        String projectRoot = new File(userDir).getParent();
        String gamePapersDir = projectRoot + File.separator + "GamePapers";
        log.info("[WebMvcConfig] 项目根目录: {}", projectRoot);
        log.info("[WebMvcConfig] GamePapers 目录路径: {}", gamePapersDir);
        
        // 检查目录是否存在
        File gamePapersFolder = new File(gamePapersDir);
        if (gamePapersFolder.exists() && gamePapersFolder.isDirectory()) {
            log.info("[WebMvcConfig] GamePapers 目录存在,文件数量: {}", gamePapersFolder.list().length);
        } else {
            log.warn("[WebMvcConfig] GamePapers 目录不存在: {}", gamePapersDir);
        }
        
        // 注册静态资源处理器
        // 注意：因为 context-path 是 /api，所以路径需要加上 /api 前缀
        registry.addResourceHandler("/api/images/games/**")
                .addResourceLocations("file:" + gamePapersDir + File.separator);
        log.info("[WebMvcConfig] 已注册 /api/images/games/** -> file:{}", gamePapersDir + File.separator);
        
        // 也可以直接访问 /api/GamePapers/ 路径
        registry.addResourceHandler("/api/GamePapers/**")
                .addResourceLocations("file:" + gamePapersDir + File.separator);
        log.info("[WebMvcConfig] 已注册 /api/GamePapers/** -> file:{}", gamePapersDir + File.separator);
    }
}
