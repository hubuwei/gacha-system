package com.cheng.cms.service;

import com.cheng.cms.entity.Banner;
import com.cheng.cms.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Banner管理Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BannerService {
    
    private final BannerRepository bannerRepository;
    
    /**
     * 获取所有Banner列表
     */
    public List<Banner> getAllBanners() {
        return bannerRepository.findAllByOrderBySortOrderAsc();
    }
    
    /**
     * 获取启用的Banner列表（前端使用）
     */
    public List<Banner> getActiveBanners() {
        return bannerRepository.findByIsActiveTrueOrderBySortOrderAsc();
    }
    
    /**
     * 创建Banner
     */
    @Transactional
    public Banner createBanner(Banner banner) {
        if (banner.getSortOrder() == null) {
            banner.setSortOrder(0);
        }
        if (banner.getIsActive() == null) {
            banner.setIsActive(true);
        }
        
        Banner saved = bannerRepository.save(banner);
        log.info("创建Banner: {}", saved.getId());
        return saved;
    }
    
    /**
     * 更新Banner
     */
    @Transactional
    public Banner updateBanner(Long id, Banner bannerData) {
        Banner banner = bannerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Banner不存在"));
        
        if (bannerData.getTitle() != null) {
            banner.setTitle(bannerData.getTitle());
        }
        if (bannerData.getImageUrl() != null) {
            banner.setImageUrl(bannerData.getImageUrl());
        }
        if (bannerData.getTargetType() != null) {
            banner.setTargetType(bannerData.getTargetType());
        }
        if (bannerData.getTargetId() != null) {
            banner.setTargetId(bannerData.getTargetId());
        }
        if (bannerData.getTargetUrl() != null) {
            banner.setTargetUrl(bannerData.getTargetUrl());
        }
        if (bannerData.getSortOrder() != null) {
            banner.setSortOrder(bannerData.getSortOrder());
        }
        if (bannerData.getIsActive() != null) {
            banner.setIsActive(bannerData.getIsActive());
        }
        if (bannerData.getStartTime() != null) {
            banner.setStartTime(bannerData.getStartTime());
        }
        if (bannerData.getEndTime() != null) {
            banner.setEndTime(bannerData.getEndTime());
        }
        
        Banner updated = bannerRepository.save(banner);
        log.info("更新Banner: {}", id);
        return updated;
    }
    
    /**
     * 删除Banner
     */
    @Transactional
    public void deleteBanner(Long id) {
        bannerRepository.deleteById(id);
        log.info("删除Banner: {}", id);
    }
    
    /**
     * 切换Banner状态
     */
    @Transactional
    public Banner toggleStatus(Long id, Boolean isActive) {
        Banner banner = bannerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Banner不存在"));
        
        banner.setIsActive(isActive);
        Banner updated = bannerRepository.save(banner);
        
        log.info("Banner {} 状态更新为: {}", id, isActive);
        return updated;
    }
}
