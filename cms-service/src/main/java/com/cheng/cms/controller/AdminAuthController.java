package com.cheng.cms.controller;

import com.cheng.cms.service.AdminAuthService;
import com.cheng.common.dto.CommonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cms/auth")
@CrossOrigin(origins = "*")
public class AdminAuthController {
    
    @Autowired
    private AdminAuthService adminAuthService;
    
    /**
     * Admin login
     */
    @PostMapping("/login")
    public CommonResponse<Map<String, Object>> login(
            @RequestBody Map<String, String> loginRequest,
            HttpServletRequest request) {
        try {
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");
            String ip = request.getRemoteAddr();
            
            if (username == null || password == null) {
                return CommonResponse.error(400, "用户名和密码不能为空");
            }
            
            Map<String, Object> result = adminAuthService.login(username, password, ip);
            return CommonResponse.success(result);
        } catch (Exception e) {
            return CommonResponse.error(e.getMessage());
        }
    }
    
    /**
     * Test endpoint to verify password
     */
    @GetMapping("/test-password")
    public CommonResponse<Map<String, Object>> testPassword() {
        try {
            String testPassword = "admin123";
            String newHash = adminAuthService.encodePassword(testPassword);
            boolean matches = adminAuthService.passwordMatches(testPassword, newHash);
            
            Map<String, Object> result = new HashMap<>();
            result.put("original_password", testPassword);
            result.put("generated_hash", newHash);
            result.put("matches", matches);
            
            return CommonResponse.success(result);
        } catch (Exception e) {
            return CommonResponse.error(e.getMessage());
        }
    }
    
    /**
     * Get current admin info
     */
    @GetMapping("/info")
    public CommonResponse<Map<String, Object>> getAdminInfo(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return CommonResponse.error(401, "未授权访问");
            }
            
            // TODO: Validate token and get admin info
            
            return CommonResponse.error(500, "Not implemented yet");
        } catch (Exception e) {
            return CommonResponse.error(e.getMessage());
        }
    }
}
