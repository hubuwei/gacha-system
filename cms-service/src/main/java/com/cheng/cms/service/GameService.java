package com.cheng.cms.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * Game Service - 游戏管理业务逻辑
 */
@Service
@RequiredArgsConstructor
public class GameService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 获取游戏列表（支持分页和搜索）
     */
    public Map<String, Object> getGames(String keyword, int page, int size) {
        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM games WHERE 1=1");
        StringBuilder querySql = new StringBuilder(
            "SELECT id, title, base_price, current_price, is_on_sale, rating, total_sales, " +
            "cover_image, short_description, created_at " +
            "FROM games WHERE 1=1"
        );

        List<Object> params = new ArrayList<>();

        // 关键词搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            countSql.append(" AND title LIKE ?");
            querySql.append(" AND title LIKE ?");
            params.add("%" + keyword + "%");
        }

        // 查询总数
        Long total = jdbcTemplate.queryForObject(countSql.toString(), Long.class, params.toArray());

        // 分页查询
        int offset = (page - 1) * size;
        querySql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
        params.add(size);
        params.add(offset);

        List<Map<String, Object>> games = jdbcTemplate.queryForList(querySql.toString(), params.toArray());

        // 转换数据格式
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> game : games) {
            Map<String, Object> item = new HashMap<>();
            item.put("key", String.valueOf(game.get("id")));
            item.put("id", game.get("id"));
            item.put("title", game.get("title"));
            item.put("basePrice", ((BigDecimal) game.get("base_price")).doubleValue());
            item.put("currentPrice", ((BigDecimal) game.get("current_price")).doubleValue());
            item.put("isOnSale", game.get("is_on_sale"));
            item.put("rating", game.get("rating"));
            item.put("totalSales", game.get("total_sales"));
            item.put("coverImage", game.get("cover_image"));
            item.put("description", game.get("short_description"));
            item.put("createdAt", game.get("created_at"));
            result.add(item);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("list", result);
        response.put("total", total);
        response.put("page", page);
        response.put("size", size);

        return response;
    }

    /**
     * 根据ID获取游戏详情
     */
    public Map<String, Object> getGameById(Long id) {
        String sql = "SELECT * FROM games WHERE id = ?";
        List<Map<String, Object>> games = jdbcTemplate.queryForList(sql, id);

        if (games.isEmpty()) {
            return null;
        }

        Map<String, Object> game = games.get(0);
        Map<String, Object> result = new HashMap<>();
        result.put("id", game.get("id"));
        result.put("title", game.get("title"));
        result.put("basePrice", ((BigDecimal) game.get("base_price")).doubleValue());
        result.put("currentPrice", ((BigDecimal) game.get("current_price")).doubleValue());
        result.put("isOnSale", game.get("is_on_sale"));
        result.put("rating", game.get("rating"));
        result.put("totalSales", game.get("total_sales"));
        result.put("coverImage", game.get("cover_image"));
        result.put("description", game.get("short_description"));

        return result;
    }

    /**
     * 创建游戏
     */
    @Transactional
    public Map<String, Object> createGame(Map<String, Object> gameData) {
        String sql = "INSERT INTO games (title, base_price, current_price, is_on_sale, rating, " +
            "total_sales, cover_image, short_description, created_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";

        String title = (String) gameData.get("title");
        Double basePrice = ((Number) gameData.get("basePrice")).doubleValue();
        Double currentPrice = ((Number) gameData.get("currentPrice")).doubleValue();
        Boolean isOnSale = (Boolean) gameData.getOrDefault("isOnSale", false);
        Double rating = ((Number) gameData.getOrDefault("rating", 0.0)).doubleValue();
        Integer totalSales = ((Number) gameData.getOrDefault("totalSales", 0)).intValue();
        String coverImage = (String) gameData.getOrDefault("coverImage", "");
        String description = (String) gameData.getOrDefault("description", "");

        jdbcTemplate.update(sql, title, basePrice, currentPrice, isOnSale, rating,
            totalSales, coverImage, description);

        // 返回新创建的游戏
        return getGameById(getLastInsertId());
    }

    /**
     * 更新游戏
     */
    @Transactional
    public Map<String, Object> updateGame(Long id, Map<String, Object> gameData) {
        // 先检查游戏是否存在
        Map<String, Object> existingGame = getGameById(id);
        if (existingGame == null) {
            return null;
        }

        String sql = "UPDATE games SET title=?, base_price=?, current_price=?, is_on_sale=?, " +
            "rating=?, total_sales=?, cover_image=?, short_description=?, updated_at=NOW() " +
            "WHERE id=?";

        String title = (String) gameData.get("title");
        Double basePrice = ((Number) gameData.get("basePrice")).doubleValue();
        Double currentPrice = ((Number) gameData.get("currentPrice")).doubleValue();
        Boolean isOnSale = (Boolean) gameData.get("isOnSale");
        Double rating = ((Number) gameData.get("rating")).doubleValue();
        Integer totalSales = ((Number) gameData.get("totalSales")).intValue();
        String coverImage = (String) gameData.get("coverImage");
        String description = (String) gameData.get("description");

        jdbcTemplate.update(sql, title, basePrice, currentPrice, isOnSale, rating,
            totalSales, coverImage, description, id);

        return getGameById(id);
    }

    /**
     * 删除游戏
     */
    @Transactional
    public void deleteGame(Long id) {
        String sql = "DELETE FROM games WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    /**
     * 更新游戏状态（上下架）- 使用is_featured代替
     */
    @Transactional
    public void updateGameStatus(Long id, Boolean isFeatured) {
        String sql = "UPDATE games SET is_featured = ?, updated_at = NOW() WHERE id = ?";
        jdbcTemplate.update(sql, isFeatured, id);
    }

    /**
     * 获取最后插入的ID
     */
    private Long getLastInsertId() {
        return jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }
}
