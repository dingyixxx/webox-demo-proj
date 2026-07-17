package com.example.weboxbackend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemVO {
    private Long id;
    private Long menuItemId;
    private String name;
    private int price;
    private int quantity;
    private int subtotal;
}
