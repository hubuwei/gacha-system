package com.cheng.mall.service;

import com.cheng.mall.dto.GameIntroRequest;
import com.cheng.mall.dto.ChatRequest;
import com.cheng.mall.dto.GameIntroResponse;
import com.cheng.mall.entity.Game;
import com.cheng.mall.repository.GameRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import com.fasterxml.jackson.core.JsonProcessingException;

@Slf4j
@Service
public class AIGameIntroService {

    @Value("${openai.api-key:}")
    private String apiKey;

    @Value("${openai.base-url:https://api.openai.com}")
    private String baseUrl;

    @Value("${openai.model:gpt-4.1-mini}")
    private String model;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final GameRepository gameRepository;

    public AIGameIntroService(GameRepository gameRepository) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.gameRepository = gameRepository;
    }

    public GameIntroResponse generateIntro(GameIntroRequest request) {
        String gameName = request.getGameName();
        String genre = request.getGenre();
        String style = request.getStyle();

        if (apiKey == null || apiKey.isBlank()) {
            log.warn("OpenAI API key not configured, returning mock data");
            return buildMockResponse(gameName, genre, style);
        }

        try {
            String prompt = buildPrompt(gameName, genre, style);
            String aiResponse = callOpenAI(prompt);
            return parseResponse(aiResponse, gameName);
        } catch (Exception e) {
            log.error("Failed to generate intro via OpenAI, falling back to mock", e);
            return buildMockResponse(gameName, genre, style);
        }
    }

    private String buildPrompt(String gameName, String genre, String style) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一个专业的游戏文案撰写人。请为以下游戏撰写一份引人入胜的游戏介绍。\n\n");
        sb.append("游戏名称：").append(gameName).append("\n");
        if (genre != null && !genre.isBlank()) {
            sb.append("游戏类型：").append(genre).append("\n");
        }
        if (style != null && !style.isBlank()) {
            sb.append("文案风格：").append(style).append("\n");
        }
        sb.append("\n请严格按照以下 JSON 格式返回（不要包含任何其他文字，只返回 JSON）：\n");
        sb.append("{\n");
        sb.append("  \"title\": \"游戏主标题\",\n");
        sb.append("  \"summary\": \"一段150字左右的游戏简介\",\n");
        sb.append("  \"background\": \"游戏背景故事，约200字\",\n");
        sb.append("  \"gameplay\": \"核心玩法介绍，约200字\",\n");
        sb.append("  \"highlights\": \"游戏特色亮点，约150字\",\n");
        sb.append("  \"recommendation\": \"向玩家推荐的理由，约100字\"\n");
        sb.append("}\n");
        sb.append("\n要求：文案精彩、有感染力，突出游戏特色。");
        return sb.toString();
    }

    private String callOpenAI(String prompt) {
        String url = baseUrl + "/v1/chat/completions";

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", List.of(message));
        body.put("temperature", 0.8);
        body.put("max_tokens", 2000);
        body.put("response_format", Map.of("type", "json_object"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        log.info("Calling OpenAI API: model={}", model);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        if (response.getBody() == null) {
            throw new RuntimeException("OpenAI returned empty response");
        }

        JsonNode root;
        try {
            root = objectMapper.readTree(response.getBody());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse OpenAI response: " + e.getMessage(), e);
        }
        return root.path("choices").get(0).path("message").path("content").asText();
    }

    private GameIntroResponse parseResponse(String jsonContent, String gameName) {
        try {
            GameIntroResponse response = objectMapper.readValue(jsonContent, GameIntroResponse.class);
            if (response.getTitle() == null || response.getTitle().isBlank()) {
                response.setTitle(gameName);
            }
            return response;
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("Failed to parse AI response JSON", e);
            throw new RuntimeException("AI response parse error: " + e.getMessage());
        }
    }


    /**
     * AI 客服聊天
     */
    /**
     * AI 客服聊天（支持打折查询注入真实数据）
     */
    public String chat(ChatRequest request) {
        if (apiKey == null || apiKey.isBlank()) {
            return "您好！我是游戏商城 AI 客服。当前 AI 服务未配置，请联系管理员接入。不过别担心，有什么关于游戏的问题都可以问我哦！😊";
        }
        try {
            String userMsg = request.getMessage();
            String discountContext = buildDiscountContext(userMsg);

            List<Map<String, Object>> messages = new ArrayList<>();

            // System prompt
            String systemPrompt = "你是游戏商城的 AI 客服助手。你的职责是：1) 解答用户关于游戏的疑问 2) 推荐适合用户的游戏 3) 帮助用户了解购买流程、退款政策等 4) 回答账号、订单、充值等问题。请用热情、专业的语气回复，回复控制在300字以内，适当使用emoji。如果用户问打折但你没有数据，建议访问商城首页折扣专区。";

            Map<String, Object> systemMsg = new LinkedHashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);
            messages.add(systemMsg);

            if (request.getHistory() != null) {
                for (Map<String, String> h : request.getHistory()) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("role", h.get("role"));
                    m.put("content", h.get("content"));
                    messages.add(m);
                }
            }

            // Append discount context to user message for stronger AI adherence
            String finalUserMsg = userMsg;
            if (discountContext != null && !discountContext.isEmpty()) {
                finalUserMsg = userMsg + discountContext;
            }

            Map<String, Object> userMsgObj = new LinkedHashMap<>();
            userMsgObj.put("role", "user");
            userMsgObj.put("content", finalUserMsg);
            messages.add(userMsgObj);

            return callChatAPI(messages);
        } catch (Exception e) {
            log.error("AI chat failed", e);
            return "抱歉，我暂时无法处理您的请求 😢 请稍后再试，或联系人工客服获取帮助。";
        }
    }

    /**
     * 检测打折关键字并查询数据库，构建上下文信息
     */
    private String buildDiscountContext(String message) {
        if (message == null) return null;

        String lower = message.toLowerCase();
        boolean isDiscountQuery = lower.contains("打折") || lower.contains("折扣")
                || lower.contains("特价") || lower.contains("优惠")
                || lower.contains("降价") || lower.contains("促销")
                || lower.contains("便宜") || lower.contains("discount")
                || lower.contains("sale") || lower.contains("划算")
                || lower.contains("活动") || lower.contains("推荐");

        log.info("buildDiscountContext: isDiscountQuery={}, message={}", isDiscountQuery, message);
        if (!isDiscountQuery) return null;

        try {
            // Try strict time-window queries first, fallback to all discounted
            List<Game> discounted = gameRepository.findCurrentDiscountedGames();
            if (discounted == null || discounted.isEmpty()) {
                discounted = gameRepository.findThisWeekDiscountedGames();
            }
            if (discounted == null || discounted.isEmpty()) {
                discounted = gameRepository.findAllDiscountedGames();
            }

            log.info("buildDiscountContext: found {} discounted games", discounted.size());
            if (discounted == null || discounted.isEmpty()) {
                return "\n\n【系统提示：数据库中暂无正在打折的游戏，请告知用户关注商城首页折扣专区】";
            }

            // Limit to top 10
            if (discounted.size() > 10) {
                discounted = discounted.subList(0, 10);
            }

            StringBuilder sb = new StringBuilder();
            sb.append("\n\n--- 以下是商城真实的打折游戏数据，请据此回答 ---\n");
            sb.append("共").append(discounted.size()).append("款打折游戏：\n");
            for (int i = 0; i < discounted.size(); i++) {
                Game g = discounted.get(i);
                sb.append(i + 1).append(". 【").append(g.getTitle()).append("】");
                sb.append(" 原价¥").append(g.getBasePrice());
                sb.append(" → 现价¥").append(g.getCurrentPrice());
                sb.append(" (").append(g.getDiscountRate()).append("%OFF)");
                if (g.getShortDescription() != null && !g.getShortDescription().isEmpty()) {
                    String desc = g.getShortDescription();
                    if (desc.length() > 60) desc = desc.substring(0, 60);
                    sb.append(" - ").append(desc);
                }
                sb.append("\n");
            }
            String result = sb.toString();
            log.info("buildDiscountContext: context built, length={}", result.length());
            return result;
        } catch (Exception e) {
            log.error("Failed to query discounted games", e);
            return null;
        }
    }

    private String callChatAPI(List<Map<String, Object>> messages) {
        String url = baseUrl + "/v1/chat/completions";
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("temperature", 0.7);
        body.put("max_tokens", 800);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        log.info("AI Chat: sending request");
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        if (response.getBody() == null) throw new RuntimeException("Empty response from AI");
        JsonNode root;
        try { root = objectMapper.readTree(response.getBody()); }
        catch (JsonProcessingException e) { throw new RuntimeException("Parse error: " + e.getMessage(), e); }
        return root.path("choices").get(0).path("message").path("content").asText();
    }
    private GameIntroResponse buildMockResponse(String gameName, String genre, String style) {
        GameIntroResponse resp = new GameIntroResponse();
        String g = gameName != null ? gameName : "未知游戏";
        String genreTag = (genre != null && !genre.isBlank()) ? genre : "动作冒险";
        String styleTag = (style != null && !style.isBlank()) ? style : "史诗宏大";

        resp.setTitle(g);
        resp.setSummary(g + "是一款令人叹为观止的" + genreTag + "类游戏，融合了" + styleTag + "的叙事风格与精致的画面表现。在这个精心打造的世界中，玩家将踏上一段难以忘怀的冒险旅程，探索未知的领域，揭开隐藏在深处的秘密，体验前所未有的沉浸式游戏乐趣。");
        resp.setBackground("在遥远的未来（或古老的传说时代），" + g + "的世界正面临着前所未有的危机。古老的预言逐渐应验，黑暗势力在暗处蠢蠢欲动。玩家将扮演一位被命运选中的英雄，从平凡的起点出发，在旅途中结识志同道合的伙伴，逐渐揭开世界的真相。每一个选择都将影响故事的走向，每一段旅程都蕴含着深刻的哲理与感动。");
        resp.setGameplay(g + "的核心玩法围绕" + genreTag + "展开，玩家可以在开放世界（或精心设计的关卡）中自由探索。战斗系统流畅而富有深度，结合了即时操作与策略搭配。此外还有丰富的养成系统、装备打造、技能树等RPG元素，以及多样的支线任务和隐藏内容等待玩家发掘。多人联机模式更增添了社交互动的乐趣。");
        resp.setHighlights("• 精致绝伦的画面表现与" + styleTag + "艺术风格\n• 引人入胜的原创剧情与世界观\n• 流畅爽快的战斗操作手感\n• 丰富的角色养成与自定义系统\n• 庞大的可探索区域与隐藏内容\n• 沉浸式音效与电影级配乐");
        resp.setRecommendation("如果你热爱" + genreTag + "类游戏，喜欢" + styleTag + "的叙事风格，那么" + g + "绝对值得一试。它不仅拥有出色的游戏性，更在故事深度和艺术表现上达到了新的高度。无论你是硬核玩家还是休闲党，都能在这款游戏中找到属于自己的乐趣。强烈推荐！");

        return resp;
    }
}
