package com.sean.seanBI.utils;

import cn.hutool.core.util.StrUtil;
import com.sean.seanBI.model.entity.Chart;
import com.sean.seanBI.model.enums.ChartStatusEnum;
import com.sean.seanBI.service.ChartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author sean
 * @Date 2025/01/14
 */
@Slf4j
@Component
public class BICommonCode {

    /**
     * 处理图表更新失败问题
     * @param chartId
     * @param execMessage
     */
    public static void handleChartUpdate(long chartId, String execMessage) {
        Chart updateChart = new Chart();
        updateChart.setId(chartId);
        updateChart.setExecMessage(execMessage);
        updateChart.setStatus(ChartStatusEnum.Failed.getStatus());
        ChartService chartService = SpringContextUtils.getBean(ChartService.class);
        boolean result = chartService.updateById(updateChart);
        if (!result) {
            log.error("更新图表状态失败：" + chartId + "失败信息" + execMessage);
        }
    }

    /**
     * 构建用户输入
     * @param chart
     * @return
     */
    public static String getUserInput(Chart chart) {
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
}
