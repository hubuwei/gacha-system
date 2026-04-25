package com.cheng.cms.controller;

import com.cheng.common.dto.CommonResponse;
import com.cheng.cms.entity.Banner;
import com.cheng.cms.service.BannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Banner管理Controller
 */
@RestController
@RequestMapping("/api/cms/banners")
@RequiredArgsConstructor
public class BannerController {
    
    private final BannerService bannerService;
    
    /**
     * 获取所有Banner列表
     */
    @GetMapping
    public CommonResponse<List<Banner>> getBanners() {
        try {
            List<Banner> banners = bannerService.getAllBanners();
            return CommonResponse.success(banners);
        } catch (Exception e) {
            return CommonResponse.error("获取Banner列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取启用的Banner列表（前端使用）
     */
    @GetMapping("/active")
    public CommonResponse<List<Banner>> getActiveBanners() {
        try {
            List<Banner> banners = bannerService.getActiveBanners();
            return CommonResponse.success(banners);
        } catch (Exception e) {
            return CommonResponse.error("获取Banner列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建Banner
     */
    @PostMapping
    public CommonResponse<Banner> createBanner(@RequestBody Banner banner) {
        try {
            Banner created = bannerService.createBanner(banner);
            return CommonResponse.success(created);
        } catch (Exception e) {
            return CommonResponse.error("创建Banner失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新Banner
     */
    @PutMapping("/{id}")
    public CommonResponse<Banner> updateBanner(
            @PathVariable Long id,
            @RequestBody Banner banner) {
        try {
            Banner updated = bannerService.updateBanner(id, banner);
            return CommonResponse.success(updated);
        } catch (Exception e) {
            return CommonResponse.error("更新Banner失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除Banner
     */
    @DeleteMapping("/{id}")
    public CommonResponse<Void> deleteBanner(@PathVariable Long id) {
        try {
            bannerService.deleteBanner(id);
            return CommonResponse.success(null);
        } catch (Exception e) {
            return CommonResponse.error("删除Banner失败: " + e.getMessage());
        }
    }
    
    /**
     * 切换Banner状态
     */
    @PutMapping("/{id}/status")
    public CommonResponse<Banner> toggleStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body) {
        try {
            Boolean isActive = body.get("isActive");
            if (isActive == null) {
                return CommonResponse.error("请提供isActive参数");
            }
            Banner updated = bannerService.toggleStatus(id, isActive);
            return CommonResponse.success(updated);
        } catch (Exception e) {
            return CommonResponse.error("更新状态失败: " + e.getMessage());
        }
    }
}
