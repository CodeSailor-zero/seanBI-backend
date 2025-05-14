package com.sean.seanBI.manager.AIManagers;

import org.springframework.stereotype.Service;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2025/3/11
 **/
@Service
public interface JudgeStrategy {
    String doChat(String message);
}
