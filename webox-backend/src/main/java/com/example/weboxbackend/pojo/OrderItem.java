package com.example.weboxbackend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem extends BaseEntity{
    private String orderId;     // 订单用户
    private String menuItemId;  // 菜品 ID
    private String name;        // 菜品名称（冗余快照）
    private Integer price;       // 下单时单价（快照）
    private Integer quantity;   // 数量
}
