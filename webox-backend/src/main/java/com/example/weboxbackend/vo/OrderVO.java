package com.example.weboxbackend.vo;

import com.example.weboxbackend.pojo.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class OrderVO extends Order {
    private List<OrderItemVO> items; // 订单菜品列表
}
