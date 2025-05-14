package com.sean.seanBI.manager.AIManagers;

import com.sean.seanBI.common.ErrorCode;
import com.sean.seanBI.exception.ThrowUtils;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.ChatCompletionRequest;
import com.zhipu.oapi.service.v4.model.ChatMessage;
import com.zhipu.oapi.service.v4.model.ModelApiResponse;
import com.zhipu.oapi.service.v4.model.ModelData;
import io.reactivex.Flowable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2025/3/11
 **/
@Service
public class ZhiPuManager implements JudgeStrategy {
    @Resource
    private ClientV4 clientV4;
    //靠谱
    private static final Float TEMPERATURE = 0.5f;
    //不靠谱
    private static final Float UN_TEMPERATURE = 0.8f;

    // 系统预设
    public static final String SYSTEM_PROMPT = "你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容：\n" +
            "分析需求：\n" +
            "{数据分析的需求或者目标}\n" +
            "原始数据：\n" +
            "{csv格式的原始数据，用,作为分隔符}\n" +
            "请根据这两部分内容：按照以下指定格式生成内容（此外不要输出任何多余的开头、结尾、注释）\n" +
            "【【【【【\n" +
            "{前端 Echarts V5 的 option 配置对象js代码，合理地将数据进行可视化，不要生成任何多余的内容，比如注释}\n" +
            "【【【【【\n" +
            "{明确的数据分析结论，越详细越好，不要生成多余的注释}\n";

    /**
     * 通用Ai解析
     * @param message
     * @return
     */

    public String doChat(String message) {
        ThrowUtils.throwIf(StringUtils.isAnyBlank(message), ErrorCode.PARAMS_ERROR,"请检查你的选项，一些是必填的");
        List<ChatMessage> chatMessage = getChatMessage(SYSTEM_PROMPT, message);

        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
            chatCompletionRequest.setMessages(chatMessage);
            chatCompletionRequest.setModel("glm-4-flash");
            chatCompletionRequest.setStream(false);
            chatCompletionRequest.setInvokeMethod(Constants.invokeMethod);
            ModelApiResponse invokeModelApiResp = clientV4.invokeModelApi(chatCompletionRequest);
            String result = invokeModelApiResp.getData().getChoices().get(0).getMessage().getContent().toString();
            return result;
    }

    /***
     * AI回答 通过流式输出（靠谱）
     * @param message
     * @return
     */
    public Flowable<ModelData> doChatBySSE(String message) {
        ThrowUtils.throwIf(StringUtils.isAnyBlank(message), ErrorCode.PARAMS_ERROR,"请检查你的选项，一些是必填的");
        List<ChatMessage> chatMessage = getChatMessage(SYSTEM_PROMPT, message);
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(true)
                .temperature(TEMPERATURE)
                .messages(chatMessage)
                .build();
        ModelApiResponse invokeModelApiResp = clientV4.invokeModelApi(chatCompletionRequest);
        Flowable<ModelData> flowable = invokeModelApiResp.getFlowable();
        return flowable;
    }

    /**
     * 构建发送消息
     * @param SystemMessages
     * @param UserMessages
     * @return
     */
    public List<ChatMessage> getChatMessage(String SystemMessages,String UserMessages){
        List<ChatMessage> messages = new ArrayList<>(2);
        //构建系统预设
        ChatMessage systemMessage = new ChatMessage();
        systemMessage.setRole("system");
        systemMessage.setContent(SystemMessages);
        messages.add(systemMessage);

        //构建用户发送内容
        ChatMessage userMessage = new ChatMessage();
        userMessage.setRole("user");
        userMessage.setContent(UserMessages);
        messages.add(userMessage);
        return messages;
    }
}
