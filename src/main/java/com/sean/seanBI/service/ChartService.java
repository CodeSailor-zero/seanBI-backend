package com.sean.seanBI.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sean.seanBI.model.dto.chart.ChartRetryRequest;
import com.sean.seanBI.model.entity.Chart;


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
