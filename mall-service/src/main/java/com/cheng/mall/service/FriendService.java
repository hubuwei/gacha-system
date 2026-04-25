package com.cheng.mall.service;

import com.cheng.common.dto.CommonResponse;
import com.cheng.mall.entity.*;
import com.cheng.mall.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 好友系统Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FriendService {

    private final UserFriendRepository friendRepository;
    private final FriendApplyRepository applyRepository;
    private final UserOnlineStatusRepository onlineStatusRepository;
    private final NotificationService notificationService;
    private final FriendBlacklistRepository blacklistRepository;
    private final OrderRepository orderRepository;
    private final GameReviewRepository reviewRepository;
    private final FriendActivityRepository activityRepository;
    private final FriendInvitationRepository invitationRepository;

    // 缓存已禁用，直接使用数据库查询
    private static final boolean CACHE_ENABLED = false;

    // ==================== 在线状态管理 ====================

    /**
     * 更新用户在线状态
     */
    @Transactional
    public void updateOnlineStatus(Long uid, Integer status, Long gameId) {
        UserOnlineStatus onlineStatus = onlineStatusRepository.findByUid(uid)
            .orElse(new UserOnlineStatus());
        
        onlineStatus.setUid(uid);
        onlineStatus.setStatus(status);
        onlineStatus.setGameId(gameId);
        onlineStatus.setUpdateTime(LocalDateTime.now());
        
        onlineStatusRepository.save(onlineStatus);
        
        log.info("用户 {} 在线状态更新为: {}", uid, status);
    }

    /**
     * 获取用户在线状态（优先从Redis读取）
     */
    public Map<String, Object> getOnlineStatus(Long uid) {
        // 从数据库读取
        UserOnlineStatus status = onlineStatusRepository.findByUid(uid)
            .orElse(null);
        
        Map<String, Object> result = new HashMap<>();
        if (status != null) {
            result.put("uid", status.getUid());
            result.put("status", status.getStatus());
            result.put("gameId", status.getGameId());
            result.put("updateTime", status.getUpdateTime());
        } else {
            result.put("uid", uid);
            result.put("status", 0); // 默认离线
            result.put("gameId", null);
            result.put("updateTime", null);
        }
        
        return result;
    }

    /**
     * 批量获取好友在线状态
     */
    public List<Map<String, Object>> getFriendsOnlineStatus(List<Long> friendUids) {
        return friendUids.stream()
            .map(this::getOnlineStatus)
            .collect(Collectors.toList());
    }

    // ==================== 好友申请管理 ====================

    /**
     * 发送好友申请
     */
    @Transactional
    public CommonResponse<Void> sendFriendApply(Long applyUid, Long receiveUid, String message) {
        // 不能添加自己为好友
        if (applyUid.equals(receiveUid)) {
            return CommonResponse.error("不能添加自己为好友");
        }

        // 检查是否已经是好友
        Optional<UserFriend> existingFriend = friendRepository
            .findByUidAndFriendUid(applyUid, receiveUid);
        if (existingFriend.isPresent() && existingFriend.get().getStatus() == 1) {
            return CommonResponse.error("你们已经是好友了");
        }

        // 检查是否已有待处理的申请
        Optional<FriendApply> existingApply = applyRepository
            .findByApplyUidAndReceiveUidAndStatus(applyUid, receiveUid, 0);
        if (existingApply.isPresent()) {
            return CommonResponse.error("已发送过好友申请，请等待对方同意");
        }

        // 创建申请记录
        FriendApply apply = new FriendApply();
        apply.setApplyUid(applyUid);
        apply.setReceiveUid(receiveUid);
        apply.setMessage(message);
        apply.setStatus(0);
        
        applyRepository.save(apply);
        
        // 通过WebSocket发送通知给接收人
        notificationService.sendFriendRequestNotification(receiveUid, applyUid, message);
        
        log.info("用户 {} 向用户 {} 发送好友申请", applyUid, receiveUid);
        return CommonResponse.success(null);
    }

    /**
     * 获取收到的好友申请列表
     */
    public Page<Map<String, Object>> getReceivedApplies(Long receiveUid, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<FriendApply> applies = applyRepository
            .findByReceiveUidAndStatus(receiveUid, 0, pageable);
        
        return applies.map(apply -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", apply.getId());
            map.put("applyUid", apply.getApplyUid());
            map.put("message", apply.getMessage());
            map.put("createTime", apply.getCreateTime());
            // TODO: 调用auth-service API获取用户信息
            map.put("username", "用户" + apply.getApplyUid());
            map.put("avatar", null);
            return map;
        });
    }

    /**
     * 同意好友申请
     */
    @Transactional
    public CommonResponse<Void> acceptApply(Long applyId, Long currentUid) {
        FriendApply apply = applyRepository.findById(applyId)
            .orElseThrow(() -> new RuntimeException("申请不存在"));
        
        // 权限检查
        if (!apply.getReceiveUid().equals(currentUid)) {
            return CommonResponse.error("无权操作此申请");
        }
        
        // 更新申请状态
        apply.setStatus(1);
        applyRepository.save(apply);
        
        // 创建双向好友关系
        createFriendRelation(apply.getApplyUid(), apply.getReceiveUid());
        createFriendRelation(apply.getReceiveUid(), apply.getApplyUid());
        
        // 发送通知给申请人
        notificationService.sendFriendAcceptedNotification(apply.getApplyUid(), currentUid);
        
        // 记录动态
        recordActivity(apply.getApplyUid(), "FRIEND_ADDED", 
            "成为了好友", null, null);
        recordActivity(currentUid, "FRIEND_ADDED", 
            "成为了好友", null, null);
        
        log.info("用户 {} 同意了用户 {} 的好友申请", currentUid, apply.getApplyUid());
        return CommonResponse.success(null);
    }

    /**
     * 拒绝好友申请
     */
    @Transactional
    public CommonResponse<Void> rejectApply(Long applyId, Long currentUid) {
        FriendApply apply = applyRepository.findById(applyId)
            .orElseThrow(() -> new RuntimeException("申请不存在"));
        
        if (!apply.getReceiveUid().equals(currentUid)) {
            return CommonResponse.error("无权操作此申请");
        }
        
        apply.setStatus(2);
        applyRepository.save(apply);
        
        log.info("用户 {} 拒绝了用户 {} 的好友申请", currentUid, apply.getApplyUid());
        return CommonResponse.success(null);
    }

    // ==================== 好友列表管理 ====================

    /**
     * 获取好友列表（带分页和缓存）
     */
    public Page<Map<String, Object>> getFriendList(Long uid, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<UserFriend> friends = friendRepository
            .findByUidAndStatus(uid, 1, pageable);
        
        Page<Map<String, Object>> result = friends.map(friend -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", friend.getId());
            map.put("friendUid", friend.getFriendUid());
            map.put("remark", friend.getRemark());
            map.put("createTime", friend.getCreateTime());
            
            // TODO: 调用auth-service API获取用户信息
            map.put("username", "用户" + friend.getFriendUid());
            map.put("nickname", null);
            map.put("avatar", null);
            
            // 获取在线状态
            Map<String, Object> onlineStatus = getOnlineStatus(friend.getFriendUid());
            map.put("onlineStatus", onlineStatus.get("status"));
            map.put("gameId", onlineStatus.get("gameId"));
            
            return map;
        });
        
        return result;
    }

    /**
     * 删除好友
     */
    @Transactional
    public CommonResponse<Void> deleteFriend(Long uid, Long friendUid) {
        // 删除双向关系
        Optional<UserFriend> relation1 = friendRepository.findByUidAndFriendUid(uid, friendUid);
        relation1.ifPresent(friendRepository::delete);
        
        Optional<UserFriend> relation2 = friendRepository.findByUidAndFriendUid(friendUid, uid);
        relation2.ifPresent(friendRepository::delete);
        
        log.info("用户 {} 删除了好友 {}", uid, friendUid);
        return CommonResponse.success(null);
    }

    /**
     * 设置好友备注
     */
    @Transactional
    public CommonResponse<Void> setFriendRemark(Long uid, Long friendUid, String remark) {
        Optional<UserFriend> relation = friendRepository.findByUidAndFriendUid(uid, friendUid);
        if (relation.isEmpty()) {
            return CommonResponse.error("好友关系不存在");
        }
        
        UserFriend friend = relation.get();
        friend.setRemark(remark);
        friendRepository.save(friend);
        
        return CommonResponse.success(null);
    }

    /**
     * 统计好友数量
     */
    public long getFriendCount(Long uid) {
        return friendRepository.countByUidAndStatus(uid, 1);
    }

    /**
     * 统计待处理申请数量
     */
    public long getPendingApplyCount(Long uid) {
        return applyRepository.countByReceiveUidAndStatus(uid, 0);
    }

    // ==================== 私有方法 ====================

    /**
     * 创建好友关系
     */
    private void createFriendRelation(Long uid, Long friendUid) {
        Optional<UserFriend> existing = friendRepository.findByUidAndFriendUid(uid, friendUid);
        if (existing.isEmpty()) {
            UserFriend friend = new UserFriend();
            friend.setUid(uid);
            friend.setFriendUid(friendUid);
            friend.setStatus(1);
            friend.setGroupName("My Friends");
            friendRepository.save(friend);
        }
    }
    
    // ==================== 好友分组管理 ====================
    
    /**
     * 设置好友分组
     */
    @Transactional
    public CommonResponse<Void> setFriendGroup(Long uid, Long friendUid, String groupName) {
        // 检查是否在黑名单中
        if (isBlocked(uid, friendUid)) {
            return CommonResponse.error("该用户在您的黑名单中");
        }
        
        Optional<UserFriend> friend = friendRepository.findByUidAndFriendUid(uid, friendUid);
        if (friend.isEmpty() || friend.get().getStatus() != 1) {
            return CommonResponse.error("好友不存在");
        }
        
        friend.get().setGroupName(groupName);
        friendRepository.save(friend.get());
        
        log.info("用户 {} 将好友 {} 移动到分组: {}", uid, friendUid, groupName);
        return CommonResponse.success(null);
    }
    
    /**
     * 获取所有分组名称
     */
    public List<String> getGroups(Long uid) {
        List<UserFriend> friends = friendRepository.findByUidAndStatus(uid, 1);
        return friends.stream()
            .map(UserFriend::getGroupName)
            .distinct()
            .collect(Collectors.toList());
    }
    
    /**
     * 按分组获取好友列表
     */
    public Page<Map<String, Object>> getFriendsByGroup(Long uid, String groupName, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<UserFriend> friends = friendRepository.findByUidAndStatusAndGroupName(uid, 1, groupName, pageable);
        
        return friends.map(friend -> {
            Map<String, Object> map = new HashMap<>();
            map.put("friendUid", friend.getFriendUid());
            map.put("remark", friend.getRemark());
            map.put("groupName", friend.getGroupName());
            map.put("createTime", friend.getCreateTime());
            // TODO: 调用auth-service API获取用户信息
            map.put("username", "用户" + friend.getFriendUid());
            map.put("avatar", null);
            return map;
        });
    }
    
    // ==================== 黑名单管理 ====================
    
    /**
     * 拉黑用户
     */
    @Transactional
    public CommonResponse<Void> blockUser(Long uid, Long blockedUid) {
        if (uid.equals(blockedUid)) {
            return CommonResponse.error("不能拉黑自己");
        }
        
        // 检查是否已拉黑
        Optional<FriendBlacklist> existing = blacklistRepository.findByUidAndBlockedUid(uid, blockedUid);
        if (existing.isPresent()) {
            return CommonResponse.error("该用户已在黑名单中");
        }
        
        // 添加到黑名单
        FriendBlacklist blacklist = new FriendBlacklist();
        blacklist.setUid(uid);
        blacklist.setBlockedUid(blockedUid);
        blacklistRepository.save(blacklist);
        
        // 删除好友关系（如果存在）
        friendRepository.findByUidAndFriendUid(uid, blockedUid)
            .ifPresent(friendRepository::delete);
        friendRepository.findByUidAndFriendUid(blockedUid, uid)
            .ifPresent(friendRepository::delete);
        
        log.info("用户 {} 拉黑了用户 {}", uid, blockedUid);
        return CommonResponse.success(null);
    }
    
    /**
     * 取消拉黑
     */
    @Transactional
    public CommonResponse<Void> unblockUser(Long uid, Long blockedUid) {
        Optional<FriendBlacklist> blacklist = blacklistRepository.findByUidAndBlockedUid(uid, blockedUid);
        if (blacklist.isEmpty()) {
            return CommonResponse.error("该用户不在黑名单中");
        }
        
        blacklistRepository.delete(blacklist.get());
        log.info("用户 {} 取消拉黑用户 {}", uid, blockedUid);
        return CommonResponse.success(null);
    }
    
    /**
     * 获取黑名单列表
     */
    public List<Map<String, Object>> getBlacklist(Long uid) {
        List<FriendBlacklist> blacklists = blacklistRepository.findByUidOrderByCreateTimeDesc(uid);
        
        return blacklists.stream().map(blacklist -> {
            Map<String, Object> map = new HashMap<>();
            map.put("blockedUid", blacklist.getBlockedUid());
            map.put("createTime", blacklist.getCreateTime());
            // TODO: 调用auth-service API获取用户信息
            map.put("username", "用户" + blacklist.getBlockedUid());
            return map;
        }).collect(Collectors.toList());
    }
    
    /**
     * 检查是否已拉黑某用户
     */
    private boolean isBlocked(Long uid, Long targetUid) {
        return blacklistRepository.findByUidAndBlockedUid(uid, targetUid).isPresent();
    }
    
    // ==================== 好友资料查看 ====================
    
    /**
     * 获取好友的游戏库（已购买游戏）
     */
    public Page<Map<String, Object>> getFriendGames(Long friendUid, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
            
        // 从订单表查询该用户已购买的订单
        Page<Order> orders = orderRepository.findByUserId(friendUid, pageable);
            
        return orders.map(order -> {
            Map<String, Object> map = new HashMap<>();
            map.put("orderId", order.getId());
            map.put("orderNo", order.getOrderNo());
            map.put("purchaseTime", order.getCreatedAt());
            map.put("totalAmount", order.getTotalAmount());
            map.put("actualAmount", order.getActualAmount());
            map.put("paymentStatus", order.getPaymentStatus());
            map.put("orderStatus", order.getOrderStatus());
            // TODO: 通过 OrderItem 查询具体游戏，调用 game-service API获取游戏详情
            map.put("games", "待实现：需关联OrderItem表");
            return map;
        });
    }
    
    /**
     * 获取好友的最近游玩记录
     */
    public List<Map<String, Object>> getFriendRecentGames(Long friendUid, int limit) {
        // 从user_online_status表查询最近玩过的游戏
        // 注意：这里需要历史记录，当前表只保留最新状态
        // 简化实现：返回当前正在玩的游戏
        UserOnlineStatus status = onlineStatusRepository.findByUid(friendUid).orElse(null);
        
        List<Map<String, Object>> result = new ArrayList<>();
        if (status != null && status.getGameId() != null && status.getStatus() == 3) {
            Map<String, Object> game = new HashMap<>();
            game.put("gameId", status.getGameId());
            game.put("status", "游戏中");
            game.put("updateTime", status.getUpdateTime());
            // TODO: 调用game-service API获取游戏详情
            game.put("gameTitle", "游戏" + status.getGameId());
            result.add(game);
        }
        
        return result;
    }
    
    /**
     * 获取好友的游戏评测
     */
    public Page<Map<String, Object>> getFriendReviews(Long friendUid, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
            
        // 查询该用户发布的评测
        Page<GameReview> reviews = reviewRepository.findByUserIdOrderByCreatedAtDesc(friendUid, pageable);
            
        return reviews.map(review -> {
            Map<String, Object> map = new HashMap<>();
            map.put("reviewId", review.getId());
            map.put("gameId", review.getGameId());
            map.put("rating", review.getRating());
            map.put("title", review.getTitle());
            map.put("content", review.getContent());
            map.put("pros", review.getPros());
            map.put("cons", review.getCons());
            map.put("playHours", review.getPlayHours());
            map.put("helpfulCount", review.getHelpfulCount());
            map.put("createTime", review.getCreatedAt());
            map.put("isVerifiedPurchase", review.getIsVerifiedPurchase());
            // TODO: 调用 game-service API获取游戏详情
            map.put("gameTitle", "游戏" + review.getGameId());
            return map;
        });
    }
    
    // ==================== 好友动态系统 ====================
    
    /**
     * Record activity
     */
    @Transactional
    public void recordActivity(Long uid, String type, String content, Long gameId, String metadata) {
        FriendActivity activity = new FriendActivity();
        activity.setUid(uid);
        activity.setType(type);
        activity.setContent(content);
        activity.setGameId(gameId);
        activity.setMetadata(metadata);
        activityRepository.save(activity);
        log.info("记录用户 {} 的动态: {}", uid, type);
    }
    
    /**
     * Get friend activities feed
     */
    public Page<Map<String, Object>> getFriendActivities(Long uid, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        
        // Get current user's friends
        List<UserFriend> friends = friendRepository.findByUidAndStatus(uid, 1);
        if (friends.isEmpty()) {
            return Page.empty(pageable);
        }
        
        List<Long> friendUids = friends.stream()
            .map(UserFriend::getFriendUid)
            .collect(Collectors.toList());
        
        // Get activities from all friends
        // Note: JPA doesn't support IN with Page directly, so we fetch and paginate manually
        List<FriendActivity> allActivities = new ArrayList<>();
        for (Long friendUid : friendUids) {
            List<FriendActivity> activities = activityRepository.findByUidOrderByCreatedAtDesc(
                friendUid, Pageable.unpaged()).getContent();
            allActivities.addAll(activities);
        }
        
        // Sort by createdAt desc
        allActivities.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        
        // Manual pagination
        int start = (page - 1) * size;
        int end = Math.min(start + size, allActivities.size());
        List<FriendActivity> pagedActivities = start < allActivities.size() 
            ? allActivities.subList(start, end) 
            : Collections.emptyList();
        
        long total = allActivities.size();
        
        return new org.springframework.data.domain.PageImpl<>(
            pagedActivities.stream().map(activity -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", activity.getId());
                map.put("uid", activity.getUid());
                map.put("type", activity.getType());
                map.put("content", activity.getContent());
                map.put("gameId", activity.getGameId());
                map.put("metadata", activity.getMetadata());
                map.put("createdAt", activity.getCreatedAt());
                // TODO: Call auth-service API to get username and avatar
                map.put("username", "用户" + activity.getUid());
                map.put("avatar", null);
                // TODO: Call game-service API to get game title
                if (activity.getGameId() != null) {
                    map.put("gameTitle", "游戏" + activity.getGameId());
                }
                return map;
            }).collect(Collectors.toList()),
            pageable,
            total
        );
    }
    
    /**
     * Manually publish activity
     */
    @Transactional
    public CommonResponse<Void> publishActivity(Long uid, String content, Long gameId) {
        recordActivity(uid, "CUSTOM", content, gameId, null);
        return CommonResponse.success(null);
    }
    
    // ==================== 好友邀请系统 ====================
    
    /**
     * Send game invitation to friend
     */
    @Transactional
    public CommonResponse<Void> sendInvitation(Long inviterUid, Long inviteeUid, Long gameId, String message) {
        // Check if they are friends
        Optional<UserFriend> friendship = friendRepository.findByUidAndFriendUid(inviterUid, inviteeUid);
        if (friendship.isEmpty() || friendship.get().getStatus() != 1) {
            return CommonResponse.error("只能邀请好友");
        }
        
        // Check if already has pending invitation
        Optional<FriendInvitation> existing = invitationRepository
            .findByInviterUidAndInviteeUidAndGameIdAndStatus(inviterUid, inviteeUid, gameId, 0);
        if (existing.isPresent()) {
            return CommonResponse.error("已有待处理的邀请");
        }
        
        // Create invitation
        FriendInvitation invitation = new FriendInvitation();
        invitation.setInviterUid(inviterUid);
        invitation.setInviteeUid(inviteeUid);
        invitation.setGameId(gameId);
        invitation.setMessage(message);
        invitation.setStatus(0); // pending
        invitation.setExpiresAt(LocalDateTime.now().plusHours(24)); // expires in 24 hours
        
        invitationRepository.save(invitation);
        
        // Send WebSocket notification
        notificationService.sendGameInvitationNotification(inviteeUid, inviterUid, gameId, message);
        
        log.info("用户 {} 向用户 {} 发送游戏邀请: 游戏ID={}", inviterUid, inviteeUid, gameId);
        return CommonResponse.success(null);
    }
    
    /**
     * Accept invitation
     */
    @Transactional
    public CommonResponse<Map<String, Object>> acceptInvitation(Long invitationId, Long currentUid) {
        FriendInvitation invitation = invitationRepository.findById(invitationId)
            .orElseThrow(() -> new RuntimeException("邀请不存在"));
        
        if (!invitation.getInviteeUid().equals(currentUid)) {
            return CommonResponse.error("无权操作此邀请");
        }
        
        if (invitation.getStatus() != 0) {
            return CommonResponse.error("邀请已处理");
        }
        
        // Update status
        invitation.setStatus(1); // accepted
        invitation.setRespondedAt(LocalDateTime.now());
        invitationRepository.save(invitation);
        
        // Generate virtual room code for simulation
        String roomCode = "ROOM-" + System.currentTimeMillis() % 100000;
        invitation.setRoomCode(roomCode);
        invitationRepository.save(invitation);
        
        Map<String, Object> result = new HashMap<>();
        result.put("invitationId", invitation.getId());
        result.put("gameId", invitation.getGameId());
        result.put("roomCode", roomCode);
        result.put("inviterUid", invitation.getInviterUid());
        
        log.info("用户 {} 接受了游戏邀请: 房间码={}", currentUid, roomCode);
        return CommonResponse.success(result);
    }
    
    /**
     * Reject invitation
     */
    @Transactional
    public CommonResponse<Void> rejectInvitation(Long invitationId, Long currentUid) {
        FriendInvitation invitation = invitationRepository.findById(invitationId)
            .orElseThrow(() -> new RuntimeException("邀请不存在"));
        
        if (!invitation.getInviteeUid().equals(currentUid)) {
            return CommonResponse.error("无权操作此邀请");
        }
        
        if (invitation.getStatus() != 0) {
            return CommonResponse.error("邀请已处理");
        }
        
        invitation.setStatus(2); // rejected
        invitation.setRespondedAt(LocalDateTime.now());
        invitationRepository.save(invitation);
        
        log.info("用户 {} 拒绝了游戏邀请", currentUid);
        return CommonResponse.success(null);
    }
    
    /**
     * Get pending invitations
     */
    public List<Map<String, Object>> getPendingInvitations(Long inviteeUid) {
        List<FriendInvitation> invitations = invitationRepository
            .findByInviteeUidAndStatusOrderByCreatedAtDesc(inviteeUid, 0);
        
        return invitations.stream().map(invitation -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", invitation.getId());
            map.put("inviterUid", invitation.getInviterUid());
            map.put("gameId", invitation.getGameId());
            map.put("message", invitation.getMessage());
            map.put("createdAt", invitation.getCreatedAt());
            map.put("expiresAt", invitation.getExpiresAt());
            // TODO: Call auth-service API to get inviter info
            map.put("inviterName", "用户" + invitation.getInviterUid());
            // TODO: Call game-service API to get game title
            map.put("gameTitle", "游戏" + invitation.getGameId());
            return map;
        }).collect(Collectors.toList());
    }
}
