package com.cheng.game.controller;

import com.cheng.common.dto.CommonResponse;
import com.cheng.common.dto.DeliveryAddressRequest;
import com.cheng.common.dto.ExchangeRecordResponse;
import com.cheng.common.dto.ExchangeRequest;
import com.cheng.common.entity.DeliveryAddress;
import com.cheng.common.entity.ExchangeItem;
import com.cheng.common.entity.ExchangeRecord;
import com.cheng.common.repository.DeliveryAddressRepository;
import com.cheng.common.repository.ExchangeItemRepository;
import com.cheng.common.repository.ExchangeRecordRepository;
import com.cheng.common.util.JwtUtil;
import com.cheng.game.service.ExchangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 积分兑换控制器
 */
@RestController
@RequestMapping("/api/exchange")
@CrossOrigin(origins = "*")
public class ExchangeController {
    
    @Autowired
    private ExchangeService exchangeService;
    
    @Autowired
    private ExchangeItemRepository exchangeItemRepository;
    
    @Autowired
    private ExchangeRecordRepository exchangeRecordRepository;
    
    @Autowired
    private DeliveryAddressRepository deliveryAddressRepository;
    
    /**
     * 获取所有可兑换物品列表
     */
    @GetMapping("/items")
    public CommonResponse<List<ExchangeItem>> getExchangeItems() {
        try {
            List<ExchangeItem> items = exchangeService.getAllExchangeItems();
            return CommonResponse.success(items);
        } catch (Exception e) {
            return CommonResponse.error(e.getMessage());
        }
    }
    
    /**
     * 根据 ID 获取物品详情
     */
    @GetMapping("/items/{itemId}")
    public CommonResponse<ExchangeItem> getExchangeItem(@PathVariable Long itemId) {
        try {
            ExchangeItem item = exchangeService.getExchangeItemById(itemId);
            return CommonResponse.success(item);
        } catch (Exception e) {
            return CommonResponse.error(e.getMessage());
        }
    }
    
    /**
     * 执行积分兑换
     */
    @PostMapping
    public CommonResponse<ExchangeRecord> exchange(
            @RequestBody ExchangeRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            // 从 Token 中获取用户 ID
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return CommonResponse.error(401, "未授权访问");
            }
            
            String token = authorization.substring(7);
            
            // 验证 Token
            if (!JwtUtil.validateToken(token)) {
                return CommonResponse.error(401, "Token 无效或已过期");
            }
            
