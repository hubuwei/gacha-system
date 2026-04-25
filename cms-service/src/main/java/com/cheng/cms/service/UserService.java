package com.cheng.cms.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * User Service - 用户管理业务逻辑
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 获取用户列表（支持筛选和分页）
     */
    public Map<String, Object> getUsers(String username, Integer accountStatus, int page, int size) {
        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM users WHERE 1=1");
        StringBuilder querySql = new StringBuilder(
            "SELECT id, username, phone, email, avatar_url, nickname, " +
            "account_status, user_level, experience_points, last_login_time, created_at " +
            "FROM users WHERE 1=1"
        );

        List<Object> params = new ArrayList<>();

        // 用户名筛选
        if (username != null && !username.trim().isEmpty()) {
            countSql.append(" AND username LIKE ?");
            querySql.append(" AND username LIKE ?");
            params.add("%" + username + "%");
        }

        // 账号状态筛选
        if (accountStatus != null) {
            countSql.append(" AND account_status = ?");
            querySql.append(" AND account_status = ?");
            params.add(accountStatus);
        }

        // 查询总数
        Long total = jdbcTemplate.queryForObject(countSql.toString(), Long.class, params.toArray());

        // 分页查询
        int offset = (page - 1) * size;
        querySql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
        params.add(size);
        params.add(offset);

        List<Map<String, Object>> users = jdbcTemplate.queryForList(querySql.toString(), params.toArray());

        // 转换数据格式
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> user : users) {
            Map<String, Object> item = new HashMap<>();
            item.put("key", String.valueOf(user.get("id")));
            item.put("id", user.get("id"));
            item.put("username", user.get("username"));
            item.put("phone", user.get("phone"));
            item.put("email", user.get("email"));
            item.put("avatarUrl", user.get("avatar_url"));
            item.put("nickname", user.get("nickname"));
            item.put("accountStatus", user.get("account_status"));
            item.put("userLevel", user.get("user_level"));
            item.put("experiencePoints", user.get("experience_points"));
            item.put("lastLoginTime", user.get("last_login_time"));
            item.put("createdAt", user.get("created_at"));
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
     * 更新用户状态
     */
    public void updateUserStatus(Long id, Integer accountStatus) {
        String sql = "UPDATE users SET account_status = ?, updated_at = NOW() WHERE id = ?";
        jdbcTemplate.update(sql, accountStatus, id);
    }
}
