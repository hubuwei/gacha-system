package com.cheng.mall.controller;

import com.cheng.common.dto.CommonResponse;
import com.cheng.mall.dto.ChatRequest;
import com.cheng.mall.dto.GameIntroRequest;
import com.cheng.mall.dto.GameIntroResponse;
import com.cheng.mall.service.AIGameIntroService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/ai")
@CrossOrigin(origins = "*")
public class AIGameIntroController {

    @Autowired
    private AIGameIntroService aiGameIntroService;

    @PostMapping("/generate-game-intro")
    public CommonResponse<GameIntroResponse> generateGameIntro(@RequestBody GameIntroRequest request) {
        try {
            if (request.getGameName() == null || request.getGameName().isBlank()) {
                return CommonResponse.error("游戏名称不能为空");
            }
            log.info("Generating game intro for: {}", request.getGameName());
            GameIntroResponse result = aiGameIntroService.generateIntro(request);
            return CommonResponse.success(result);
        } catch (Exception e) {
            log.error("Failed to generate game intro", e);
            return CommonResponse.error("生成失败: " + e.getMessage());
        }
    }

    @PostMapping("/chat")
    public CommonResponse<Map<String, String>> chat(@RequestBody ChatRequest request) {
        try {
            if (request.getMessage() == null || request.getMessage().isBlank()) {
                return CommonResponse.error("消息不能为空");
            }
            log.info("AI Chat: {}", request.getMessage());
            String reply = aiGameIntroService.chat(request);
            Map<String, String> result = new HashMap<>();
            result.put("reply", reply);
            return CommonResponse.success(result);
        } catch (Exception e) {
            log.error("Chat failed", e);
            return CommonResponse.error("聊天失败: " + e.getMessage());
        }
    }
}