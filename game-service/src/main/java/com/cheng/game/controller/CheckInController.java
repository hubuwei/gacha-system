package com.cheng.game.controller;

import com.cheng.common.dto.CheckInRequest;
import com.cheng.common.dto.CheckInResult;
import com.cheng.common.dto.CheckInStatus;
import com.cheng.common.dto.CommonResponse;
import com.cheng.game.service.CheckInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 签到控制器
 */
@RestController
@RequestMapping("/api/check-in")
public class CheckInController {
    
    @Autowired
    private CheckInService checkInService;
    
    /**
     * 执行签到
     */
    @PostMapping
    public CommonResponse<CheckInResult> checkIn(@RequestBody CheckInRequest request) {
        try {
            if (request.getUserId() == null) {
                return CommonResponse.error("用户 ID 不能为空");
            }
            
            CheckInResult result = checkInService.checkIn(request.getUserId());
            return CommonResponse.success(result);
        } catch (Exception e) {
            return CommonResponse.error("签到失败：" + e.getMessage());
        }
    }
    
    /**
     * 查询签到状态
     */
    @GetMapping("/status")
    public CommonResponse<CheckInStatus> getCheckInStatus(@RequestParam Long userId) {
        try {
            CheckInStatus status = checkInService.getCheckInStatus(userId);
            return CommonResponse.success(status);
        } catch (Exception e) {
            return CommonResponse.error("查询失败：" + e.getMessage());
        }
    }
}
