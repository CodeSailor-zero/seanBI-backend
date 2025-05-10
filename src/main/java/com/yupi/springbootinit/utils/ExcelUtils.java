package com.yupi.springbootinit.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2024/12/11
 * Execl 工具类
 **/
@Slf4j
public class ExcelUtils {
    /**
     * excel转csv
     * @param multipartFile
     * @return
     */
    public static String excelToCsv(MultipartFile multipartFile) {
//        File file = null;
//        try {
//            file = ResourceUtils.getFile("classpath:网站数据.xlsx");
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        }
        List<Map<Integer, String>> list = null;
        try {
            list = EasyExcel.read(multipartFile.getInputStream())
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet()
                    .headRowNumber(0)
                    .doReadSync();
        } catch (IOException e) {
            log.error("excelToCsv error", e);
        }
        if (CollUtil.isEmpty(list)) {
            return "";
        }
        //转换为 csv
        StringBuilder stringBuilder = new StringBuilder();
        //读取表头，为什么要使用LinkedHashMap，因为要保证顺序【LinkedHashMap是顺序的】
        //并且 list 本身的输出就是 LinkedHashMap
        LinkedHashMap<Integer, String> headerMap = (LinkedHashMap) list.get(0);
        List<String> headerList = headerMap.values()
                .stream()
                .filter(ObjectUtils::isNotEmpty)
                .collect(Collectors.toList());
        stringBuilder.append(StrUtil.join(",", headerList)).append("\n");
        //读取数据内容
        for (int i = 1; i < list.size(); i++) {
            LinkedHashMap<Integer, String> valueMap = (LinkedHashMap) list.get(i);
            List<String> valueList = valueMap.values()
                    .stream()
                    .filter(ObjectUtils::isNotEmpty)
                    .collect(Collectors.toList());
            stringBuilder.append(StrUtil.join(",", valueList)).append("\n");
        }
        return stringBuilder.toString();
    }

    public static void main(String[] args) {
        excelToCsv(null);
    }
}
