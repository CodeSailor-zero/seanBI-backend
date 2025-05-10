package com.yupi.springbootinit.manager.AIManagers;

import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
import io.github.briqt.spark4j.SparkClient;
import io.github.briqt.spark4j.constant.SparkApiVersion;
import io.github.briqt.spark4j.model.SparkMessage;
import io.github.briqt.spark4j.model.SparkSyncChatResponse;
import io.github.briqt.spark4j.model.request.SparkRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2024/12/13
 **/
@Service
@Slf4j
public class SparkAIManager implements JudgeStrategy {
    @Resource
    private SparkClient sparkClient;

    // 系统预设
    final String SYSTEM_PROMPT = "你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容：\n" +
            "分析需求：\n" +
            "{数据分析的需求或者目标}\n" +
            "原始数据：\n" +
            "{csv格式的原始数据，用,作为分隔符}\n" +
            "请根据这两部分内容：必须严格按照一下指定格式生成内容（此外不要输出任何多余的开头、结尾、注释）同时不要使用这个符号'】'\n" +
            "&&&&&\n" +
            "{前端 Echarts V5 的 option 配置对象 JSON 代码，不要生成任何多余的内容，比如注释和代码块标记}\n" +
            "&&&&&\n" +
            "{明确的数据分析结论，越详细越好，不要生成多余的注释} 也就是说格式为：\n" +
            "（&&&&&+代码+&&&&&+结论），严格按照这个格式。\n" +
            "下面是一个具体的例子的模板：" +
            "&&&&&\n" +
            "JSON格式代码" +
            "&&&&&\n" +
            "结论：";

    public String doChat(String message) {
        ThrowUtils.throwIf(StringUtils.isAnyBlank(message),ErrorCode.PARAMS_ERROR,"一些选项是必填的");

        List<SparkMessage> messages = new ArrayList<>(2);
        // 构建系统信息
        SparkMessage systemMessage = SparkMessage.systemContent(SYSTEM_PROMPT);
        messages.add(systemMessage);
        //构建用户信息
        SparkMessage userMessage = SparkMessage.userContent(message);
        messages.add(userMessage);
        // 构造请求
        SparkRequest sparkRequest = SparkRequest.builder()
                // 消息列表
                .messages(messages)
                // 模型回答的tokens的最大长度,非必传,取值为[1,4096],默认为2048
                .maxTokens(2048)
                // 核采样阈值。用于决定结果随机性,取值越高随机性越强即相同的问题得到的不同答案的可能性越高 非必传,取值为[0,1],默认为0.5
                .temperature(0.2)
                // 指定请求版本，默认使用最新2.0版本
                .apiVersion(SparkApiVersion.V3_5)
                .build();
        // 同步调用
        SparkSyncChatResponse chatResponse = sparkClient.chatSync(sparkRequest);
        if (chatResponse == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "星火 AI 调用失败");
        }
        String responseContent = chatResponse.getContent();
//        log.info("星火 AI 返回的结果 {}", responseContent);
        return responseContent;
    }

//    /**
//     * DeepSeekAI 的调用(放弃，需要花钱)
//     *
//     * @param message
//     * @return
//     */
//    @Deprecated
//    public String doChatDeepSeek(String message) {
//        // 系统预设
//        final String SYSTEM_PROMPT = "你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容：\n" +
//                "分析需求：\n" +
//                "{数据分析的需求或者目标}\n" +
//                "原始数据：\n" +
//                "{csv格式的原始数据，用,作为分隔符}\n" +
//                "请根据这两部分内容：按照以下指定格式生成内容（此外不要输出任何多余的开头、结尾、注释）\n" +
//                "【【【【【\n" +
//                "{前端 Echarts V5 的 option 配置对象js代码，合理地将数据进行可视化，不要生成任何多余的内容，比如注释}\n" +
//                "【【【【【\n" +
//                "{明确的数据分析结论，越详细越好，不要生成多余的注释}\n";
//        try {
//            // 实例化一个请求对象,每个接口都会对应一个request对象
//            ChatCompletionsRequest req = new ChatCompletionsRequest();
//            req.setModel("deepseek-v3");
//            req.setStream(false);// 是否流式输出
//            // 系统信息
//            Message[] messages = new Message[2];
//            Message message0 = new Message();
//            message0.setRole("system");
//            message0.setContent(SYSTEM_PROMPT);//发送给AI的内容
//            messages[0] = message0;
//
//            //用户信息
//            Message message1 = new Message();
//            message1.setRole("user");
//            message1.setContent(message);//发送给AI的内容
//            messages[1] = message1;
//
//            req.setMessages(messages);
//
//            // 返回的resp是一个ChatCompletionsResponse的实例，与请求对象对应
//            ChatCompletionsResponse resp = deepSeekClient.ChatCompletions(req);
//            // 输出json格式的字符串回包
//            return AbstractModel.toJsonString(resp);
//        } catch (TencentCloudSDKException e) {
//            log.error("deepSeek API 调用失败", e);
//            throw new BusinessException(ErrorCode.OPERATION_ERROR, "deepSeek AI 调用失败: " + e.getMessage());
//        } catch (Exception e) {
//            log.error("未知错误", e);
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统错误: " + e.getMessage());
//        }
//    }

}
