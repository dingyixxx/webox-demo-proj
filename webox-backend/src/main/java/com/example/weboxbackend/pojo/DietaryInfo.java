package com.example.weboxbackend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DietaryInfo extends BaseEntity{
    private List<String> allergens; // 过敏原标签（如 ["peanut", "dairy"]）
    private int flavorSpicinessLevel;   // 辣度（高90, 中50, 低10）
    private int flavorTasteLevel;       // 口味（重口90, 适中50, 清淡10）
    private int flavorProteinLevel; // 蛋白含量（高90, 中50, 低10）
    private int flavorFatLevel;     // 脂肪含量（高90, 中50, 低10）
}
