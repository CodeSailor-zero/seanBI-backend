package com.sean.seanBI.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sean.seanBI.annotation.AuthCheck;
import com.sean.seanBI.common.BaseResponse;
import com.sean.seanBI.common.DeleteRequest;
import com.sean.seanBI.common.ErrorCode;
import com.sean.seanBI.common.ResultUtils;
import com.sean.seanBI.constant.UserConstant;
import com.sean.seanBI.exception.BusinessException;
import com.sean.seanBI.exception.ThrowUtils;
import com.sean.seanBI.manager.AIManagers.AIManager;
import com.sean.seanBI.manager.AIManagers.SparkAIManager;
import com.sean.seanBI.manager.RedisLimiterManager;
import com.sean.seanBI.model.dto.chart.*;
import com.sean.seanBI.model.entity.Chart;
import com.sean.seanBI.model.entity.User;
import com.sean.seanBI.model.enums.ChartStatusEnum;
import com.sean.seanBI.model.vo.BIResponse;
import com.sean.seanBI.utils.BICommonCode;
import com.sean.seanBI.mq.BIMessageSender;
import com.sean.seanBI.mq.seadMessageMQ;
import com.sean.seanBI.service.ChartService;
import com.sean.seanBI.service.UserService;
import com.sean.seanBI.utils.ExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 图表接口
 *
 * @author sean
 * </a>
 */
