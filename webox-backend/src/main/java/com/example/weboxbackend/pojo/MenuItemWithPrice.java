package com.example.weboxbackend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MenuItemWithPrice extends MenuItem{
    private Integer price;       // 单价

}
