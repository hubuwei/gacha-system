package com.cheng.mall.controller;

import com.cheng.common.dto.CommonResponse;
import com.cheng.mall.service.BannerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 轮播图控制器
 */
@Slf4j
@RestController
@RequestMapping("/banners")
@CrossOrigin(origins = "*")
public class BannerController {
    
    @Autowired
    private BannerService bannerService;
    
    /**
     * 获取所有启用的轮播图
     */
    @GetMapping("/active")
    public CommonResponse<List<Map<String, Object>>> getActiveBanners() {
        try {
            List<Map<String, Object>> banners = bannerService.getActiveBanners();
            return CommonResponse.success(banners);
        } catch (Exception e) {
            log.error("获取轮播图失败", e);
            return CommonResponse.error("获取轮播图失败：" + e.getMessage());
        }
    }
}
