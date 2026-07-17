package com.example.weboxbackend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecommendationVO {
    private Long menuItemId;        // 菜品 ID
    private String name;            // 菜品名称
    private String description;     // 菜品描述
    private String image;           // 图片 URL
    private String category;        // 所属分类
    private List<String> allergens; // 过敏原标签
    private int price;              // 单价
    private String reason;          // 推荐理由
}
