package com.cheng.mall.service;

import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信支付服务
 */
@Slf4j
@Service
public class WechatPayService {
    
    @Value("${wechat.pay.app-id}")
    private String appId;
    
    @Value("${wechat.pay.mch-id}")
    private String mchId;
    
    @Value("${wechat.pay.api-v3-key}")
    private String apiV3Key;
    
    @Value("${wechat.pay.private-key-path}")
    private String privateKeyPath;
    
    @Value("${wechat.pay.serial-no}")
    private String serialNo;
    
    @Value("${wechat.pay.notify-url}")
    private String notifyUrl;
    
    @Value("${wechat.pay.mock-enabled:false}")
    private boolean mockEnabled;
    
    private NativePayService nativePayService;
    private Config config;
    
    @PostConstruct
    public void init() {
        // 模拟支付模式不需要初始化真实配置
        if (mockEnabled) {
            log.info("微信支付服务已启用模拟模式，跳过证书初始化");
            return;
        }
        
        try {
            // 使用自动更新证书的配置
            config = new RSAAutoCertificateConfig.Builder()
                    .merchantId(mchId)
                    .privateKeyFromPath(privateKeyPath.replace("classpath:", ""))
                    .merchantSerialNumber(serialNo)
                    .apiV3Key(apiV3Key)
                    .build();
            
            nativePayService = new NativePayService.Builder().config(config).build();
            log.info("微信支付服务初始化成功");
        } catch (Exception e) {
            // TODO: 微信支付功能已临时禁用，记录警告但不阻止启动
            log.warn("微信支付服务初始化失败（已禁用），将使用模拟模式: {}", e.getMessage());
            // 强制启用模拟模式，确保应用可以正常启动
            mockEnabled = true;
            log.info("已自动切换到模拟支付模式");
        }
    }
    
    /**
     * Native 下单（生成支付二维码）
     * 
     * @param orderNo 订单号
     * @param totalAmount 订单金额（元）
     * @param description 商品描述
     * @return 包含 code_url 的 Map，用于生成二维码
     */
    public Map<String, Object> createNativeOrder(String orderNo, Double totalAmount, String description) {
        try {
            log.info("创建微信 Native 订单: orderNo={}, amount={}, mock={}", orderNo, totalAmount, mockEnabled);
            
            // 模拟支付模式
            if (mockEnabled) {
                log.info("【模拟支付】订单创建成功: orderNo={}", orderNo);
                
                Map<String, Object> result = new HashMap<>();
                // 生成模拟的二维码链接（包含订单信息）
                String mockCodeUrl = String.format(
                    "weixin://wxpay/bizpayurl?mock=true&orderNo=%s&amount=%.2f",
                    orderNo, totalAmount
                );
                result.put("codeUrl", mockCodeUrl);
                result.put("orderNo", orderNo);
                result.put("amount", totalAmount);
                result.put("mockMode", true);
                result.put("tip", "演示模式：扫码后将自动模拟支付成功");
                
                return result;
            }
            
            // 真实支付模式
            // 构建请求参数
            PrepayRequest request = new PrepayRequest();
            request.setAppid(appId);
            request.setMchid(mchId);
            request.setDescription(description);
            request.setOutTradeNo(orderNo);
            request.setNotifyUrl(notifyUrl);
            
            // 设置金额（转换为分）
            Amount amount = new Amount();
            amount.setTotal((int) (totalAmount * 100)); // 元转分
            amount.setCurrency("CNY");
            request.setAmount(amount);
            
            // 调用统一下单 API
            PrepayResponse response = nativePayService.prepay(request);
            
            log.info("微信 Native 下单成功: codeUrl={}", response.getCodeUrl());
            
            // 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("codeUrl", response.getCodeUrl());
            result.put("orderNo", orderNo);
            result.put("amount", totalAmount);
            result.put("mockMode", false);
            
            return result;
            
        } catch (Exception e) {
            log.error("微信 Native 下单失败: orderNo={}", orderNo, e);
            throw new RuntimeException("微信下单失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询订单状态
     * 
     * @param orderNo 商户订单号
     * @return 订单信息
     */
    public Map<String, Object> queryOrder(String orderNo) {
        try {
            log.info("查询微信订单: orderNo={}", orderNo);
            
            // TODO: 实现订单查询逻辑
            // 这里需要调用微信支付查询接口
            
            Map<String, Object> result = new HashMap<>();
            result.put("orderNo", orderNo);
            result.put("status", "UNKNOWN");
            
            return result;
            
        } catch (Exception e) {
            log.error("查询微信订单失败: orderNo={}", orderNo, e);
            throw new RuntimeException("查询订单失败: " + e.getMessage());
        }
    }
    
    /**
     * 关闭订单
     * 
     * @param orderNo 商户订单号
     */
    public void closeOrder(String orderNo) {
        try {
            log.info("关闭微信订单: orderNo={}", orderNo);
            
            // TODO: 实现关闭订单逻辑
            // 调用微信支付关闭订单接口
            
        } catch (Exception e) {
            log.error("关闭微信订单失败: orderNo={}", orderNo, e);
            throw new RuntimeException("关闭订单失败: " + e.getMessage());
        }
    }
}
