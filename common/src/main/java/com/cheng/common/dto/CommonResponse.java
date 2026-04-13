package com.cheng.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 统一响应 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommonResponse<T> {
    
    private Integer code;
    private String message;
    private T data;
    
    public static <T> CommonResponse<T> success(T data) {
        return CommonResponse.<T>builder()
                .code(200)
                .message("success")
                .data(data)
                .build();
    }
    
    public static <T> CommonResponse<T> error(String message) {
        return CommonResponse.<T>builder()
                .code(500)
                .message(message)
                .build();
    }
    
    public static <T> CommonResponse<T> error(Integer code, String message) {
        return CommonResponse.<T>builder()
                .code(code)
                .message(message)
                .build();
    }
}
