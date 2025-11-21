package com.itdonghuang.config;

import com.itdonghuang.mqtt.MqttMessageHandler;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

/**
 * MQTT配置类
 * 配置MQTT连接和消息处理
 */
@Configuration
public class MqttConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(MqttConfig.class);
    
    @Value("${mqtt.broker.url}")
    private String brokerUrl;
    
    @Value("${mqtt.client.id}")
    private String clientId;
    
    @Value("${mqtt.username}")
    private String username;
    
    @Value("${mqtt.password}")
    private String password;
    
    @Autowired
    private MqttTopicConfig topicConfig;

    /**
     * 创建MQTT客户端工厂
     */
    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        logger.info("初始化MQTT客户端工厂 - Broker: {}", brokerUrl);
        
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[] { brokerUrl });
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setCleanSession(true);
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(30);
        options.setKeepAliveInterval(60);
        options.setMaxReconnectDelay(5000);
        factory.setConnectionOptions(options);
        
        logger.info("MQTT客户端工厂初始化完成");
        return factory;
    }

    /**
     * 创建MQTT输入通道
     */
    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    /**
     * 创建MQTT输出通道
     */
    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    /**
     * 创建MQTT消息生产者
     */
    @Bean
    public MessageProducer inbound() {
        logger.info("开始初始化MQTT消息生产者");
        
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(clientId + "_inbound", mqttClientFactory(),
                        topicConfig.getSub().getCarData(),
                        topicConfig.getSub().getTip(),
                        topicConfig.getPub().getCarVid(),
                        topicConfig.getPub().getTip());
        
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        adapter.setOutputChannel(mqttInputChannel());
        
        logger.info("MQTT消息生产者初始化完成 - 订阅主题: {}, {}, {}, {}", 
                   topicConfig.getSub().getCarData(),
                   topicConfig.getSub().getTip(),
                   topicConfig.getPub().getCarVid(),
                   topicConfig.getPub().getTip());
        return adapter;
    }

    /**
     * 创建MQTT消息处理器
     */
    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler() {
        return new MqttMessageHandler();
    }

    /**
     * 创建MQTT消息发送处理器
     */
    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MqttPahoMessageHandler mqttOutbound() {
        
        MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler(clientId + "_outbound", mqttClientFactory());
        messageHandler.setAsync(true);
        messageHandler.setDefaultQos(1);
        
        logger.info("MQTT消息发送处理器初始化完成");
        return messageHandler;
    }
} 