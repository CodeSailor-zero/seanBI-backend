package com.sean.seanBI.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2024/12/13
 * BI 返回结果封装
 **/
@Data
@Builder
@AllArgsConstructor
public class BIResponse {
    private String genChart;
    private String genResult;
    private Long chartId;
}
