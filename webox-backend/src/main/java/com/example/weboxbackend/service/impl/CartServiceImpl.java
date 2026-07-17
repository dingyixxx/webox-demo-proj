package com.example.weboxbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.weboxbackend.dto.CartItemRequest;
import com.example.weboxbackend.dto.UpdateQuantityRequest;
import com.example.weboxbackend.exception.BusinessException;
import com.example.weboxbackend.mapper.CartMapper;
import com.example.weboxbackend.pojo.Cart;
import com.example.weboxbackend.service.CartService;
import com.example.weboxbackend.vo.CartVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    private final CartMapper cartMapper;

    public CartServiceImpl(CartMapper cartMapper) {
        this.cartMapper = cartMapper;
    }

    @Override
    public List<CartVO> getCart(Long userId) {
        return cartMapper.selectUserCart(userId);
    }

    @Override
    public CartVO addItem(Long userId, CartItemRequest request) {
        if (request.getQuantity() < 1) {
            throw new BusinessException(400, "数量不能小于1");
        }

        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId)
                .eq(Cart::getMenuItemId, request.getMenuItemId())
                .eq(Cart::getIsDeleted, 0);
        Cart existing = cartMapper.selectOne(wrapper);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + request.getQuantity());
            existing.setUpdatedBy(String.valueOf(userId));
            existing.setUpdatedAt(LocalDateTime.now());
            cartMapper.updateById(existing);
        } else {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setMenuItemId(request.getMenuItemId());
            cart.setQuantity(request.getQuantity());
            cart.setIsDeleted(0);
            cart.setCreatedBy(String.valueOf(userId));
            cart.setUpdatedBy(String.valueOf(userId));
            cart.setCreatedAt(LocalDateTime.now());
            cart.setUpdatedAt(LocalDateTime.now());
            cartMapper.insert(cart);
        }

        List<CartVO> cartList = cartMapper.selectUserCart(userId);
        return cartList.stream()
                .filter(v -> v.getMenuItemId().equals(request.getMenuItemId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(500, "添加失败"));
    }

    @Override
    public CartVO updateItem(Long userId, Long cartItemId, UpdateQuantityRequest request) {
        if (request.getQuantity() < 1) {
            throw new BusinessException(400, "数量不能小于1");
        }

        Cart cart = cartMapper.selectById(cartItemId);
        if (cart == null || cart.getIsDeleted() == 1 || !cart.getUserId().equals(userId)) {
            throw new BusinessException(404, "购物车项不存在");
        }

        cart.setQuantity(request.getQuantity());
        cart.setUpdatedBy(String.valueOf(userId));
        cart.setUpdatedAt(LocalDateTime.now());
        cartMapper.updateById(cart);

        List<CartVO> cartList = cartMapper.selectUserCart(userId);
        return cartList.stream()
                .filter(v -> v.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(500, "更新失败"));
    }

    @Override
    public void removeItem(Long userId, Long cartItemId) {
        Cart cart = cartMapper.selectById(cartItemId);
        if (cart == null || cart.getIsDeleted() == 1 || !cart.getUserId().equals(userId)) {
            throw new BusinessException(404, "购物车项不存在");
        }

        cart.setIsDeleted(1);
        cart.setUpdatedBy(String.valueOf(userId));
        cart.setUpdatedAt(LocalDateTime.now());
        cartMapper.updateById(cart);
    }
}
