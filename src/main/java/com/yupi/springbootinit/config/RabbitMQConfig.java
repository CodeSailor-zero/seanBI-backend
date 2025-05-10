package com.yupi.springbootinit.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author sean
 * @Date 2025/05/10
 */
@Configuration
@ConfigurationProperties(prefix = "spring.rabbitmq")
@Slf4j
public class RabbitMQConfig {
    private String host;
    private int port;
    private String username;
    private String password;
    private String publisherConfirmType;

    public CachingConnectionFactory getConnectionFactory() {
        CachingConnectionFactory  connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
        return connectionFactory;
    }

    @Bean
    RabbitTemplate rabbitTemplate(){
        CachingConnectionFactory connectionFactory = getConnectionFactory();
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);

        // 消息投递到交换机确认回调
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            String messageId = correlationData != null ? correlationData.getId() : "null";
            if (ack) {
                log.info("消息成功发送到交换机，消息ID：{}", messageId);
            } else {
                log.error("消息发送到交换机失败，消息ID：{}，原因：{}", messageId, cause);
            }
        });
        return rabbitTemplate;
    }

    /**
     * 消费者需要手动确认信息
     * @param connectionFactory
     * @return
     */
    @Bean
    public SimpleRabbitListenerContainerFactory myFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL); // 设置手动确认模式
        return factory;
    }
}
