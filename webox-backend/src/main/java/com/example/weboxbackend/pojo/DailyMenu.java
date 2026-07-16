package com.example.weboxbackend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyMenu  extends BaseEntity{
    private LocalDate today; // 菜单对应的当前日期
    private MenuItemWithPrice menuItem;//菜品集合
}
