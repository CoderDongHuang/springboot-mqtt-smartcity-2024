package com.itdonghuang.service.impl;

import com.itdonghuang.entity.BatteryData;
import com.itdonghuang.entity.BatteryStatusRecord;
import com.itdonghuang.mapper.BatteryDataMapper;
import com.itdonghuang.mapper.BatteryStatusRecordMapper;
import com.itdonghuang.service.BatteryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

/**
 * 电池服务实现类
 * 处理电池数据和状态相关的业务逻辑
 */
@Service
public class BatteryServiceImpl implements BatteryService {

    private static final Logger logger = LoggerFactory.getLogger(BatteryServiceImpl.class);

    @Autowired
    private BatteryDataMapper batteryDataMapper;
    
    @Autowired
    private BatteryStatusRecordMapper statusRecordMapper;

    /**
     * 处理电池数据
     * @param vid 车辆ID
     * @param pid 电池ID
     * @param T 电池温度
     * @param C 电池电量
     */
    @Override
    public void handleBatteryData(String vid, String pid, Double T, Double C) {
        // 检查数据是否重复
        boolean isDuplicate = batteryDataMapper.checkDuplicate(pid, T, C);
        
        if (!isDuplicate) {
            // 创建新的电池数据对象
            BatteryData batteryData = new BatteryData();
            batteryData.setVID(vid);
            batteryData.setPID(pid);
            batteryData.setT(T);
            batteryData.setC(C);
            
            // 保存数据
            batteryDataMapper.insert(batteryData);
            logger.info("保存新的电池数据 - VID: {}, PID: {}, T: {}, C: {}", vid, pid, T, C);
        } else {
            logger.info("数据未变化，跳过保存 - VID: {}, PID: {}, T: {}, C: {}", vid, pid, T, C);
        }
    }

    /**
     * 处理电池状态异常
     * @param pid 电池ID
     * @param status 状态码
     * @param time 时间
     */
    @Override
    public void handleBatteryStatus(String pid, Integer status, LocalTime time) {
        logger.info("开始处理电池状态异常 - PID: {}, 状态: {}, 时间: {}", pid, status, time);
        
        // 将LocalTime转换为LocalDateTime
        LocalDateTime dateTime = LocalDateTime.now().with(time);
        
        // 检查数据是否重复
        boolean isDuplicate = statusRecordMapper.checkDuplicate(pid, status, dateTime);
        
        if (!isDuplicate) {
            // 创建新的状态记录
            BatteryStatusRecord record = new BatteryStatusRecord();
            record.setPID(pid);
            record.setStatus(status);
            record.setTime(dateTime);
            
            // 保存状态记录
            statusRecordMapper.insert(record);
            logger.info("电池状态记录保存成功 - PID: {}, 状态: {}", pid, status);
        } else {
            logger.info("数据未变化，跳过保存 - PID: {}, 状态: {}, 时间: {}", pid, status, time);
        }
    }

    /**
     * 获取电池历史数据
     * @param pid 电池ID
     * @return 电池数据列表
     */
    @Override
    public List<BatteryData> getBatteryHistory(String pid) {
        logger.info("查询电池历史数据 - PID: {}", pid);
        List<BatteryData> history = batteryDataMapper.findByPid(pid);
        logger.info("查询到 {} 条历史记录", history.size());
        return history;
    }

    /**
     * 获取异常历史记录
     * @param status 状态码
     * @return 状态记录列表
     */
    @Override
    public List<BatteryStatusRecord> getStatusHistory(Integer status) {
        logger.info("查询状态历史记录 - 状态: {}", status);
        List<BatteryStatusRecord> history = status != null ? 
            statusRecordMapper.findByStatus(status) : 
            statusRecordMapper.findAll();
        logger.info("查询到 {} 条状态记录", history.size());
        return history;
    }

    /**
     * 获取电池状态历史记录
     * @param pid 电池ID
     * @return 状态历史记录列表，包含PID、状态和时间信息
     */
    @Override
    public List<Map<String, Object>> getBatteryStatusHistory(String pid) {
        logger.info("查询电池状态历史记录 - PID: {}", pid);
        
        // 查询该电池的所有状态记录
        List<BatteryStatusRecord> records = statusRecordMapper.findByPid(pid);
        logger.info("查询到 {} 条状态记录", records.size());
        
        // 转换为前端需要的格式
        return records.stream()
            .map(record -> {
                Map<String, Object> map = new HashMap<>();
                map.put("PID", record.getPID());
                map.put("status", record.getStatus());
                map.put("time", record.getTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                return map;
            })
            .collect(Collectors.toList());
    }

    /**
     * 获取所有电池状态历史记录
     * @return 状态历史记录列表，包含PID、状态描述和时间信息
     */
    @Override
    public List<Map<String, Object>> getAllBatteryStatusHistory() {
        logger.info("查询所有电池状态历史记录");
        
        // 查询所有状态记录
        List<BatteryStatusRecord> records = statusRecordMapper.findAll();
        logger.info("查询到 {} 条状态记录", records.size());
        
        // 转换为前端需要的格式
        return records.stream()
            .map(record -> {
                Map<String, Object> map = new HashMap<>();
                map.put("PID", record.getPID());
                map.put("time", record.getTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                
                // 将状态码转换为文字描述
                String statusDesc;
                switch (record.getStatus()) {
                    case 1:
                        statusDesc = "电池温度异常";
                        break;
                    case 2:
                        statusDesc = "电池电量过低";
                        break;
                    case 3:
                        statusDesc = "不能到达最近换电站";
                        break;
                    default:
                        statusDesc = "未知状态";
                }
                map.put("status", statusDesc);
                
                return map;
            })
            .collect(Collectors.toList());
    }
} 