package com.example.weboxbackend.service.impl;

import com.example.weboxbackend.vo.MenuVO;
import com.example.weboxbackend.exception.BusinessException;
import com.example.weboxbackend.mapper.DailyMenuMapper;
import com.example.weboxbackend.service.MenuService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class MenuServiceImpl implements MenuService {

    private final DailyMenuMapper dailyMenuMapper;

    public MenuServiceImpl(DailyMenuMapper dailyMenuMapper) {
        this.dailyMenuMapper = dailyMenuMapper;
    }

    @Override
    public List<MenuVO> getMenuList(String category) {
        return dailyMenuMapper.selectTodayMenu(LocalDate.now(), category);
    }

    @Override
    public MenuVO getMenuDetail(Long menuItemId) {
        MenuVO menu = dailyMenuMapper.selectTodayMenuById(LocalDate.now(), menuItemId);
        if (menu == null) {
            throw new BusinessException(404, "菜品不存在或今日未上架");
        }
        return menu;
    }
}
