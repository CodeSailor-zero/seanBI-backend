package com.sean.seanBI.mq;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
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
    @RabbitListener(queues = {BIConstant.DEAD_LETTER_QUEUE})
    public void receiveMessage(String message, Channel channel,@Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            //重新发送信息到对应队列
            BIMessageSender biMessageSender = new BIMessageSender();
            biMessageSender.sendMessage(message);
            channel.basicNack(deliveryTag, false, false);
        } catch (IOException e) {
            log.error("Failed to nack message: " + message, e);
            channel.basicNack(deliveryTag, false, false);
            throw new RuntimeException(e);
        }
    }
}
