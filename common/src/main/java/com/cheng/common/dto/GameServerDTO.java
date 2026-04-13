package com.cheng.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 大区服务器 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameServerDTO {
    
    private Long id;
    private String serverCode;
    private String serverName;
    private Integer status;
}
