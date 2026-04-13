package com.cheng.common.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 大区实体类（已废弃，使用 Region 替代）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "game_servers")
public class GameServer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String serverCode;  // 服务器代码，如"east-china-1"
    
    @Column(nullable = false, length = 100)
    private String serverName;  // 服务器名称，如"华东一区"
    
    @Column(nullable = false)
    private Integer status = 1;  // 1:正常，0:维护中
    
    private LocalDateTime createdAt = LocalDateTime.now();
}
