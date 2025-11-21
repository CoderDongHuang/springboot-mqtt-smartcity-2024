package com.itdonghuang.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.itdonghuang.config.MqttTopicConfig;
import com.itdonghuang.entity.BatteryData;
import com.itdonghuang.service.BatteryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * MQTT消息处理器
 * 负责处理所有MQTT主题的消息
 */
@Component
public class MqttMessageHandler implements MessageHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(MqttMessageHandler.class);

    @Autowired
    private BatteryService batteryService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private MqttTopicConfig topicConfig;
    
    @Autowired
    private org.springframework.messaging.MessageChannel mqttOutboundChannel;

    /**
     * 处理接收到的MQTT消息
     * @param message MQTT消息
     */
    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        String topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC, String.class);
        String payload = message.getPayload().toString();
        
        logger.info("收到MQTT消息 - 主题: {}, 内容: {}", topic, payload);
        
        try {
            if (topic.equals(topicConfig.getSub().getCarData())) {
                handleCarData(payload);
            } else if (topic.equals(topicConfig.getSub().getTip())) {
                handleStatusTip(payload);
            } else if (topic.equals(topicConfig.getPub().getCarVid())) {
                handleCarVIDQuery(payload);
            } else if (topic.equals(topicConfig.getPub().getTip())) {
                handleTipQuery(payload);
            } else {
                logger.warn("未知的MQTT主题: {}", topic);
            }
        } catch (Exception e) {
            logger.error("处理MQTT消息时发生错误 - 主题: {}, 错误: {}", topic, e.getMessage(), e);
        }
    }

    /**
     * 处理车辆数据
     * @param payload 消息内容
     */
    private void handleCarData(String payload) {
        try {
            // 解析JSON数据
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(payload);
            
            // 获取车辆ID和电池ID
            String vid = rootNode.get("VID").asText();  // 使用asText()将任何类型转换为String
            String pid = rootNode.get("PID").asText();
            
            // 获取电池温度和电量
            Double temperature = rootNode.get("T").asDouble();
            Double charge = rootNode.get("C").asDouble();
            
            // 处理电池数据
            batteryService.handleBatteryData(vid, pid, temperature, charge);
            
            logger.info("处理车辆数据成功 - VID: {}, PID: {}, T: {}, C: {}", vid, pid, temperature, charge);
        } catch (Exception e) {
            logger.error("处理车辆数据时发生错误: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理电池状态异常消息
     * 处理status=1/2/3的异常状态，并记录时间
     * @param payload 消息内容
     */
    private void handleStatusTip(String payload) {
        try {
            // 解析JSON数据
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(payload);
            
            // 获取PID和状态
            String pid = rootNode.get("PID").asText();
            Integer status = rootNode.get("status").asInt();
            String timeStr = rootNode.get("time").asText();
            
            // 验证PID不为空
            if (pid == null || pid.trim().isEmpty()) {
                logger.warn("收到无效的PID - 消息内容: {}", payload);
                return;
            }
            
            // 解析时间字符串
            LocalTime time = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm:ss"));
            
            logger.info("处理电池状态异常 - PID: {}, 状态: {}, 时间: {}", pid, status, time);
            
            // 处理电池状态
            batteryService.handleBatteryStatus(pid, status, time);
        } catch (Exception e) {
            logger.error("处理电池状态异常时发生错误: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理车辆VID查询请求
     * @param payload 消息内容
     */
    private void handleCarVIDQuery(String payload) throws Exception {
        Map<String, Object> data = objectMapper.readValue(payload, Map.class);
        String pid = (String) data.get("PID");
        
        logger.info("处理车辆VID查询请求 - PID: {}", pid);
        
        // 查询电池历史数据
        List<BatteryData> history = batteryService.getBatteryHistory(pid);
        
        // 转换为前端需要的格式，只包含温度和电量
        List<Map<String, Double>> responseData = history.stream()
            .map(record -> Map.of(
                "T", record.getT(),
                "C", record.getC()
            ))
            .toList();
        
        // 发送响应
        String responseJson = objectMapper.writeValueAsString(responseData);
        logger.info("发送车辆历史数据 - 数据: {}", responseJson);
        
        // 发送到subAllMsg主题
        Message<?> message = MessageBuilder
            .withPayload(responseJson)
            .setHeader(MqttHeaders.TOPIC, topicConfig.getSub().getAllMsg())
            .build();
        mqttOutboundChannel.send(message);
    }

    /**
     * 处理异常信息查询请求
     * @param payload 消息内容
     */
    private void handleTipQuery(String payload) throws Exception {
        Map<String, Object> data = objectMapper.readValue(payload, Map.class);
        Integer search = ((Number) data.get("search")).intValue();
        
        logger.info("处理异常信息查询请求 - search: {}", search);
        
        // 查询所有异常历史数据
        List<Map<String, Object>> history = batteryService.getAllBatteryStatusHistory();
        
        // 发送响应
        String responseJson = objectMapper.writeValueAsString(history);
        logger.info("发送异常历史数据 - 数据: {}", responseJson);
        
        // 发送到subHistoryTip主题
        Message<?> message = MessageBuilder
            .withPayload(responseJson)
            .setHeader(MqttHeaders.TOPIC, topicConfig.getSub().getHistoryTip())
            .build();
        mqttOutboundChannel.send(message);
    }
} 