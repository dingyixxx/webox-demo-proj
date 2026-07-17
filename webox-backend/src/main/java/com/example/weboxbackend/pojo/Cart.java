package com.example.weboxbackend.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_cart")
public class Cart extends BaseEntity {
    private Long userId;       // 用户 ID
    private Long menuItemId;   // 菜品 ID
    private int quantity;      // 数量
}
