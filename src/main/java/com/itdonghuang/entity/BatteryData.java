package com.itdonghuang.entity;

import lombok.Data;

/**
 * 电池数据实体类
 */
@Data
public class BatteryData {
    private Long id;
    private String VID;
    private String PID;
    private Double T;  // 温度
    private Double C;  // 电量
} 