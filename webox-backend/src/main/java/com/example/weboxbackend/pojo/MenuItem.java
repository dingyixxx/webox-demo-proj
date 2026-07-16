package com.example.weboxbackend.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "t_menu_item", autoResultMap = true)
public class MenuItem extends DietaryInfo{
    private String name;        // 菜品名称
    private String description; // 菜品描述
    private String image;       // 图片 URL
    private String category;    // 所属分类（单选, 如"chinese"或"japanese"）
}
