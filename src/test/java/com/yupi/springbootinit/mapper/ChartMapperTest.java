package com.sean.seanBI.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.List;
import java.util.Map;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2024/12/17
 **/
@SpringBootTest
class ChartMapperTest {
    @Resource
    private ChartMapper chartMapper;

    @Test
    void queryChartData() {
        List<Map<String, Object>> stringObjectMap = chartMapper.queryChartData("1868205171067121666");
        System.out.println(stringObjectMap);
    }
}