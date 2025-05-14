package com.sean.seanBI.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2025/2/26
 **/
@Component
@Slf4j
public class BiInitMain {
    public static void main(String[] args) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            // 死信交换机
            HashMap<String, Object> mapConfig = new HashMap<>();
            mapConfig.put("x-dead-letter-exchange",BIConstant.DEAD_LETTER_EXCHANGE);
            mapConfig.put("x-dead-letter-routing-key",BIConstant.DEAD_LETTER_ROUTING_KEY);
            channel.exchangeDeclare(BIConstant.DEAD_LETTER_EXCHANGE, "direct",true,false,mapConfig);
            // 死信队列
            channel.queueDeclare(BIConstant.DEAD_LETTER_QUEUE, true, false, false, null);
            // 绑定死信队列
            channel.queueBind(BIConstant.DEAD_LETTER_QUEUE, BIConstant.DEAD_LETTER_EXCHANGE, BIConstant.DEAD_LETTER_ROUTING_KEY);

            // 创建交换机
            channel.exchangeDeclare(BIConstant.BI_EXCHANGE_NAME, "direct", true, false, null);
            //创建vip和普通队列
            channel.queueDeclare(BIConstant.BI_QUEUE_NAME, true, false, false, null);
            channel.queueDeclare(BIConstant.BI_COMMON_QUEUE_NAME, true, false, false, null);
            channel.queueBind(BIConstant.BI_QUEUE_NAME, BIConstant.BI_EXCHANGE_NAME, BIConstant.BI_ROUTING_KEY);
            channel.queueBind(BIConstant.BI_COMMON_QUEUE_NAME, BIConstant.BI_EXCHANGE_NAME, BIConstant.BI_COMMON_ROUTING_KEY);

            //死信队列和vip/普通队列绑定
            channel.queueBind(BIConstant.BI_QUEUE_NAME, BIConstant.DEAD_LETTER_EXCHANGE, BIConstant.DEAD_LETTER_ROUTING_KEY);
            channel.queueBind(BIConstant.BI_COMMON_QUEUE_NAME, BIConstant.DEAD_LETTER_EXCHANGE, BIConstant.DEAD_LETTER_ROUTING_KEY);
        } catch (Exception e) {
            log.error("初始化失败", e);
        }
    }
}
