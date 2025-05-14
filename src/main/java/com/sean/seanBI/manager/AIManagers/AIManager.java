package com.sean.seanBI.manager.AIManagers;

import com.sean.seanBI.common.ErrorCode;
import com.sean.seanBI.exception.BusinessException;
import com.sean.seanBI.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2025/3/11
 **/
@Service
public class AIManager {
    @Resource
    private SparkAIManager sparkAIManager;

    @Resource
    private ZhiPuManager zhiPuManager;
    @Resource
    private UserService userService;

    private static final Map<String, JudgeStrategy> strategyMap = new HashMap<>();

    public AIManager() {
    }

    @PostConstruct
    public void init() {
        strategyMap.put("星火AI", sparkAIManager);
        strategyMap.put("智普AI", zhiPuManager);
        // 可以在这里添加更多的策略实现
    }

    public String doChat(String AiType, String message) {
        JudgeStrategy strategy = strategyMap.get(AiType);
        if (strategy == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的AI类型");
        }

        return strategy.doChat(message);
    }
}
