package com.yupi.springbootinit.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.manager.AIManagers.SparkAIManager;
import com.yupi.springbootinit.mapper.ChartMapper;
import com.yupi.springbootinit.model.dto.chart.ChartRetryRequest;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.service.ChartService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
* @author 24395
* @description 针对表【chart(图表消息表)】的数据库操作Service实现
* @createDate 2024-12-10 20:58:01
*/
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService {
    @Resource
    private SparkAIManager sparkAiManager;
    /**
     * 重新生成图表数据
     * @param chartRetryRequest
     */
    @Override
    public void retry(ChartRetryRequest chartRetryRequest) {
        Long id = chartRetryRequest.getId();
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "此图表不存在");
        String name = chartRetryRequest.getName();
        String goal = chartRetryRequest.getGoal();
        String chartData = chartRetryRequest.getChartData();
        String chartType = chartRetryRequest.getChartType();
        if (StringUtils.isAnyBlank(goal, chartData, chartType)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请补全需要的参数");
        }
        // 拼接用户输入
        String prompt = "你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容：\n" + "分析需求：\n" + "{数据分析的需求或者目标}\n" + "原始数据：\n" + "{csv格式的原始数居，用,作为分隔符}\n" + "请根据这两两部分内容，按照以下指定格式生成内容（此外不要输任何多余的开头、结尾、注释，同时不要使用这个符号 '）\n" + "【【【【【\n" + "{前端Echarts V5 的 option 配置对象 json 代码，不要生成任何多余的内容，比如注释和代码块标记}\n" + "【【【【【\n" + "{明确的数据分析结论，越详细越好，不要生成多余的注释}";
        StringBuilder userInput = new StringBuilder();
        userInput.append(prompt).append("\n");
        String userGoal = goal;
        if (StrUtil.isNotBlank(chartType)) {
            userGoal += "，请使用" + chartType;
        }
        userInput.append("分析目标：").append(userGoal).append("\n");
        userInput.append("数据：").append(chartData).append("\n");

        //调用AI
        String result = sparkAiManager.doChat(userInput.toString());
        String[] splits = result.split("【【【【");
        ThrowUtils.throwIf(splits.length > 3, ErrorCode.SYSTEM_ERROR, "AI生成错误");
        String genChart = splits[1].trim();
        String genResult = splits[2].trim();

        //插入数据库
        Chart chart = new Chart();
        chart.setId(id);
        chart.setGoal(goal);
        chart.setName(name);
        chart.setChartData(chartData);
        chart.setChartType(chartType);
        chart.setGenChart(genChart);
        chart.setGenResult(genResult);
        chart.setStatus("succeed");
        boolean res = updateById(chart);
        ThrowUtils.throwIf(!res, ErrorCode.SYSTEM_ERROR, "图表更新错误");
    }
}




