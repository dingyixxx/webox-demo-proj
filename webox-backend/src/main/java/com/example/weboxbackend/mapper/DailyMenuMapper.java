package com.example.weboxbackend.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.example.weboxbackend.pojo.DailyMenu;
import com.example.weboxbackend.vo.MenuVO;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface DailyMenuMapper extends BaseMapper<DailyMenu> {

    @Delete("DELETE FROM t_daily_menu WHERE today = #{today} AND is_deleted = 0")
    int deleteByToday(@Param("today") LocalDate today);

    @Select("<script>SELECT dm.id, dm.today, dm.menu_item_id AS menuItemId, dm.price, " +
            "mi.name, mi.description, mi.image, mi.category, mi.allergens " +
            "FROM t_daily_menu dm " +
            "JOIN t_menu_item mi ON dm.menu_item_id = mi.id AND mi.is_deleted = 0 " +
            "WHERE dm.today = #{today} AND dm.is_deleted = 0 " +
            "<if test='category != null and category != \"\"'>" +
            "AND mi.category = #{category} " +
            "</if>" +
            "ORDER BY dm.id</script>")
    List<MenuVO> selectTodayMenu(@Param("today") LocalDate today, @Param("category") String category);

    @Results({
            @Result(column = "allergens", property = "allergens", typeHandler = JacksonTypeHandler.class)
    })
    @Select("SELECT dm.id, dm.today, dm.menu_item_id AS menuItemId, dm.price, " +
            "mi.name, mi.description, mi.image, mi.category, mi.allergens " +
            "FROM t_daily_menu dm " +
            "JOIN t_menu_item mi ON dm.menu_item_id = mi.id AND mi.is_deleted = 0 " +
            "WHERE dm.today = #{today} AND dm.menu_item_id = #{menuItemId} AND dm.is_deleted = 0")
    MenuVO selectTodayMenuById(@Param("today") LocalDate today, @Param("menuItemId") Long menuItemId);

}
