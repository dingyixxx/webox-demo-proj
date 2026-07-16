package com.example.weboxbackend.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.weboxbackend.pojo.DailyMenu;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

@Mapper
public interface DailyMenuMapper extends BaseMapper<DailyMenu> {

    @Delete("DELETE FROM t_daily_menu WHERE today = #{today} AND is_deleted = 0")
    int deleteByToday(@Param("today") LocalDate today);
}
