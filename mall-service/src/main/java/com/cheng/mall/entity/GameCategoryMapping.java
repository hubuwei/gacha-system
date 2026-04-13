package com.cheng.mall.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 游戏分类关联表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "game_category_mapping")
public class GameCategoryMapping {
    
    @EmbeddedId
    private GameCategoryMappingId id;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Embeddable
    public static class GameCategoryMappingId implements java.io.Serializable {
        @Column(name = "game_id")
        private Long gameId;
        
        @Column(name = "category_id")
        private Integer categoryId;
    }
}
