package com.example.weboxbackend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MenuItem  extends DietaryInfo{
    private String name;        // 菜品名称
    private String description; // 菜品描述
    private String image;       // 图片 URL
}
