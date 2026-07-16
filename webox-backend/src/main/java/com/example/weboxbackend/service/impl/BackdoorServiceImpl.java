package com.example.weboxbackend.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.weboxbackend.mapper.DailyMenuMapper;
import com.example.weboxbackend.mapper.MenuItemHistoryMapper;
import com.example.weboxbackend.mapper.MenuItemMapper;
import com.example.weboxbackend.pojo.DailyMenu;
import com.example.weboxbackend.pojo.MenuItem;
import com.example.weboxbackend.pojo.MenuItemHistory;
import com.example.weboxbackend.service.BackdoorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BackdoorServiceImpl implements BackdoorService {

    private final MenuItemMapper menuItemMapper;
    private final MenuItemHistoryMapper menuItemHistoryMapper;
    private final DailyMenuMapper dailyMenuMapper;

    public BackdoorServiceImpl(MenuItemMapper menuItemMapper,
                               MenuItemHistoryMapper menuItemHistoryMapper,
                               DailyMenuMapper dailyMenuMapper) {
        this.menuItemMapper = menuItemMapper;
        this.menuItemHistoryMapper = menuItemHistoryMapper;
        this.dailyMenuMapper = dailyMenuMapper;
    }

    @Transactional
    @Override
    public void syncDailyMenu() {
        List<MenuItem> menuItems = menuItemMapper.selectList(
                new LambdaQueryWrapper<MenuItem>().eq(MenuItem::getIsDeleted, 0));

        List<MenuItemHistory> histories = menuItemHistoryMapper.selectList(
                new LambdaQueryWrapper<MenuItemHistory>().eq(MenuItemHistory::getIsDeleted, 0));

        Map<Long, List<MenuItemHistory>> historyMap = histories.stream()
                .collect(Collectors.groupingBy(MenuItemHistory::getMenuItemId));

        LocalDate today = LocalDate.now();

        for (int i = 0; i < 20; i++) {
            LocalDate date = today.plusDays(i);

            dailyMenuMapper.deleteByToday(date);

            for (MenuItem item : menuItems) {
                List<MenuItemHistory> itemHistories = historyMap.get(item.getId());
                if (itemHistories == null) {
                    continue;
                }

                MenuItemHistory applicable = itemHistories.stream()
                        .filter(h -> !h.getValidFromDate().isAfter(date))
                        .max((a, b) -> a.getValidFromDate().compareTo(b.getValidFromDate()))
                        .orElse(null);

                if (applicable == null) {
                    continue;
                }

                DailyMenu dailyMenu = new DailyMenu();
                dailyMenu.setToday(date);
                dailyMenu.setMenuItemId(item.getId());
                dailyMenu.setPrice(applicable.getPrice());
                dailyMenu.setIsDeleted(0);
                dailyMenu.setCreatedBy("system");
                dailyMenu.setUpdatedBy("system");
                dailyMenu.setCreatedAt(LocalDateTime.now());
                dailyMenu.setUpdatedAt(LocalDateTime.now());

                dailyMenuMapper.insert(dailyMenu);
            }
        }
    }
}
