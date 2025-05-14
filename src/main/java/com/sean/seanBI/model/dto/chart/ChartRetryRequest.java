package com.sean.seanBI.model.dto.chart;

import lombok.Data;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2025/3/8
 **/
@Data
public class ChartRetryRequest {
    /**
     * 图表id
     */
    private Long id;
    /**
     * 图表名称
     */
    private String name;
    /**
     * 分析目标
     */
    private String goal;
    /**
     * 图表数据
     */
    private String chartData;
    /**
     * 图表类型
     */
    private String chartType;
}
