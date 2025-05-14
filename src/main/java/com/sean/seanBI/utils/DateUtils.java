package com.sean.seanBI.utils;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2024/10/11
 **/
@Component
public class DateUtils {
    /**
     * 获取当前月份与当月首日的天数差
     * @return
     */
    public static int gap() {
        // 获取当前日期
        LocalDate today = LocalDate.now();
        // 获取当前月份的第一天
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        // 计算当前日期与本月第一天之间的天数差
        long days = ChronoUnit.DAYS.between(firstDayOfMonth, today);
        return (int) days;
    }
}
