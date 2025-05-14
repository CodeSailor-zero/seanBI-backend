package com.sean.seanBI.mq;

import com.sean.seanBI.mq.example.MyMessageSender;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2025/2/26
 **/
@SpringBootTest
public class MyMessageTest {
    @Resource
    private MyMessageSender myMessageSender;
    @Test
    public void testSendMessage() {
        myMessageSender.sendMessage("friendship.exchange","friend","hello");

    }
}
