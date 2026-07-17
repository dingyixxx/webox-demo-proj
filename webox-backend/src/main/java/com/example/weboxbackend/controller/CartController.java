package com.example.weboxbackend.controller;

import com.example.weboxbackend.dto.CartItemRequest;
import com.example.weboxbackend.dto.Result;
import com.example.weboxbackend.dto.UpdateQuantityRequest;
import com.example.weboxbackend.service.CartService;
import com.example.weboxbackend.vo.CartVO;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public Result<List<CartVO>> getCart(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<CartVO> list = cartService.getCart(userId);
        return Result.success(list);
    }

    @PostMapping("/items")
    public Result<CartVO> addItem(HttpServletRequest request, @RequestBody CartItemRequest cartItemRequest) {
        Long userId = (Long) request.getAttribute("userId");
        CartVO vo = cartService.addItem(userId, cartItemRequest);
        return Result.success(vo);
    }

    @PutMapping("/items/{id}")
    public Result<CartVO> updateItem(HttpServletRequest request,
                                     @PathVariable Long id,
                                     @RequestBody UpdateQuantityRequest updateQuantityRequest) {
        Long userId = (Long) request.getAttribute("userId");
        CartVO vo = cartService.updateItem(userId, id, updateQuantityRequest);
        return Result.success(vo);
    }

    @DeleteMapping("/items/{id}")
    public Result<Void> removeItem(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        cartService.removeItem(userId, id);
        return Result.success();
    }
}
