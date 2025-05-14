package com.sean.seanBI.controller;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2024/12/21
 **/
@RestController
@RequestMapping("/queue")
@Slf4j
@Profile({"dev","local"})
@Deprecated
public class Queue {
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @GetMapping("/add")
    public void add(String name){
        CompletableFuture.runAsync(() -> {
            System.out.println("hello world " + name + "执行人：" + Thread.currentThread().getName());
            try {
                Thread.sleep(6000000);
            } catch (InterruptedException e) {
               e.printStackTrace();
            }
        }, threadPoolExecutor);// 指定线程池
    }

    @GetMapping("/get")
    public String get(){
        HashMap<String, Object> map = new HashMap<>();
        int size = threadPoolExecutor.getQueue().size();
        map.put("队列的长度", size);
        long taskCount = threadPoolExecutor.getTaskCount();
        map.put("队列中任务总数", taskCount);
        long completedTaskCount = threadPoolExecutor.getCompletedTaskCount();
        map.put("已完成的任务总数", completedTaskCount);
        int activeCount = threadPoolExecutor.getActiveCount();
        map.put("正在执行的任务数", activeCount);
        String jsonStr = JSONUtil.toJsonStr(map);
        return jsonStr;
    }
}
