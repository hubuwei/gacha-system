package com.cheng.cms.controller;

import com.cheng.common.dto.CommonResponse;
import com.cheng.cms.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Announcement Controller - 活动公告接口
 */
@RestController
@RequestMapping("/api/cms/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    /**
     * 获取公告列表
     */
    @GetMapping
    public CommonResponse<List<Map<String, Object>>> getAnnouncements(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Map<String, Object> result = announcementService.getAnnouncements(type, isActive, page, size);
            return CommonResponse.success((List<Map<String, Object>>) result.get("list"));
        } catch (Exception e) {
            return CommonResponse.error("获取公告列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取公告详情
     */
    @GetMapping("/{id}")
    public CommonResponse<Map<String, Object>> getAnnouncementById(@PathVariable Long id) {
        try {
            Map<String, Object> announcement = announcementService.getAnnouncementById(id);
            if (announcement == null) {
                return CommonResponse.error("公告不存在");
            }
            return CommonResponse.success(announcement);
        } catch (Exception e) {
            return CommonResponse.error("获取公告详情失败: " + e.getMessage());
        }
    }

    /**
     * 新增公告
     */
    @PostMapping
    public CommonResponse<Map<String, Object>> createAnnouncement(@RequestBody Map<String, Object> data) {
        try {
            Map<String, Object> announcement = announcementService.createAnnouncement(data);
            return CommonResponse.success(announcement);
        } catch (Exception e) {
            return CommonResponse.error("创建公告失败: " + e.getMessage());
        }
    }

    /**
     * 更新公告
     */
    @PutMapping("/{id}")
    public CommonResponse<Map<String, Object>> updateAnnouncement(
            @PathVariable Long id,
            @RequestBody Map<String, Object> data) {
        try {
            Map<String, Object> announcement = announcementService.updateAnnouncement(id, data);
            if (announcement == null) {
                return CommonResponse.error("公告不存在");
            }
            return CommonResponse.success(announcement);
        } catch (Exception e) {
            return CommonResponse.error("更新公告失败: " + e.getMessage());
        }
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/{id}")
    public CommonResponse<Void> deleteAnnouncement(@PathVariable Long id) {
        try {
            announcementService.deleteAnnouncement(id);
            return CommonResponse.success(null);
        } catch (Exception e) {
            return CommonResponse.error("删除公告失败: " + e.getMessage());
        }
    }
}
