package com.example.weboxbackend.service;

import com.example.weboxbackend.dto.CartItemRequest;
import com.example.weboxbackend.dto.UpdateQuantityRequest;
import com.example.weboxbackend.vo.CartVO;

import java.util.List;

public interface CartService {
    List<CartVO> getCart(Long userId);

    CartVO addItem(Long userId, CartItemRequest request);

    CartVO updateItem(Long userId, Long cartItemId, UpdateQuantityRequest request);

    void removeItem(Long userId, Long cartItemId);
}
