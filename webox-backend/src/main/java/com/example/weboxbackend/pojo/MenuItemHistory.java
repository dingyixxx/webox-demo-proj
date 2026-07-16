package com.example.weboxbackend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MenuItemHistory extends BaseEntity{
    private String menuItemId;          // 菜品 ID
    private Integer price;       // 单价
    private LocalDate validFromDate; // 生效起始日期
}
