package com.sean.seanBI.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sean.seanBI.model.entity.Chart;

import java.util.List;
import java.util.Map;

/**
* @author 24395
* @description 针对表【chart(图表消息表)】的数据库操作Mapper
* @createDate 2024-12-10 20:58:01
* @Entity generator.domain.Chart
*/
public interface ChartMapper extends BaseMapper<Chart> {

    List<Map<String, Object>> queryChartData(String id);
}




