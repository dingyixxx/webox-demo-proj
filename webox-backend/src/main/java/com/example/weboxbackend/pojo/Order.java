package com.example.weboxbackend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order extends BaseEntity{
    private String userId;          // 下单用户
    private List<OrderItem> items;  // 订单菜品列表
    private Integer totalAmount;     // 订单总额
    private LocalDate deliveryDate;    // 配送日期 "YYYY-MM-DD"
    private String mealPeriod;      // "lunch" | "dinner"
    private String deliveryAddress; // 配送地址
    private String status;          // "pending" | "confirmed" | "completed" | "cancelled"
}
