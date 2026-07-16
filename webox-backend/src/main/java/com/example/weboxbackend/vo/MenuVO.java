package com.example.weboxbackend.vo;

import com.example.weboxbackend.pojo.DailyMenu;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class MenuVO extends DailyMenu {
    public List<String> allergens; // 过敏原标签
}
