package com.example.weboxbackend.dto;

import lombok.Data;

@Data
public class CartItemRequest {
    private Long menuItemId;
    private int quantity;
}