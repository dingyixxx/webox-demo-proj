package com.example.weboxbackend.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_order_item")
public class OrderItem extends BaseEntity{
    private Long orderId;     // 订单 ID
    private Long userId;          // 下单用户
    private Long menuItemId;  // 菜品 ID
    private String name;        // 菜品名称（冗余快照）
    private Integer price;       // 下单时单价（快照）
    private Integer quantity;   // 数量
}
