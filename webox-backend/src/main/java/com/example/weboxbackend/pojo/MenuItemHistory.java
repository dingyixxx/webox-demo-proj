package com.example.weboxbackend.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_menu_item_history")
public class MenuItemHistory extends BaseEntity{
    private Long menuItemId;          // 菜品 ID
    private int price;       // 单价
    private LocalDate validFromDate; // 生效起始日期
}
