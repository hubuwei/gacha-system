package com.cheng.mall.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 游戏系统配置要求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "game_system_requirements")
public class GameSystemRequirement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "game_id", unique = true, nullable = false)
    private Long gameId;
    
    @Column(name = "os_min", length = 200)
    private String osMin;
    
    @Column(name = "os_recommended", length = 200)
    private String osRecommended;
    
    @Column(name = "cpu_min", length = 200)
    private String cpuMin;
    
    @Column(name = "cpu_recommended", length = 200)
    private String cpuRecommended;
    
    @Column(name = "ram_min", length = 50)
    private String ramMin;
    
    @Column(name = "ram_recommended", length = 50)
    private String ramRecommended;
    
    @Column(name = "gpu_min", length = 200)
    private String gpuMin;
    
    @Column(name = "gpu_recommended", length = 200)
    private String gpuRecommended;
    
    @Column(name = "directx_min", length = 50)
    private String directxMin;
    
    @Column(name = "directx_recommended", length = 50)
    private String directxRecommended;
    
    @Column(name = "storage_min", length = 50, nullable = false)
    private String storageMin;
    
    @Column(name = "storage_recommended", length = 50)
    private String storageRecommended;
    
    @Column(name = "network_min", length = 100)
    private String networkMin;
    
    @Column(name = "network_recommended", length = 100)
    private String networkRecommended;
    
    @Column(name = "sound_card_min", length = 100)
    private String soundCardMin;
    
    @Column(name = "sound_card_recommended", length = 100)
    private String soundCardRecommended;
    
    @Column(name = "additional_notes", columnDefinition = "TEXT")
    private String additionalNotes;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
