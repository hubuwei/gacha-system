package com.cheng.mall.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 游戏详情 DTO(包含分类、标签、配置要求)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameDetailDTO {
    
    // 游戏基本信息
    private Long id;
    private String title;
    private String shortDescription;
    private String fullDescription;
    private String coverImage;
    private String bannerImage;
    private String screenshots;
    private String trailerUrl;
    private BigDecimal basePrice;
    private BigDecimal currentPrice;
    private Integer discountRate;
    private Boolean isFeatured;
    private Boolean isOnSale;
    private String developer;
    private String publisher;
    private BigDecimal rating;
    private Integer ratingCount;
    private Long totalSales;
    private Integer totalReviews;
    private Long fileSize;
    private String version;
    
    // 分类列表
    private List<String> categories;
    
    // 标签列表
    private List<GameTagDTO> tags;
    
    // 系统配置要求
    private GameSystemRequirementDTO systemRequirements;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GameTagDTO {
        private Integer id;
        private String name;
        private String color;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GameSystemRequirementDTO {
        private String osMin;
        private String osRecommended;
        private String cpuMin;
        private String cpuRecommended;
        private String ramMin;
        private String ramRecommended;
        private String gpuMin;
        private String gpuRecommended;
        private String directxMin;
        private String directxRecommended;
        private String storageMin;
        private String storageRecommended;
        private String networkMin;
        private String networkRecommended;
        private String soundCardMin;
        private String soundCardRecommended;
        private String additionalNotes;
    }
}
