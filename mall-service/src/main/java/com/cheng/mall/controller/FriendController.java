package com.cheng.mall.controller;

import com.cheng.common.dto.CommonResponse;
import com.cheng.mall.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 好友系统Controller
 */
@RestController
@RequestMapping("/api/friend")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    // ==================== 在线状态接口 ====================

    /**
     * 更新在线状态
     */
    @PostMapping("/status")
    public CommonResponse<Void> updateOnlineStatus(
            @RequestParam Long uid,
            @RequestParam Integer status,
            @RequestParam(required = false) Long gameId) {
        friendService.updateOnlineStatus(uid, status, gameId);
        return CommonResponse.success(null);
    }

    /**
     * 获取用户在线状态
     */
    @GetMapping("/status/{uid}")
    public CommonResponse<Map<String, Object>> getOnlineStatus(@PathVariable Long uid) {
        Map<String, Object> status = friendService.getOnlineStatus(uid);
        return CommonResponse.success(status);
    }

    /**
     * 批量获取好友在线状态
     */
    @PostMapping("/status/batch")
    public CommonResponse<List<Map<String, Object>>> getFriendsOnlineStatus(
            @RequestBody List<Long> friendUids) {
        List<Map<String, Object>> statuses = friendService.getFriendsOnlineStatus(friendUids);
        return CommonResponse.success(statuses);
    }

    // ==================== 好友申请接口 ====================

    /**
     * 发送好友申请
     */
    @PostMapping("/apply")
    public CommonResponse<Void> sendFriendApply(
            @RequestParam Long receiveUid,
            @RequestParam(required = false) String message) {
        // TODO: 从JWT Token中获取当前用户ID
        Long applyUid = 1L; // 临时硬编码，实际应从SecurityContext获取
        return friendService.sendFriendApply(applyUid, receiveUid, message);
    }

    /**
     * 获取收到的好友申请列表
     */
    @GetMapping("/apply/received")
    public CommonResponse<Page<Map<String, Object>>> getReceivedApplies(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        // TODO: 从JWT Token中获取当前用户ID
        Long receiveUid = 1L;
        Page<Map<String, Object>> applies = friendService.getReceivedApplies(receiveUid, page, size);
        return CommonResponse.success(applies);
    }

    /**
     * 同意好友申请
     */
    @PostMapping("/apply/{id}/accept")
    public CommonResponse<Void> acceptApply(@PathVariable Long id) {
        // TODO: 从JWT Token中获取当前用户ID
        Long currentUid = 1L;
        return friendService.acceptApply(id, currentUid);
    }

    /**
     * 拒绝好友申请
     */
    @PostMapping("/apply/{id}/reject")
    public CommonResponse<Void> rejectApply(@PathVariable Long id) {
        // TODO: 从JWT Token中获取当前用户ID
        Long currentUid = 1L;
        return friendService.rejectApply(id, currentUid);
    }

    // ==================== 好友列表接口 ====================

    /**
     * 获取好友列表
     */
    @GetMapping("/list")
    public CommonResponse<Page<Map<String, Object>>> getFriendList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        // TODO: 从JWT Token中获取当前用户ID
        Long uid = 1L;
        Page<Map<String, Object>> friends = friendService.getFriendList(uid, page, size);
        return CommonResponse.success(friends);
    }

    /**
     * 删除好友
     */
    @DeleteMapping("/{friendUid}")
    public CommonResponse<Void> deleteFriend(@PathVariable Long friendUid) {
        // TODO: 从JWT Token中获取当前用户ID
        Long uid = 1L;
        return friendService.deleteFriend(uid, friendUid);
    }

    /**
     * 设置好友备注
     */
    @PutMapping("/{friendUid}/remark")
    public CommonResponse<Void> setFriendRemark(
            @PathVariable Long friendUid,
            @RequestParam String remark) {
        // TODO: 从JWT Token中获取当前用户ID
        Long uid = 1L;
        return friendService.setFriendRemark(uid, friendUid, remark);
    }

    /**
     * 统计信息
     */
    @GetMapping("/stats")
    public CommonResponse<Map<String, Object>> getStats() {
        // TODO: 从 JWT Token中获取当前用户ID
        Long uid = 1L;
        
        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("friendCount", friendService.getFriendCount(uid));
        stats.put("pendingApplyCount", friendService.getPendingApplyCount(uid));
        
        return CommonResponse.success(stats);
    }
    
    // ==================== 好友分组接口 ====================
    
    /**
     * 设置好友分组
     */
    @PutMapping("/{friendUid}/group")
    public CommonResponse<Void> setFriendGroup(
            @PathVariable Long friendUid,
            @RequestParam String groupName) {
        // TODO: 从 JWT Token中获取当前用户ID
        Long uid = 1L;
        return friendService.setFriendGroup(uid, friendUid, groupName);
    }
    
    /**
     * 获取所有分组名称
     */
    @GetMapping("/groups")
    public CommonResponse<List<String>> getGroups() {
        // TODO: 从 JWT Token中获取当前用户ID
        Long uid = 1L;
        List<String> groups = friendService.getGroups(uid);
        return CommonResponse.success(groups);
    }
    
    /**
     * 按分组获取好友列表
     */
    @GetMapping("/group/{groupName}")
    public CommonResponse<Page<Map<String, Object>>> getFriendsByGroup(
            @PathVariable String groupName,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        // TODO: 从 JWT Token中获取当前用户ID
        Long uid = 1L;
        Page<Map<String, Object>> friends = friendService.getFriendsByGroup(uid, groupName, page, size);
        return CommonResponse.success(friends);
    }
    
    // ==================== 黑名单接口 ====================
    
    /**
     * 拉黑用户
     */
    @PostMapping("/blacklist")
    public CommonResponse<Void> blockUser(@RequestParam Long blockedUid) {
        // TODO: 从 JWT Token中获取当前用户ID
        Long uid = 1L;
        return friendService.blockUser(uid, blockedUid);
    }
    
    /**
     * 取消拉黑
     */
    @DeleteMapping("/blacklist/{blockedUid}")
    public CommonResponse<Void> unblockUser(@PathVariable Long blockedUid) {
        // TODO: 从 JWT Token中获取当前用户ID
        Long uid = 1L;
        return friendService.unblockUser(uid, blockedUid);
    }
    
    /**
     * 获取黑名单列表
     */
    @GetMapping("/blacklist")
    public CommonResponse<List<Map<String, Object>>> getBlacklist() {
        // TODO: 从 JWT Token中获取当前用户ID
        Long uid = 1L;
        List<Map<String, Object>> blacklist = friendService.getBlacklist(uid);
        return CommonResponse.success(blacklist);
    }
}
