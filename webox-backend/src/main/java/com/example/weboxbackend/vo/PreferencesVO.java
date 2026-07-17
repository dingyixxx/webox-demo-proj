package com.example.weboxbackend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PreferencesVO {
    private List<String> allergens;           // 过敏原标签
    private List<String> cuisinePreferences;  // 菜系偏好
    private int spicinessLevel;               // 辣度（高90, 中50, 低10）
    private int tasteLevel;                   // 口味（重口90, 适中50, 清淡10）
    private int proteinLevel;                 // 蛋白含量（高90, 中50, 低10）
    private int fatLevel;                     // 脂肪含量（高90, 中50, 低10）
    private int preferredMinPrice;            // 偏好起始价格（分）
    private int preferredMaxPrice;            // 偏好终止价格（分）

}
