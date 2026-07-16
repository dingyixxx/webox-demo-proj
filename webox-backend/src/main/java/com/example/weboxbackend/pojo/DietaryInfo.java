package com.example.weboxbackend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DietaryInfo extends BaseEntity{
    private String category;    // 偏好分类（如 "chinese", "japanese", "western", "salad"）
    private List<String> allergens; // 过敏原标签（如 ["peanut", "dairy"]）
    private String flavorSpiciness;   // 辣度（高90, 中50, 低10）
    private String flavorTaste;       // 甜口/咸口（sweet, salty）
    private String flavorProteinLevel; // 蛋白含量（高90, 中50, 低10）
    private String flavorFatLevel;     // 脂肪含量（高90, 中50, 低10）
}
