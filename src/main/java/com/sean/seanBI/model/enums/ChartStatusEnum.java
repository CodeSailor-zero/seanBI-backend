package com.sean.seanBI.model.enums;

import org.apache.commons.lang3.ObjectUtils;

/**
 * @author sean
 * @Date 2025/07/14
 * 图表生成状态枚举
 */
public enum ChartStatusEnum {
    Wait("等待","wait"),
    Running("运行中","running"),
    Success("成功","success"),
    Failed("失败","failed");
    private final String text;

    private final String status;
    ChartStatusEnum(String text, String status) {
        this.text = text;
        this.status = status;
    }

    public String getText() {
        return text;
    }

    public String getStatus() {
        return status;
    }

    /**
     * 根据 status 获取枚举
     *
     * @param status
     * @return
     */
    public static ChartStatusEnum getEnumByStatus(String status) {
        if (ObjectUtils.isEmpty(status)) {
            return null;
        }
        for (ChartStatusEnum chartStatusEnum : ChartStatusEnum.values()) {
            if (chartStatusEnum.status.equals(status)) {
                return chartStatusEnum;
            }
        }
        return null;
    }
}
