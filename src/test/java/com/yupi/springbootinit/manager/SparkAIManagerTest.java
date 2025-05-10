package com.yupi.springbootinit.manager;

import com.yupi.springbootinit.manager.AIManagers.SparkAIManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2024/12/13
 **/
@SpringBootTest
class SparkAIManagerTest {

    @Resource
    private SparkAIManager sparkAiManager;
    @Test
    void doChat() {
//        String prompt = "你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容：\n" +
//                "分析需求：\n" +
//                "{数据分析的需求或者目标}\n" +
//                "原始数据：\n" +
//                "{csv格式的原始数居，用,作为分隔符}\n" +
//                "请根据这两两部分内容，按照以下指定格式生成内容（此外不要输任何多余的开头、结尾、注释，同时不要使用这个符号 '）\n" +
//                "【【【【【\n" +
//                "{前端Echarts V5 的 option 配置对象 js 代码，不要生成任何多余的内容，比如注释和代码块标记}\n" +
//                "【【【【【\n" +
//                "{明确的数据分析结论，越详细越好，不要生成多余的注释}";
        String message = "原始数据如下：\n" +
                "日期，用户数\n" +
                "1号,10\n" +
                "2号,20\n" +
                "3号,30";
//        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append(prompt).append("\n").append(message).append("\n");
//        String result = sparkAiManager.doChat(stringBuilder.toString());
        String result = sparkAiManager.doChat(message);
        System.out.println(result);
    }
}