            // 获取用户 ID
            Long userId = JwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                return CommonResponse.error(401, "Token 无效或已过期");
            }
            
            // 设置用户 ID
            request.setUserId(userId);
            
            // 执行兑换
            ExchangeRecord record = exchangeService.exchange(request);
            
            return CommonResponse.success(record);
        } catch (IllegalArgumentException e) {
            return CommonResponse.error(400, e.getMessage());
        } catch (RuntimeException e) {
            return CommonResponse.error(e.getMessage());
        } catch (Exception e) {
            return CommonResponse.error("兑换失败：" + e.getMessage());
        }
    }
    
    /**
     * 查询用户兑换记录
     */
    @GetMapping("/records")
    public CommonResponse<List<ExchangeRecordResponse>> getUserExchangeRecords(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            // 从 Token 中获取用户 ID
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return CommonResponse.error(401, "未授权访问");
            }
            
            String token = authorization.substring(7);
            
            // 验证 Token
            if (!JwtUtil.validateToken(token)) {
                return CommonResponse.error(401, "Token 无效或已过期");
            }
            
            // 获取用户 ID
            Long userId = JwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                return CommonResponse.error(401, "Token 无效或已过期");
            }
            
            List<ExchangeRecord> records = exchangeService.getUserExchangeRecords(userId);
            
            // 转换为响应 DTO
            List<ExchangeRecordResponse> responseList = records.stream()
                .map(record -> {
                    ExchangeRecordResponse response = new ExchangeRecordResponse();
                    response.setId(record.getId());
                    response.setUserId(record.getUserId());
                    response.setItemId(record.getItemId());
                    response.setPointsCost(record.getUsedPoints());
                    response.setStatus(record.getStatus());
                    response.setExchangeDate(record.getExchangeDate());
                    response.setCreatedAt(record.getCreatedAt());
                    
                    // 获取物品信息
                    ExchangeItem item = exchangeItemRepository.findById(record.getItemId()).orElse(null);
                    if (item != null) {
                        response.setItemName(item.getName());
                        response.setItemIconUrl(item.getIconUrl());
                    }
                    
                    // 获取地址信息
                    DeliveryAddress address = deliveryAddressRepository
                        .findByExchangeRecordId(record.getId()).orElse(null);
                    if (address != null) {
                        response.setRecipientName(address.getRecipientName());
                        response.setPhoneNumber(address.getPhoneNumber());
                        response.setFullAddress(
                            address.getProvince() + address.getCity() + address.getDistrict() + 
                            address.getDetailAddress()
                        );
                    }
                    
                    return response;
                })
                .collect(java.util.stream.Collectors.toList());
            
            return CommonResponse.success(responseList);
        } catch (Exception e) {
            return CommonResponse.error(e.getMessage());
        }
    }
    
    /**
     * 填写/更新收货地址
     */
    @PostMapping("/address")
    public CommonResponse<DeliveryAddress> saveDeliveryAddress(
            @RequestBody DeliveryAddressRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            // 从 Token 中获取用户 ID
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return CommonResponse.error(401, "未授权访问");
            }
            
            String token = authorization.substring(7);
            
            // 验证 Token
            if (!JwtUtil.validateToken(token)) {
                return CommonResponse.error(401, "Token 无效或已过期");
            }
            
            // 获取用户 ID
            Long userId = JwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                return CommonResponse.error(401, "Token 无效或已过期");
            }
            
            // 验证兑换记录是否存在且属于该用户
            ExchangeRecord record = exchangeRecordRepository.findById(request.getExchangeRecordId())
                    .orElseThrow(() -> new RuntimeException("兑换记录不存在"));
            
            if (!record.getUserId().equals(userId)) {
                return CommonResponse.error(403, "无权操作该兑换记录");
            }
            
            // 检查是否已有地址信息
            DeliveryAddress address = deliveryAddressRepository
                    .findByExchangeRecordId(request.getExchangeRecordId())
                    .orElse(null);
            
            if (address == null) {
                // 创建新地址
                address = new DeliveryAddress();
                address.setExchangeRecordId(request.getExchangeRecordId());
                address.setUserId(userId);
                address.setRecipientName(request.getRecipientName());
                address.setPhoneNumber(request.getPhoneNumber());
                address.setProvince(request.getProvince());
                address.setCity(request.getCity());
                address.setDistrict(request.getDistrict());
                address.setDetailAddress(request.getDetailAddress());
            } else {
                // 更新现有地址
                address.setRecipientName(request.getRecipientName());
                address.setPhoneNumber(request.getPhoneNumber());
                address.setProvince(request.getProvince());
                address.setCity(request.getCity());
                address.setDistrict(request.getDistrict());
                address.setDetailAddress(request.getDetailAddress());
                address.setUpdatedAt(LocalDateTime.now());
            }
            
            deliveryAddressRepository.save(address);
            
            // 更新兑换记录状态为"已填写地址"
            record.setStatus(1);
            exchangeRecordRepository.save(record);
            
            return CommonResponse.success(address);
        } catch (Exception e) {
            return CommonResponse.error("保存地址失败：" + e.getMessage());
        }
    }
    
    /**
     * 查询兑换记录的收货地址
     */
    @GetMapping("/address/{exchangeRecordId}")
    public CommonResponse<DeliveryAddress> getDeliveryAddress(
            @PathVariable Long exchangeRecordId,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            // 从 Token 中获取用户 ID
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return CommonResponse.error(401, "未授权访问");
            }
            
            String token = authorization.substring(7);
            
            // 验证 Token
            if (!JwtUtil.validateToken(token)) {
                return CommonResponse.error(401, "Token 无效或已过期");
            }
            
            // 获取用户 ID
            Long userId = JwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                return CommonResponse.error(401, "Token 无效或已过期");
            }
            
            // 验证兑换记录是否存在且属于该用户
            ExchangeRecord record = exchangeRecordRepository.findById(exchangeRecordId)
                    .orElseThrow(() -> new RuntimeException("兑换记录不存在"));
            
            if (!record.getUserId().equals(userId)) {
                return CommonResponse.error(403, "无权操作该兑换记录");
            }
            
            // 查询地址
            DeliveryAddress address = deliveryAddressRepository
                    .findByExchangeRecordId(exchangeRecordId)
                    .orElseThrow(() -> new RuntimeException("地址信息不存在"));
            
            return CommonResponse.success(address);
        } catch (Exception e) {
            return CommonResponse.error(e.getMessage());
        }
    }
}
