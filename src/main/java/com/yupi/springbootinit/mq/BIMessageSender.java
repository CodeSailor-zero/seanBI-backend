package com.yupi.springbootinit.mq;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.constant.UserConstant;
import com.yupi.springbootinit.exception.ThrowUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2025/2/26
 **/
@Component
public class BIMessageSender {
    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送信息
     * @param message
     */
    public void sendMessage(String message) {
        seadMessageMQ seadMessageMQ = JSONUtil.toBean(message, seadMessageMQ.class);
        ThrowUtils.throwIf(seadMessageMQ == null, ErrorCode.PARAMS_ERROR, "消息为空");
        String userRole = seadMessageMQ.getUserRole();
        if (UserConstant.DEFAULT_ROLE.equals(userRole)) {
            // 普通用户
            rabbitTemplate.convertAndSend(BIConstant.BI_EXCHANGE_NAME, BIConstant.BI_COMMON_ROUTING_KEY, message);
        } else {
            // VIP用户
            rabbitTemplate.convertAndSend(BIConstant.BI_EXCHANGE_NAME, BIConstant.BI_ROUTING_KEY, message);
        }
    }
}
