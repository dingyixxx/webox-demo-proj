package com.example.weboxbackend.vo;

import com.example.weboxbackend.pojo.Cart;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class CartVO extends Cart {
    private String name;            // 菜品名称
    private String image;           // 图片 URL
    private String category;        // 所属分类
    private List<String> allergens; // 过敏原标签
    private int price;              // 单价
    private int subtotal;           // 小计（price * quantity）
}
