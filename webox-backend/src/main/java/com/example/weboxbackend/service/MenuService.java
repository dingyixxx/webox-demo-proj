package com.example.weboxbackend.service;

import com.example.weboxbackend.vo.MenuVO;

import java.util.List;

public interface MenuService {
    List<MenuVO> getMenuList(String category);

    MenuVO getMenuDetail(Long menuItemId);
}
