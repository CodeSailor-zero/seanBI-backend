package com.sean.seanBI.mq;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.rabbitmq.client.Channel;
import com.sean.seanBI.common.ErrorCode;
import com.sean.seanBI.exception.BusinessException;
import com.sean.seanBI.exception.ThrowUtils;
import com.sean.seanBI.manager.AIManagers.AIManager;
import com.sean.seanBI.manager.AIManagers.SparkAIManager;
import com.sean.seanBI.model.entity.Chart;
import com.sean.seanBI.model.enums.ChartStatusEnum;
import com.sean.seanBI.service.ChartService;
import com.sean.seanBI.utils.BICommonCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
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
public class BICommonMessageConsumer {
    @Resource
    private ChartService chartService;

    @Resource
    private SparkAIManager sparkAiManager;

    @Resource
    private AIManager aiManager;

    //指定程序监听的队列和确认模式
    @RabbitListener(queues = {BIConstant.BI_COMMON_QUEUE_NAME})
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            log.error("我已经接收到信息 普通用户");
            if (StrUtil.isBlank(message)) {
                channel.basicNack(deliveryTag, false, false);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "消息为空");
            }
            seadMessageMQ seadMessageMQ = JSONUtil.toBean(message, seadMessageMQ.class);
            String aiType = seadMessageMQ.getAiType();
            ThrowUtils.throwIf(StrUtil.isBlank(aiType) || "智普AI".equals(aiType),ErrorCode.OPERATION_ERROR,"AI类型有误");
            long chartId = seadMessageMQ.getId();
            Chart chart = chartService.getById(chartId);
            if (chart == null) {
                channel.basicNack(deliveryTag, false, false);
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图表不存在");
            }
            Chart updateChart = new Chart();
            updateChart.setId(chartId);
            updateChart.setStatus(ChartStatusEnum.Running.getStatus());
            boolean b = chartService.updateById(updateChart);
            if (!b) {
                // 修改数据库，将状态修改为 failed 失败
                updateChart.setStatus(ChartStatusEnum.Failed.getStatus());
                chartService.updateById(updateChart);
                channel.basicNack(deliveryTag, false, false);
                BICommonCode.handleChartUpdate(chartId, "更新图表状态失败");
                return;
            }
            //调用AI
            String userInput = BICommonCode.getUserInput(chart);
            String result = aiManager.doChat(aiType, userInput);
            String[] splits = result.split("&&&&&");
            if (splits.length > 3) {
                BICommonCode.handleChartUpdate(chartId, "AI分析结果格式生成错误");
                return;
            }
            String genChart = splits[1].trim();
            String genResult = splits[2].trim();
            //当执行完成后，修改chart状态为 succeed
            updateChart.setStatus(ChartStatusEnum.Success.getStatus());
            updateChart.setGenChart(genChart);
            updateChart.setGenResult(genResult);
            b = chartService.updateById(updateChart);
            if (!b) {
                // 再次修改数据库，将状态修改为 failed 失败
                updateChart.setStatus(ChartStatusEnum.Failed.getStatus());
                chartService.updateById(updateChart);
                //手动拒绝确认消息，让信息路由到死信队列
                channel.basicNack(deliveryTag, false, false);
                BICommonCode.handleChartUpdate(chartId, "更新图表状态失败");
                return;
            }
            // 处理成功，确认消息
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            // 处理失败，拒绝消息
            channel.basicNack(deliveryTag, false, false);
            throw new RuntimeException(e);
        }
    }
}
