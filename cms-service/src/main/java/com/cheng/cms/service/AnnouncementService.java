package com.cheng.cms.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Announcement Service - 活动公告业务逻辑
 */
@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 获取公告列表（支持筛选和分页）
     */
    public Map<String, Object> getAnnouncements(String type, Boolean isActive, int page, int size) {
        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM announcements WHERE 1=1");
        StringBuilder querySql = new StringBuilder(
            "SELECT id, title, type, priority, image_url, is_active, " +
            "start_time, end_time, click_count, created_at " +
            "FROM announcements WHERE 1=1"
        );

        List<Object> params = new ArrayList<>();

        // 类型筛选
        if (type != null && !type.trim().isEmpty()) {
            countSql.append(" AND type = ?");
            querySql.append(" AND type = ?");
            params.add(type);
        }

        // 状态筛选
        if (isActive != null) {
            countSql.append(" AND is_active = ?");
            querySql.append(" AND is_active = ?");
            params.add(isActive ? 1 : 0);
        }

        // 查询总数
        Long total = jdbcTemplate.queryForObject(countSql.toString(), Long.class, params.toArray());

        // 分页查询
        int offset = (page - 1) * size;
        querySql.append(" ORDER BY priority DESC, created_at DESC LIMIT ? OFFSET ?");
        params.add(size);
        params.add(offset);

        List<Map<String, Object>> announcements = jdbcTemplate.queryForList(querySql.toString(), params.toArray());

        // 转换数据格式
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> announcement : announcements) {
            Map<String, Object> item = new HashMap<>();
            item.put("key", String.valueOf(announcement.get("id")));
            item.put("id", announcement.get("id"));
            item.put("title", announcement.get("title"));
            item.put("type", announcement.get("type"));
            item.put("priority", announcement.get("priority"));
            item.put("imageUrl", announcement.get("image_url"));
            item.put("isActive", announcement.get("is_active"));
            item.put("startTime", announcement.get("start_time"));
            item.put("endTime", announcement.get("end_time"));
            item.put("clickCount", announcement.get("click_count"));
            item.put("createdAt", announcement.get("created_at"));
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
     * 根据ID获取公告详情
     */
    public Map<String, Object> getAnnouncementById(Long id) {
        String sql = "SELECT * FROM announcements WHERE id = ?";
        List<Map<String, Object>> announcements = jdbcTemplate.queryForList(sql, id);

        if (announcements.isEmpty()) {
            return null;
        }

        Map<String, Object> announcement = announcements.get(0);
        Map<String, Object> result = new HashMap<>();
        result.put("id", announcement.get("id"));
        result.put("title", announcement.get("title"));
        result.put("content", announcement.get("content"));
        result.put("type", announcement.get("type"));
        result.put("priority", announcement.get("priority"));
        result.put("imageUrl", announcement.get("image_url"));
        result.put("targetType", announcement.get("target_type"));
        result.put("isActive", announcement.get("is_active"));
        result.put("startTime", announcement.get("start_time"));
        result.put("endTime", announcement.get("end_time"));
        result.put("clickCount", announcement.get("click_count"));
        result.put("createdBy", announcement.get("created_by"));
        result.put("createdAt", announcement.get("created_at"));
        result.put("updatedAt", announcement.get("updated_at"));

        return result;
    }

    /**
     * 创建公告
     */
    @Transactional
    public Map<String, Object> createAnnouncement(Map<String, Object> data) {
        String sql = "INSERT INTO announcements (title, content, type, priority, image_url, " +
            "target_type, is_active, start_time, end_time, created_by, created_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";

        String title = (String) data.get("title");
        String content = (String) data.get("content");
        String type = (String) data.getOrDefault("type", "info");
        Integer priority = ((Number) data.getOrDefault("priority", 0)).intValue();
        String imageUrl = (String) data.getOrDefault("imageUrl", "");
        String targetType = (String) data.getOrDefault("targetType", "all");
        Boolean isActive = (Boolean) data.getOrDefault("isActive", true);
        String startTime = (String) data.get("startTime");
        String endTime = (String) data.get("endTime");
        Long createdBy = 1L; // TODO: 从当前登录管理员获取

        jdbcTemplate.update(sql, title, content, type, priority, imageUrl,
            targetType, isActive ? 1 : 0, startTime, endTime, createdBy);

        return getAnnouncementById(getLastInsertId());
    }

    /**
     * 更新公告
     */
    @Transactional
    public Map<String, Object> updateAnnouncement(Long id, Map<String, Object> data) {
        // 先检查公告是否存在
        Map<String, Object> existing = getAnnouncementById(id);
        if (existing == null) {
            return null;
        }

        String sql = "UPDATE announcements SET title=?, content=?, type=?, priority=?, " +
            "image_url=?, target_type=?, is_active=?, start_time=?, end_time=?, updated_at=NOW() " +
            "WHERE id=?";

        String title = (String) data.get("title");
        String content = (String) data.get("content");
        String type = (String) data.get("type");
        Integer priority = ((Number) data.get("priority")).intValue();
        String imageUrl = (String) data.get("imageUrl");
        String targetType = (String) data.get("targetType");
        Boolean isActive = (Boolean) data.get("isActive");
        String startTime = (String) data.get("startTime");
        String endTime = (String) data.get("endTime");

        jdbcTemplate.update(sql, title, content, type, priority, imageUrl,
            targetType, isActive ? 1 : 0, startTime, endTime, id);

        return getAnnouncementById(id);
    }

    /**
     * 删除公告
     */
    @Transactional
    public void deleteAnnouncement(Long id) {
        String sql = "DELETE FROM announcements WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    /**
     * 获取最后插入的ID
     */
    private Long getLastInsertId() {
        return jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }
}
