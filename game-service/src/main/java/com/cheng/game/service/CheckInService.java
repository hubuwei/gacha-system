package com.cheng.game.service;

import com.cheng.common.dto.CheckInResult;
import com.cheng.common.dto.CheckInStatus;
import org.springframework.stereotype.Service;

/**
 * 签到服务接口
 */
@Service
public interface CheckInService {
    
    /**
     * 执行签到
     * @param userId 用户 ID
     * @return 签到结果
     */
    CheckInResult checkIn(Long userId);
    
    /**
     * 查询签到状态
     * @param userId 用户 ID
     * @return 签到状态
     */
    CheckInStatus getCheckInStatus(Long userId);
}
