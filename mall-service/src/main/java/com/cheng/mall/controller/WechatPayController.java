package com.cheng.mall.controller;

import com.cheng.common.dto.CommonResponse;
import com.cheng.mall.service.OrderService;
import com.cheng.mall.service.WechatPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信支付控制器
 */
@Slf4j
@RestController
@RequestMapping("/payment/wechat")
@CrossOrigin(origins = "*")
public class WechatPayController {
    
    @Autowired
    private WechatPayService wechatPayService;
    
    @Autowired
    private OrderService orderService;
    
    /**
     * Native 下单（生成支付二维码）
     * 
     * @param request 包含 orderNo, amount, description
     * @return 包含 codeUrl 的响应
     */
    @PostMapping("/native/order")
    public CommonResponse<Map<String, Object>> createNativeOrder(@RequestBody Map<String, Object> request) {
        try {
            String orderNo = (String) request.get("orderNo");
            Double amount = Double.valueOf(request.get("amount").toString());
            String description = (String) request.getOrDefault("description", "游戏商城订单");
            
            log.info("收到微信 Native 下单请求: orderNo={}, amount={}", orderNo, amount);
            
            // 调用微信支付服务
            Map<String, Object> result = wechatPayService.createNativeOrder(orderNo, amount, description);
            
            return CommonResponse.success(result);
            
        } catch (Exception e) {
            log.error("微信 Native 下单失败", e);
            return CommonResponse.error("微信下单失败：" + e.getMessage());
        }
    }
    
    /**
     * 查询订单支付状态
     * 
     * @param orderNo 订单号
     * @return 订单状态
     */
    @GetMapping("/order/query")
    public CommonResponse<Map<String, Object>> queryOrder(@RequestParam String orderNo) {
        try {
            log.info("查询订单支付状态: orderNo={}", orderNo);
            
            Map<String, Object> result = wechatPayService.queryOrder(orderNo);
            
            return CommonResponse.success(result);
            
        } catch (Exception e) {
            log.error("查询订单失败", e);
            return CommonResponse.error("查询订单失败：" + e.getMessage());
        }
    }
    
    /**
     * 关闭订单
     * 
     * @param request 包含 orderNo
     * @return 操作结果
     */
    @PostMapping("/order/close")
    public CommonResponse<Void> closeOrder(@RequestBody Map<String, Object> request) {
        try {
            String orderNo = (String) request.get("orderNo");
            
            log.info("关闭订单: orderNo={}", orderNo);
            
            wechatPayService.closeOrder(orderNo);
            
            return CommonResponse.success(null);
            
        } catch (Exception e) {
            log.error("关闭订单失败", e);
            return CommonResponse.error("关闭订单失败：" + e.getMessage());
        }
    }
    
    /**
     * 微信支付回调通知
     * 微信支付成功后会调用此接口
     * 
     * @param request HTTP 请求
     * @return 处理结果
     */
    @PostMapping("/notify")
    public Map<String, String> notify(HttpServletRequest request) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // 读取请求体
            BufferedReader reader = request.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String body = sb.toString();
            
            log.info("收到微信支付回调通知: {}", body);
            
            // TODO: 验证签名
            // TODO: 解析回调数据
            // TODO: 更新订单状态
            
            // 示例：解析订单号并更新订单
            // 这里需要根据实际的回调数据结构来解析
            
            // 返回成功响应给微信
            response.put("code", "SUCCESS");
            response.put("message", "成功");
            
            log.info("微信支付回调处理成功");
            
        } catch (Exception e) {
            log.error("处理微信支付回调失败", e);
            response.put("code", "FAIL");
            response.put("message", "处理失败：" + e.getMessage());
        }
        
        return response;
    }
}
