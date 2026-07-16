package com.example.weboxbackend.controller;


import com.example.weboxbackend.vo.MenuVO;
import com.example.weboxbackend.dto.Result;
import com.example.weboxbackend.service.MenuService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public Result<List<MenuVO>> getMenuList(@RequestParam(required = false) String category) {
        List<MenuVO> list = menuService.getMenuList(category);
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<MenuVO> getMenuDetail(@PathVariable Long id) {
        MenuVO menu = menuService.getMenuDetail(id);
        return Result.success(menu);
    }
}
