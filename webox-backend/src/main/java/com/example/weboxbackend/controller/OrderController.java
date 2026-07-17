package com.example.weboxbackend.controller;


import com.example.weboxbackend.dto.CreateOrderRequest;
import com.example.weboxbackend.vo.OrderVO;
import com.example.weboxbackend.dto.Result;
import com.example.weboxbackend.service.OrderService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public Result<OrderVO> createOrder(HttpServletRequest request, @RequestBody CreateOrderRequest createOrderRequest) {
        Long userId = (Long) request.getAttribute("userId");
        OrderVO vo = orderService.createOrder(userId, createOrderRequest);
        return Result.success(vo);
    }

    @GetMapping
    public Result<List<OrderVO>> getOrderList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<OrderVO> list = orderService.getOrderList(userId);
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<OrderVO> getOrderDetail(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        OrderVO vo = orderService.getOrderDetail(userId, id);
        return Result.success(vo);
    }
}
