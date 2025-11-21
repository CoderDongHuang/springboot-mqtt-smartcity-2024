package com.itdonghuang.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 电池状态记录实体类
 */
@Data
public class BatteryStatusRecord {
    private Long id;
    private String PID;
    private Integer status;
    private LocalDateTime time;
} 