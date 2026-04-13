package com.cheng.common.util;

/**
 * 常量定义
 */
public class Constants {
    
    /**
     * 奖品等级
     */
    public static final int PRIZE_LEVEL_SSR = 1;  // 最高稀有度
    public static final int PRIZE_LEVEL_SR = 2;   // 中等稀有度
    public static final int PRIZE_LEVEL_R = 3;    // 普通
    
    /**
     * 抽奖消耗积分
     */
    public static final Integer GACHA_COST_SINGLE = 100;   // 单抽消耗
    public static final Integer GACHA_COST_TEN = 1000;     // 十连抽消耗
    
    /**
     * 保底机制
     */
    public static final Integer GUARANTEED_SSR_COUNT = 90;  // 90 抽内必出 SSR
    
    /**
     * 充值比例
     */
    public static final Double RECHARGE_RATE = 10.0;  // 1 元 = 10 积分
    
    /**
     * 服务器状态
     */
    public static final Integer SERVER_STATUS_NORMAL = 1;      // 正常
    public static final Integer SERVER_STATUS_MAINTENANCE = 0; // 维护中
}