@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {

    @Resource
    private ChartService chartService;

    @Resource
    private UserService userService;

    @Resource
    private SparkAIManager sparkAiManager;

    @Resource
    private AIManager aiManager;

    @Resource
    private RedisLimiterManager redisLimiterManager;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private BIMessageSender bIMessageSender;

    // region 增删改查

    /**
     * 创建
     *
     * @param chartAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartAddRequest, chart);
        User loginUser = userService.getLoginUser(request);
        chart.setUserId(loginUser.getId());
        boolean result = chartService.save(chart);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newChartId = chart.getId();
        return ResultUtils.success(newChartId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldChart.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = chartService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param chartUpdateRequest
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);
        // 参数校验
        long id = chartUpdateRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Chart> getChartById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = chartService.getById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(chart);
    }

//    /**
//     * 分页获取列表（仅管理员）
//     *
//     * @param chartQueryRequest
//     * @return
//     */
//    @PostMapping("/list/page")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
//    public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest) {
//        long current = chartQueryRequest.getCurrent();
//        long size = chartQueryRequest.getPageSize();
//        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
//                getQueryWrapper(chartQueryRequest));
//        return ResultUtils.success(chartPage);
//    }

    /**
     * 分页获取列表（封装类）（仅管理员）
     *
     * @param chartQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size), getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<Chart>> listMyChartByPage(@RequestBody ChartQueryRequest chartQueryRequest, HttpServletRequest request) {
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        chartQueryRequest.setUserId(loginUser.getId());
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
//        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size), getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 编辑（用户）
     *
     * @param chartEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editChart(@RequestBody ChartEditRequest chartEditRequest, HttpServletRequest request) {
        if (chartEditRequest == null || chartEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartEditRequest, chart);
        // 参数校验
        User loginUser = userService.getLoginUser(request);
        long id = chartEditRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldChart.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }
    // endregion
    // region AI生成图表

    /**
     * 数据分析（同步）
     *
     * @param multipartFile
     * @param genChartByAIRequest
     * @param request
     * @return
     */
    @PostMapping("/gen")
    public BaseResponse<BIResponse> geChartByAI(@RequestPart("file") MultipartFile multipartFile, GenChartByAIRequest genChartByAIRequest, HttpServletRequest request) {
        String name = genChartByAIRequest.getName();
        String goal = genChartByAIRequest.getGoal();
        String chartType = genChartByAIRequest.getChartType();
        String AiType = genChartByAIRequest.getAiType();
        // 校验参数
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");
        ThrowUtils.throwIf(StringUtils.isBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "图表名字过长");
        ThrowUtils.throwIf(StringUtils.isBlank(AiType), ErrorCode.PARAMS_ERROR, "请选择Ai类型");
        //文件校验
        FileVerification(multipartFile);

        User loginUser = userService.getLoginUser(request);
        //限流判断，每个用户一个限流器
        redisLimiterManager.doRateLimit("genChartByAI_" + String.valueOf(loginUser.getId()));

        // 用户输入
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        StringBuilder userInput = getUserInput(goal, chartType, csvData);
        //调用AI
        String result = aiManager.doChat(AiType, userInput.toString());
        String[] splits = result.split("【【【【【");
        ThrowUtils.throwIf(splits.length > 3, ErrorCode.SYSTEM_ERROR, "AI生成错误");
        String genChart = splits[1].trim();
        String genResult = splits[2].trim();

        //插入到数据库
        Chart chart = new Chart();
        chart.setGoal(goal);
        chart.setName(name);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setStatus("succeed");
        chart.setGenChart(genChart);
        chart.setGenResult(genResult);
        chart.setUserId(loginUser.getId());
        boolean isSave = chartService.save(chart);
        ThrowUtils.throwIf(!isSave, ErrorCode.SYSTEM_ERROR, "图表保存错误");
        BIResponse biResponse = BIResponse.builder()
                .chartId(chart.getId())
                .genChart(genChart)
                .genResult(genResult)
                .build();
        return ResultUtils.success(biResponse);
    }

    /**
     * 数据分析（异步）
     *
     * @param multipartFile
     * @param genChartByAIRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/Async")
    public BaseResponse<BIResponse> geChartByAIAsync(@RequestPart("file") MultipartFile multipartFile, GenChartByAIRequest genChartByAIRequest, HttpServletRequest request) {
        String name = genChartByAIRequest.getName();
        String goal = genChartByAIRequest.getGoal();
        String chartType = genChartByAIRequest.getChartType();
        String AiType = genChartByAIRequest.getAiType();
        // 校验参数
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");
        ThrowUtils.throwIf(StringUtils.isBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "图表名字过长");
        ThrowUtils.throwIf(StringUtils.isBlank(AiType), ErrorCode.PARAMS_ERROR, "请选择Ai类型");
        //文件校验
        FileVerification(multipartFile);

        User loginUser = userService.getLoginUser(request);
        //限流判断，每个用户一个限流器
        redisLimiterManager.doRateLimit("genChartByAI_" + String.valueOf(loginUser.getId()));

        // 用户输入
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        StringBuilder userInput = getUserInput(goal, chartType, csvData);

        //插入到数据库
        Chart chart = new Chart();
        chart.setGoal(goal);
        chart.setName(name);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setStatus("wait");
        chart.setUserId(loginUser.getId());
        boolean isSave = chartService.save(chart);
        ThrowUtils.throwIf(!isSave, ErrorCode.SYSTEM_ERROR, "图表保存错误");

        long chartId = chart.getId();
        CompletableFuture.runAsync(() -> {
            Chart updateChart = new Chart();
            updateChart.setId(chartId);
            updateChart.setStatus(ChartStatusEnum.Running.getStatus());
            boolean b = chartService.updateById(updateChart);
            log.info("更新图表状态" + b);
            if (!b) {
                log.error("更新图表失败");
                BICommonCode.handleChartUpdate(chartId, "更新图表状态失败");
                return;
            }
            //调用AI
            String result = aiManager.doChat(AiType, userInput.toString());
//            log.info("AI分析结果：" + result);
            String[] splits = result.split("&&&&&");
//            log.info("结果解析：" + splits);
            if (splits.length > 3) {
                log.error("AI分析结果格式生成错误");
                BICommonCode.handleChartUpdate(chartId, "AI分析结果格式生成错误");
                return;
            }
            String genChart = splits[1].trim();
            String genResult = splits[2].trim();
            //当执行完成后，修改chart状态为 succeed
            updateChart.setStatus("succeed");
            updateChart.setGenChart(genChart);
//            log.info("图表信息：" + genChart);
            updateChart.setGenResult(genResult);
//            log.info("结论信息：" + genResult);
            b = chartService.updateById(updateChart);
//            log.error("更新图表信息" + b);
//            log.error("updateChart" + updateChart);
            if (!b) {
                //再次修改数据库，将状态修改为 failed 失败
                log.error("更新图表失败2");
                BICommonCode.handleChartUpdate(chartId, "更新图表状态失败");
            }
        }, threadPoolExecutor);
        BIResponse biResponse = BIResponse.builder()
                .chartId(chartId)
                .build();
        return ResultUtils.success(biResponse);
    }

    /**
     * 数据分析（MQ）
     *
     * @param multipartFile
     * @param genChartByAIRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/Async/MQ")
    public BaseResponse<BIResponse> geChartByAIAsyncMQ(@RequestPart("file") MultipartFile multipartFile, GenChartByAIRequest genChartByAIRequest, HttpServletRequest request) {
        String name = genChartByAIRequest.getName();
        String goal = genChartByAIRequest.getGoal();
        String chartType = genChartByAIRequest.getChartType();
        String aiType = genChartByAIRequest.getAiType();
        // 校验参数
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");
        ThrowUtils.throwIf(StringUtils.isBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "图表名字过长");
        ThrowUtils.throwIf(StringUtils.isBlank(aiType), ErrorCode.PARAMS_ERROR, "请选择Ai类型");
        //文件校验
        FileVerification(multipartFile);

        User loginUser = userService.getLoginUser(request);
        //限流判断，每个用户一个限流器
        redisLimiterManager.doRateLimit("genChartByAI_" + String.valueOf(loginUser.getId()));
        //压缩后的数据
        String csvData = ExcelUtils.excelToCsv(multipartFile);

        //插入到数据库
        Chart chart = new Chart();
        chart.setGoal(goal);
        chart.setName(name);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setStatus("wait");
        chart.setUserId(loginUser.getId());
        boolean isSave = chartService.save(chart);
        ThrowUtils.throwIf(!isSave, ErrorCode.SYSTEM_ERROR, "图表保存错误");

        //todo 处理任务队列满了后的情况，添加 RejectedExecutionHandler handler 的拒绝策略（暂时不处理）
        long chartId = chart.getId();
        String userRole = loginUser.getUserRole();
        seadMessageMQ seadMessageMQ = new seadMessageMQ();
        seadMessageMQ.setId(chartId);
        seadMessageMQ.setAiType(aiType);
        seadMessageMQ.setUserRole(userRole);
        String jsonStr = JSONUtil.toJsonStr(seadMessageMQ);
        bIMessageSender.sendMessage(jsonStr);
        BIResponse biResponse = BIResponse.builder()
                .chartId(chartId)
                .build();
        return ResultUtils.success(biResponse);
    }

//    /**
//     * 数据分析（流式）
//     *
//     * @param multipartFile
//     * @param genChartByAIRequest
//     * @param request
//     * @return
//     */
//    @PostMapping("/gen/sse")
//    public SseEmitter geChartByAISSE(@RequestPart("file") MultipartFile multipartFile, GenChartByAIRequest genChartByAIRequest, HttpServletRequest request) {
//        String name = genChartByAIRequest.getName();
//        String goal = genChartByAIRequest.getGoal();
//        String chartType = genChartByAIRequest.getChartType();
//        String AiType = genChartByAIRequest.getAiType();
//        // 校验参数
//        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");
//        ThrowUtils.throwIf(StringUtils.isBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "图表名字过长");
//        ThrowUtils.throwIf(StringUtils.isBlank(AiType), ErrorCode.PARAMS_ERROR, "请选择Ai类型");
//        //文件校验
//        FileVerification(multipartFile);
//
//        User loginUser = userService.getLoginUser(request);
//        //限流判断，每个用户一个限流器
//        redisLimiterManager.doRateLimit("genChartByAI_" + loginUser.getId());
//
//        String csvData = ExcelUtils.excelToCsv(multipartFile);
//        // 用户输入
//        StringBuilder userInput = getUserInput(goal, chartType, csvData);
//        //调用AI
//        SseEmitter sseEmitter = new SseEmitter();
//        String result = aiManager.doChat(AiType, userInput.toString());
//        String[] splits = result.split("&&&&&");
//        ThrowUtils.throwIf(splits.length > 3, ErrorCode.SYSTEM_ERROR, "AI生成错误");
//        String genChart = splits[1].trim();
//        String genResult = splits[2].trim();
//        log.info("SSE的sseEmitter：" + sseEmitter);
//        log.info("SSE的genChart：" + genChart);
//        log.info("SSE的genResult：" + genResult);
////        //插入到数据库
////        Chart chart = new Chart();
////        chart.setGoal(goal);
////        chart.setName(name);
////        chart.setChartData(csvData);
////        chart.setChartType(chartType);
////        chart.setStatus("succeed");
////        chart.setGenChart(genChart);
////        chart.setGenResult(genResult);
////        chart.setUserId(loginUser.getId());
////        boolean isSave = chartService.save(chart);
////        ThrowUtils.throwIf(!isSave, ErrorCode.SYSTEM_ERROR, "图表保存错误");
//        BIResponse biResponse = BIResponse.builder()
////                .chartId(chart.getId())
//                .genChart(genChart)
//                .genResult(genResult)
//                .build();
//        return null;
//    }

    @PostMapping("/retry")
    public BaseResponse<Boolean> retryChart(@RequestBody ChartRetryRequest chartRetryRequest) {
        ThrowUtils.throwIf(chartRetryRequest == null, ErrorCode.PARAMS_ERROR, "数据为空");
        chartService.retry(chartRetryRequest);
        return ResultUtils.success(true);
    }

    /**
     * 文件校验
     *
     * @param multipartFile
     */
    public void FileVerification(MultipartFile multipartFile) {
        //文件校验
        String originalFilename = multipartFile.getOriginalFilename(); //文件名
        long size = multipartFile.getSize(); //文件大小
        //校验文件的大小
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件超过1M");
        //校验文件的类型
        String suffix = FileUtil.getSuffix(originalFilename);
        List<String> suffixList = Arrays.asList("xlsx", "xls");
        ThrowUtils.throwIf(!suffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件类型不支持");
    }

    /**
     * 构造用户输入
     *
     * @param goal
     * @param chartType
     * @return
     */
    public StringBuilder getUserInput(String goal, String chartType, String csvData) {
        StringBuilder userInput = new StringBuilder();
        if (StrUtil.isNotBlank(chartType)) {
            goal += "，请使用" + chartType;
        }
        userInput.append("分析目标：").append(goal).append("\n");
        //压缩后的数据
        userInput.append("数据：").append(csvData).append("\n");
        return userInput;
    }


    public void handleChartUpdate(long chartId, String execMessage) {
        Chart updateChart = new Chart();
        updateChart.setId(chartId);
        updateChart.setExecMessage(execMessage);
        updateChart.setStatus("failed");
        boolean result = chartService.updateById(updateChart);
        if (!result) {
            log.error("更新图表状态失败：" + chartId + "失败信息" + execMessage);
        }
    }

    // endregion

    /**
     * 获取查询包装类
     *
     * @param chartQueryRequest
     * @return
     */

    private QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return queryWrapper;
        }
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();
        Long id = chartQueryRequest.getId();
        Long userId = chartQueryRequest.getUserId();
        String goal = chartQueryRequest.getGoal();
        String name = chartQueryRequest.getName();
        String chartType = chartQueryRequest.getChartType();

        // 拼接查询条件
        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(goal), "goal", goal);
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.eq(StringUtils.isNotBlank(chartType), "chartType", chartType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        return queryWrapper;
    }

}
