package com.example.weboxbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.example.weboxbackend.vo.CartVO;
import com.example.weboxbackend.dto.CreateOrderRequest;
import com.example.weboxbackend.vo.OrderItemVO;
import com.example.weboxbackend.vo.OrderVO;
import com.example.weboxbackend.exception.BusinessException;
import com.example.weboxbackend.mapper.CartMapper;
import com.example.weboxbackend.mapper.OrderItemMapper;
import com.example.weboxbackend.mapper.OrderMapper;
import com.example.weboxbackend.pojo.Cart;
import com.example.weboxbackend.pojo.Order;
import com.example.weboxbackend.pojo.OrderItem;
import com.example.weboxbackend.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartMapper cartMapper;

    public OrderServiceImpl(OrderMapper orderMapper,
                            OrderItemMapper orderItemMapper,
                            CartMapper cartMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.cartMapper = cartMapper;
    }

    @Override
    @Transactional
    public OrderVO createOrder(Long userId, CreateOrderRequest request) {
        List<CartVO> cartItems = cartMapper.selectUserCart(userId);
        if (cartItems == null || cartItems.isEmpty()) {
            throw new BusinessException(400, "购物车为空，无法下单");
        }

        int totalAmount = cartItems.stream()
                .mapToInt(CartVO::getSubtotal)
                .sum();

        Order order = new Order();
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setDeliveryDate(request.getDeliveryDate());
        order.setMealPeriod(request.getMealPeriod());
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setStatus("pending");
        order.setIsDeleted(0);
        order.setCreatedBy(String.valueOf(userId));
        order.setUpdatedBy(String.valueOf(userId));
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.insert(order);

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartVO item : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setUserId(userId);
            orderItem.setMenuItemId(item.getMenuItemId());
            orderItem.setName(item.getName());
            orderItem.setPrice(item.getPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setIsDeleted(0);
            orderItem.setCreatedBy(String.valueOf(userId));
            orderItem.setUpdatedBy(String.valueOf(userId));
            orderItem.setCreatedAt(LocalDateTime.now());
            orderItem.setUpdatedAt(LocalDateTime.now());
            orderItemMapper.insert(orderItem);
            orderItems.add(orderItem);
        }

        LambdaQueryWrapper<Cart> cartWrapper = new LambdaQueryWrapper<>();
        cartWrapper.eq(Cart::getUserId, userId).eq(Cart::getIsDeleted, 0);
        List<Cart> carts = cartMapper.selectList(cartWrapper);
        for (Cart cart : carts) {
            cart.setIsDeleted(1);
            cart.setUpdatedBy(String.valueOf(userId));
            cart.setUpdatedAt(LocalDateTime.now());
            cartMapper.updateById(cart);
        }

        OrderVO orderVO = new OrderVO();
        orderVO.setId(order.getId());
        orderVO.setUserId(order.getUserId());
        orderVO.setTotalAmount(order.getTotalAmount());
        orderVO.setDeliveryDate(order.getDeliveryDate());
        orderVO.setMealPeriod(order.getMealPeriod());
        orderVO.setDeliveryAddress(order.getDeliveryAddress());
        orderVO.setStatus(order.getStatus());

        List<OrderItemVO> itemVOs = orderItems.stream().map(oi -> {
            OrderItemVO vo = new OrderItemVO();
            vo.setId(oi.getId());
            vo.setMenuItemId(oi.getMenuItemId());
            vo.setName(oi.getName());
            vo.setPrice(oi.getPrice());
            vo.setQuantity(oi.getQuantity());
            vo.setSubtotal(oi.getPrice() * oi.getQuantity());
            return vo;
        }).collect(Collectors.toList());
        orderVO.setItems(itemVOs);

        return orderVO;
    }

    @Override
    public List<OrderVO> getOrderList(Long userId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId)
                .eq(Order::getIsDeleted, 0)
                .orderByDesc(Order::getCreatedAt);
        List<Order> orders = orderMapper.selectList(wrapper);

        return orders.stream().map(order -> {
            OrderVO vo = new OrderVO();
            vo.setId(order.getId());
            vo.setUserId(order.getUserId());
            vo.setTotalAmount(order.getTotalAmount());
            vo.setDeliveryDate(order.getDeliveryDate());
            vo.setMealPeriod(order.getMealPeriod());
            vo.setDeliveryAddress(order.getDeliveryAddress());
            vo.setStatus(order.getStatus());

            LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(OrderItem::getOrderId, order.getId())
                    .eq(OrderItem::getIsDeleted, 0);
            List<OrderItem> items = orderItemMapper.selectList(itemWrapper);

            List<OrderItemVO> itemVOs = items.stream().map(oi -> {
                OrderItemVO itemVO = new OrderItemVO();
                itemVO.setId(oi.getId());
                itemVO.setMenuItemId(oi.getMenuItemId());
                itemVO.setName(oi.getName());
                itemVO.setPrice(oi.getPrice());
                itemVO.setQuantity(oi.getQuantity());
                itemVO.setSubtotal(oi.getPrice() * oi.getQuantity());
                return itemVO;
            }).collect(Collectors.toList());
            vo.setItems(itemVOs);

            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public OrderVO getOrderDetail(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getIsDeleted() == 1 || !order.getUserId().equals(userId)) {
            throw new BusinessException(404, "订单不存在");
        }

        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setUserId(order.getUserId());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setDeliveryDate(order.getDeliveryDate());
        vo.setMealPeriod(order.getMealPeriod());
        vo.setDeliveryAddress(order.getDeliveryAddress());
        vo.setStatus(order.getStatus());

        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, order.getId())
                .eq(OrderItem::getIsDeleted, 0);
        List<OrderItem> items = orderItemMapper.selectList(itemWrapper);

        List<OrderItemVO> itemVOs = items.stream().map(oi -> {
            OrderItemVO itemVO = new OrderItemVO();
            itemVO.setId(oi.getId());
            itemVO.setMenuItemId(oi.getMenuItemId());
            itemVO.setName(oi.getName());
            itemVO.setPrice(oi.getPrice());
            itemVO.setQuantity(oi.getQuantity());
            itemVO.setSubtotal(oi.getPrice() * oi.getQuantity());
            return itemVO;
        }).collect(Collectors.toList());
        vo.setItems(itemVOs);

        return vo;
    }
}
