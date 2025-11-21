package com.itdonghuang.mapper;

import com.itdonghuang.entity.BatteryData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 电池数据Mapper接口
 */
@Mapper
public interface BatteryDataMapper {
    
    /**
     * 插入电池数据
     * @param data 电池数据
     * @return 影响行数
     */
    int insert(BatteryData data);
    
    /**
     * 根据电池ID查询历史数据
     * @param pid 电池ID
     * @return 历史数据列表
     */
    List<BatteryData> findByPid(@Param("pid") String pid);
    
    /**
     * 检查数据是否重复
     * @param pid 电池ID
     * @param T 温度
     * @param C 电量
     * @return 是否存在重复数据
     */
    boolean checkDuplicate(@Param("pid") String pid, @Param("T") Double T, @Param("C") Double C);
} 