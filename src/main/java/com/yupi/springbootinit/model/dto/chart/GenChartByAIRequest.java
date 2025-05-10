package com.yupi.springbootinit.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

/**
 * 智能分析请求
 *
 * @author sean
 * </a>
 */
@Data
public class GenChartByAIRequest implements Serializable {
    /**
     *
     * 图表名字
     */
    private String name;

    /**
     * 分析目标
     */
    private String goal;

    /**
     * 图表类型
     */
    private String chartType;

    /**
     * AI 类型
     */
    private String AiType;
    private static final long serialVersionUID = 1L;
}