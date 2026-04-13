package com.cheng.auth.controller;

import com.cheng.auth.service.UserServerService;
import com.cheng.common.dto.CommonResponse;
import com.cheng.common.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户大区选择控制器
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserServerController {
    
    @Autowired
    private UserServerService userServerService;
    
    /**
     * 选择大区
     */
    @PostMapping("/{userId}/select-server")
    public CommonResponse<User> selectServer(
            @PathVariable Long userId,
            @RequestParam String serverCode) {
        try {
            User user = userServerService.selectServer(userId, serverCode);
            return CommonResponse.success(user);
        } catch (Exception e) {
            return CommonResponse.error(e.getMessage());
        }
    }
}
