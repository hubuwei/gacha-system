package com.cheng.mall.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 游戏标签关联表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "game_tag_mapping")
public class GameTagMapping {
    
    @EmbeddedId
    private GameTagMappingId id;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Embeddable
    public static class GameTagMappingId implements java.io.Serializable {
        @Column(name = "game_id")
        private Long gameId;
        
        @Column(name = "tag_id")
        private Integer tagId;
    }
}
