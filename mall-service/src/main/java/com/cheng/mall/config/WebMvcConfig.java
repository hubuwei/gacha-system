package com.cheng.mall.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web MVC 配置类 - 用于配置静态资源访问
 * 支持本地开发和生产环境自动适配
 */
@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Value("${static.resources.game-papers-path:}")
    private String gamePapersPath;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String gamePapersDir;
        
        // 如果配置了绝对路径，直接使用（生产环境）
        if (gamePapersPath != null && !gamePapersPath.isEmpty()) {
            gamePapersDir = gamePapersPath;
            log.info("[WebMvcConfig] 使用配置的GamePapers路径: {}", gamePapersDir);
        } else {
            // 否则自动检测（本地开发环境）
            String userDir = System.getProperty("user.dir");
            log.info("[WebMvcConfig] 当前工作目录: {}", userDir);
            
            // GamePapers 在项目根目录下,需要向上一级
            String projectRoot = new File(userDir).getParent();
            gamePapersDir = projectRoot + File.separator + "GamePapers";
            log.info("[WebMvcConfig] 自动检测到项目根目录: {}", projectRoot);
            log.info("[WebMvcConfig] 自动检测到GamePapers目录: {}", gamePapersDir);
        }
        
        // 检查目录是否存在
        File gamePapersFolder = new File(gamePapersDir);
        if (gamePapersFolder.exists() && gamePapersFolder.isDirectory()) {
            log.info("[WebMvcConfig] ✅ GamePapers目录存在，文件数量: {}", gamePapersFolder.list().length);
        } else {
            log.warn("[WebMvcConfig] ⚠️ GamePapers目录不存在: {}", gamePapersDir);
            log.warn("[WebMvcConfig] 请检查配置项 static.resources.game-papers-path 或确保目录存在");
        }
        
        // 注册静态资源处理器
        // 注意：因为 context-path 是 /api，所以路径不需要再加上 /api 前缀
        registry.addResourceHandler("/GamePapers/**")
                .addResourceLocations("file:" + gamePapersDir + File.separator);
        log.info("[WebMvcConfig] 已注册静态资源映射: /GamePapers/** -> file:{}", gamePapersDir + File.separator);
    }
}
