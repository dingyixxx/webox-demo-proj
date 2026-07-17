package com.example.weboxbackend.service;

import com.example.weboxbackend.dto.CreateOrderRequest;
import com.example.weboxbackend.vo.OrderVO;

import java.util.List;

public interface OrderService {
    OrderVO createOrder(Long userId, CreateOrderRequest request);

    List<OrderVO> getOrderList(Long userId);

    OrderVO getOrderDetail(Long userId, Long orderId);
}
