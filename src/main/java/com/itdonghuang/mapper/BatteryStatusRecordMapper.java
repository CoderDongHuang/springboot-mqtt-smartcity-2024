package com.itdonghuang.mapper;

import com.itdonghuang.entity.BatteryStatusRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.time.LocalDateTime;

@Mapper
public interface BatteryStatusRecordMapper {
    // 插入状态记录
    int insert(BatteryStatusRecord record);
    
    // 根据状态类型查询记录
    List<BatteryStatusRecord> findByStatus(@Param("status") Integer status);
    
    // 查询所有记录
    List<BatteryStatusRecord> findAll();
    
    // 根据电池ID查询记录
    List<BatteryStatusRecord> findByPid(@Param("pid") String pid);

    // 检查异常状态数据是否重复
    boolean checkDuplicate(@Param("PID") String PID, @Param("status") Integer status, @Param("time") LocalDateTime time);
} 