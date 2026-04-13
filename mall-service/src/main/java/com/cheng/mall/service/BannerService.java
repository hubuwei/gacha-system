package com.cheng.mall.service;

import com.cheng.mall.entity.Banner;
import com.cheng.mall.repository.BannerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 轮播图服务
 */
@Slf4j
@Service
public class BannerService {
    
    @Autowired
    private BannerRepository bannerRepository;
    
    /**
     * 获取所有启用的轮播图
     */
    public List<Map<String, Object>> getActiveBanners() {
        LocalDateTime now = LocalDateTime.now();
        List<Banner> banners = bannerRepository.findActiveBanners(now);
        
        return banners.stream()
            .map(this::convertToMap)
            .collect(Collectors.toList());
    }
    
    /**
     * 转换为 Map
     */
    private Map<String, Object> convertToMap(Banner banner) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", banner.getId());
        map.put("title", banner.getTitle());
        map.put("imageUrl", banner.getImageUrl());
        map.put("targetType", banner.getTargetType());
        map.put("targetId", banner.getTargetId());
        map.put("targetUrl", banner.getTargetUrl());
        map.put("sortOrder", banner.getSortOrder());
        return map;
    }
}
