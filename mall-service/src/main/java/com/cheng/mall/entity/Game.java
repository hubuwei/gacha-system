package com.cheng.mall.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 游戏实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "games")
public class Game {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    
    @Column(name = "short_description", length = 500)
    private String shortDescription;
    
    @Column(name = "full_description", columnDefinition = "TEXT")
    private String fullDescription;
    
    @Column(name = "cover_image", length = 500)
    private String coverImage;
    
    @Column(name = "banner_image", length = 500)
    private String bannerImage;
    
    @Column(name = "screenshots", columnDefinition = "JSON")
    private String screenshots;
    
    @Column(name = "trailer_url", length = 500)
    private String trailerUrl;
    
    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;
    
    @Column(name = "current_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal currentPrice;
    
    @Column(name = "discount_rate")
    private Integer discountRate = 0;
    
    @Column(name = "discount_start")
    private LocalDateTime discountStart;
    
    @Column(name = "discount_end")
    private LocalDateTime discountEnd;
    
    @Column(name = "is_featured")
    private Boolean isFeatured = false;
    
    @Column(name = "is_on_sale")
    private Boolean isOnSale = false;
    
    @Column(name = "release_date")
    private LocalDate releaseDate;
    
    @Column(name = "developer", length = 100)
    private String developer;
    
    @Column(name = "publisher", length = 100)
    private String publisher;
    
    @Column(name = "rating", precision = 3, scale = 2)
    private BigDecimal rating = BigDecimal.ZERO;
    
    @Column(name = "rating_count")
    private Integer ratingCount = 0;
    
    @Column(name = "total_sales")
    private Long totalSales = 0L;
    
    @Column(name = "total_reviews")
    private Integer totalReviews = 0;
    
    @Column(name = "download_count")
    private Integer downloadCount = 0;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "version", length = 20)
    private String version;
    
    @Column(name = "last_update")
    private LocalDateTime lastUpdate;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
