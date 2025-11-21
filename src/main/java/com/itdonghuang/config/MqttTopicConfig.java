package com.itdonghuang.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * MQTT主题配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "mqtt.topics")
public class MqttTopicConfig {
    
    private Pub pub = new Pub();
    private Sub sub = new Sub();
    
    /**
     * 发布主题配置
     */
    @Data
    public static class Pub {
        private String carVid = "pubCarVID";    // 查询主题 - 接收车辆历史数据查询请求
        private String tip = "pubTip";          // 查询主题 - 接收异常信息查询请求
    }
    
    /**
     * 订阅主题配置
     */
    @Data
    public static class Sub {
        private String allMsg = "subAllMsg";        // 响应主题 - 发送所有的历史数据
        private String carData = "subCarData";      // 接收主题 - 接收电池数据
        private String tip = "subTip";              // 接收主题 - 接收异常警报消息
        private String historyTip = "subHistoryTip"; // 响应主题 - 发送异常状态的历史数据
    }
} 