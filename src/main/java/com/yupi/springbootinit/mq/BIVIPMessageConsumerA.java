package com.yupi.springbootinit.mq;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.rabbitmq.client.Channel;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.constant.UserConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.manager.AIManagers.AIManager;
import com.yupi.springbootinit.manager.AIManagers.SparkAIManager;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.service.ChartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
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
public class BIVIPMessageConsumerA {
    @Resource
    private ChartService chartService;

    @Resource
    private SparkAIManager sparkAiManager;

    @Resource
    private AIManager aiManager;

    //指定程序监听的队列和确认模式
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(BIConstant.BI_QUEUE_NAME),
            exchange = @Exchange(value = BIConstant.BI_EXCHANGE_NAME),
            key = BIConstant.BI_ROUTING_KEY
    ),ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            log.error("我已经接收到信息 A");
            if (StrUtil.isBlank(message)) {
                channel.basicNack(deliveryTag, false, false);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "消息为空");
            }
            seadMessageMQ seadMessageMQ = JSONUtil.toBean(message, seadMessageMQ.class);
            String aiType = seadMessageMQ.getAiType();
            ThrowUtils.throwIf(StrUtil.isBlank(aiType),ErrorCode.PARAMS_ERROR,"请选择AI类型");
            String userRole = seadMessageMQ.getUserRole();
            ThrowUtils.throwIf(UserConstant.DEFAULT_ROLE.equals(userRole) && "智普AI".equals(aiType),ErrorCode.OPERATION_ERROR,"普通用户请选择星火AI");
            long chartId = seadMessageMQ.getId();
            Chart chart = chartService.getById(chartId);
            if (chart == null) {
                channel.basicNack(deliveryTag, false, false);
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图表不存在");
            }
            Chart updateChart = new Chart();
            updateChart.setId(chartId);
            //todo 后续将状态 改为枚举
            updateChart.setStatus("running");
            boolean b = chartService.updateById(updateChart);
            if (!b) {
                // todo 建议再次修改数据库，将状态修改为 failed 失败
                channel.basicNack(deliveryTag, false, false);
                handleChartUpdate(chartId, "更新图表状态失败");
                return;
            }
            //调用AI
            String userInput = getUserInput(chart);
            String result = aiManager.doChat(aiType, userInput);
            String[] splits = result.split("&&&&&");
            if (splits.length > 3) {
                handleChartUpdate(chartId, "AI分析结果格式生成错误");
                return;
            }
            String genChart = splits[1].trim();
            String genResult = splits[2].trim();
            //当执行完成后，修改chart状态为 succeed
            updateChart.setStatus("succeed");
            updateChart.setGenChart(genChart);
            updateChart.setGenResult(genResult);
            b = chartService.updateById(updateChart);
            log.error("updateChart" + updateChart);
            if (!b) {
                // todo 建议再次修改数据库，将状态修改为 failed 失败
                channel.basicNack(deliveryTag, false, false);
                handleChartUpdate(chartId, "更新图表状态失败");
                return;
            }
//            log.info("receive message：" + message);
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 构建用户输入
     *
     * @param chart
     * @return
     */
    public String getUserInput(Chart chart) {
        String goal = chart.getGoal();
        String chartType = chart.getChartType();
        String chartData = chart.getChartData();// 用户输入
        StringBuilder userInput = new StringBuilder();
        String userGoal = goal;
        if (StrUtil.isNotBlank(chartType)) {
            userGoal += "，请使用" + chartType;
        }
        userInput.append("分析目标：").append(userGoal).append("\n");
        //压缩后的数据
        userInput.append("数据：").append(chartData).append("\n");
        return userInput.toString();
    }

    public void handleChartUpdate(long chartId, String execMessage) {
        Chart updateChart = new Chart();
        updateChart.setId(chartId);
        updateChart.setExecMessage(execMessage);
        updateChart.setStatus("failed");
        boolean result = chartService.updateById(updateChart);
        if (!result) {
            log.error("更新图表状态失败：" + chartId + "失败信息" + execMessage);
        }
    }
}
