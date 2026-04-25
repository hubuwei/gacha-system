package com.cheng.cms.controller;

import com.cheng.common.dto.CommonResponse;
import com.cheng.cms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * User Controller - 用户管理接口
 */
@RestController
@RequestMapping("/api/cms/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取用户列表
     */
    @GetMapping
    public CommonResponse<List<Map<String, Object>>> getUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer accountStatus,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Map<String, Object> result = userService.getUsers(username, accountStatus, page, size);
            return CommonResponse.success((List<Map<String, Object>>) result.get("list"));
        } catch (Exception e) {
            return CommonResponse.error("获取用户列表失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户状态（封禁/解封）
     */
    @PutMapping("/{id}/status")
    public CommonResponse<Void> updateUserStatus(
            @PathVariable Long id,
            @RequestParam Integer accountStatus) {
        try {
            userService.updateUserStatus(id, accountStatus);
            return CommonResponse.success(null);
        } catch (Exception e) {
            return CommonResponse.error("更新用户状态失败: " + e.getMessage());
        }
    }
}
