package com.cheng.cms.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Game Image Entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "game_images")
public class GameImage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "game_id", nullable = false)
    private Long gameId;
    
    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;
    
    @Column(name = "image_type", length = 20)
    private String imageType = "screenshot";
    
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "width")
    private Integer width;
    
    @Column(name = "height")
    private Integer height;
    
    @Column(name = "upload_admin_id")
    private Long uploadAdminId;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
