package com.sean.seanBI.manager;

import com.sean.seanBI.manager.AIManagers.ZhiPuManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2025/3/11
 **/
@SpringBootTest
class ZhiPuManagerTest {
    @Resource
    private ZhiPuManager zhiPuManager;

    @Test
    void doChat() {
//        String res = zhiPuManager.doChat("你好，你是谁？");
//        System.out.println(res);
    }
}