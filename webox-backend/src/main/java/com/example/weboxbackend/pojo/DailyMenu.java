package com.example.weboxbackend.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_daily_menu")
public class DailyMenu extends BaseEntity{
    private LocalDate today; // 菜单对应的当前日期
    private Long menuItemId;          // 菜品 ID

    @TableField(exist = false)
    private String name;        // 菜品名称

    @TableField(exist = false)
    private String description; // 菜品描述

    @TableField(exist = false)
    private String image;       // 图片 URL

    @TableField(exist = false)
    private String category;    // 所属分类（单选, 如"chinese"或"japanese"）

    private int price;       // 单价
}
