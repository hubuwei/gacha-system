package com.cheng.cms.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * System Configuration Entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "system_configs")
public class SystemConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "config_key", unique = true, nullable = false, length = 100)
    private String configKey;
    
    @Column(name = "config_value", nullable = false, columnDefinition = "TEXT")
    private String configValue;
    
    @Column(name = "config_type", length = 20)
    private String configType = "string";
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "is_public")
    private Boolean isPublic = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
