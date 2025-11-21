package com.itdonghuang.service;

import com.itdonghuang.entity.BatteryData;
import com.itdonghuang.entity.BatteryStatusRecord;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * 电池服务接口
 */
public interface BatteryService {
    
    /**
     * 处理电池数据
     * 只处理VID、PID、V、C字段，并且需要判断数据是否变化
     * @param vid 车辆ID
     * @param pid 电池ID
     * @param voltage 电压
     * @param capacity 电量
     */
    void handleBatteryData(String vid, String pid, Double voltage, Double capacity);

    /**
     * 处理电池状态异常
     * @param vid 车辆ID
     * @param status 状态码
     * @param time 异常时间
     */
    void handleBatteryStatus(String vid, Integer status, LocalTime time);

    /**
     * 获取电池历史数据
     * @param pid 电池ID
     * @return 电池历史数据列表
     */
    List<BatteryData> getBatteryHistory(String pid);

    /**
     * 获取状态历史记录
     * @param status 状态码（可选）
     * @return 状态历史记录列表
     */
    List<BatteryStatusRecord> getStatusHistory(Integer status);

    /**
     * 获取电池状态历史记录
     * @param pid 电池ID
     * @return 状态历史记录列表，包含PID、状态和时间信息
     */
    List<Map<String, Object>> getBatteryStatusHistory(String pid);

    /**
     * 获取所有电池状态历史记录
     * @return 状态历史记录列表，包含PID、状态描述和时间信息
     */
    List<Map<String, Object>> getAllBatteryStatusHistory();
} 