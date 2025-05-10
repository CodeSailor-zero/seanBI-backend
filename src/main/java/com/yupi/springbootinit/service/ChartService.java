package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.model.dto.chart.ChartQueryRequest;
import com.yupi.springbootinit.model.dto.chart.ChartRetryRequest;
import com.yupi.springbootinit.model.entity.Chart;


/**
* @author 24395
* @description 针对表【chart(图表消息表)】的数据库操作Service
* @createDate 2024-12-10 20:58:01
*/
public interface ChartService extends IService<Chart> {
    /**
     * 重新生成图表
     * @param chartRetryRequest
     */
    void retry(ChartRetryRequest chartRetryRequest);

}
