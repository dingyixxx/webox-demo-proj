package com.example.weboxbackend.dto;


import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateOrderRequest {
    private LocalDate deliveryDate;    // 配送日期
    private String mealPeriod;         // "lunch" | "dinner"
    private String deliveryAddress;    // 配送地址
}
