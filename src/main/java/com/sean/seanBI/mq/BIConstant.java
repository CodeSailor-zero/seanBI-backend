package com.sean.seanBI.mq;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2025/2/26
 **/
public interface BIConstant {
    // VIP
    String BI_EXCHANGE_NAME = "bi.exchange";
    String BI_QUEUE_NAME = "bi.queue";
    String BI_ROUTING_KEY = "bi.routingKey";
    // 普通
    String BI_COMMON_QUEUE_NAME = "bi.common.queue";
    String BI_COMMON_ROUTING_KEY = "bi.common.routingKey";
    // 死信交换机名称
    String DEAD_LETTER_EXCHANGE = "dead-letter-exchange";
    // 死信队列名称
    String DEAD_LETTER_QUEUE = "dead-letter-queue";
   // 死信路由键
    String  DEAD_LETTER_ROUTING_KEY = " dlx.routingKey";

}
