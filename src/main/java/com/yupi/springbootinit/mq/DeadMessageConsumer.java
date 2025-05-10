package com.yupi.springbootinit.mq;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2025/2/26
 **/
@Component
@Slf4j
public class DeadMessageConsumer {

    //指定程序监听的队列和确认模式
    @RabbitListener(queues = {BIConstant.DEAD_LETTER_QUEUE},ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel,@Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            channel.basicNack(deliveryTag, false, true);
        } catch (IOException e) {
            log.error("Failed to nack message: " + message, e);
            throw new RuntimeException(e);
        }
    }
}
