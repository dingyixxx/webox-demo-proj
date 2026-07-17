package com.example.weboxbackend.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.weboxbackend.mapper.DailyMenuMapper;
import com.example.weboxbackend.mapper.MenuItemHistoryMapper;
import com.example.weboxbackend.mapper.MenuItemMapper;
import com.example.weboxbackend.pojo.DailyMenu;
import com.example.weboxbackend.pojo.MenuItem;
import com.example.weboxbackend.pojo.MenuItemHistory;
import com.example.weboxbackend.service.BackdoorService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.logging.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BackdoorServiceImpl implements BackdoorService {
    private final MenuItemMapper menuItemMapper;
    private final MenuItemHistoryMapper menuItemHistoryMapper;
    private final DailyMenuMapper dailyMenuMapper;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${shenduqiusuo:}")
    private String shenduqiusuo;

    public BackdoorServiceImpl(MenuItemMapper menuItemMapper,
                               MenuItemHistoryMapper menuItemHistoryMapper,
                               DailyMenuMapper dailyMenuMapper,
                               RestTemplate restTemplate,
                               ObjectMapper objectMapper) {
        this.menuItemMapper = menuItemMapper;
        this.menuItemHistoryMapper = menuItemHistoryMapper;
        this.dailyMenuMapper = dailyMenuMapper;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    @Override
    @Scheduled(fixedRate = 6 * 60 * 60 * 1000)
    public void syncDailyMenu() {
        System.out.println("Syncing daily menu...");
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





    @Override
    public void analyzeAndScoreMenuItems() {
        List<MenuItem> menuItems = menuItemMapper.selectList(
                new LambdaQueryWrapper<MenuItem>().eq(MenuItem::getIsDeleted, 0));

        StringBuilder allResults = new StringBuilder();
        allResults.append("=== DeepSeek 菜品评分结果 ===\n");
        allResults.append("生成时间：").append(LocalDateTime.now()).append("\n\n");

        for (MenuItem item : menuItems) {
            try {
                String prompt = String.format(
                        "请根据以下菜品信息，判断其辣度、口味、蛋白含量、脂肪含量四个维度的评分。\n" +
                                "菜品名称：%s\n" +
                                "菜品描述：%s\n\n" +
                                "评分规则：\n" +
                                "- 辣度（spicinessLevel）：不辣=10，微辣/适中=50，辣/重辣=90\n" +
                                "- 口味（tasteLevel）：清淡=10，适中=50，重口=90\n" +
                                "- 蛋白含量（proteinLevel）：低=10，中=50，高=90\n" +
                                "- 脂肪含量（fatLevel）：低=10，中=50，高=90\n\n" +
                                "要求：每个维度只能返回 10、50 或 90，不能有其他值。\n" +
                                "请严格按照以下 JSON 格式返回，不要返回其他内容：\n" +
                                "{\"spicinessLevel\": 50, \"tasteLevel\": 50, \"proteinLevel\": 50, \"fatLevel\": 50}",
                        item.getName(), item.getDescription()
                );

                Map<String, Object> message = new HashMap<>();
                message.put("role", "user");
                message.put("content", prompt);

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("model", "deepseek-chat");
                requestBody.put("messages", Collections.singletonList(message));
                requestBody.put("temperature", 0.1);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", "Bearer " + shenduqiusuo);

                HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

                ResponseEntity<String> response = restTemplate.postForEntity(
                        "https://api.deepseek.com/v1/chat/completions",
                        requestEntity,
                        String.class
                );

                String responseBody = response.getBody();
                allResults.append("【").append(item.getName()).append("】\n");
                allResults.append("DeepSeek 原始回复：\n").append(responseBody).append("\n\n");

                JsonNode root = objectMapper.readTree(responseBody);
                String content = root.path("choices").path(0).path("message").path("content").asText();

                JsonNode scoreNode = objectMapper.readTree(content);
                int spiciness = scoreNode.path("spicinessLevel").asInt(50);
                int taste = scoreNode.path("tasteLevel").asInt(50);
                int protein = scoreNode.path("proteinLevel").asInt(50);
                int fat = scoreNode.path("fatLevel").asInt(50);

                spiciness = normalizeScore(spiciness);
                taste = normalizeScore(taste);
                protein = normalizeScore(protein);
                fat = normalizeScore(fat);

                item.setFlavorSpicinessLevel(spiciness);
                item.setFlavorTasteLevel(taste);
                item.setFlavorProteinLevel(protein);
                item.setFlavorFatLevel(fat);
                item.setUpdatedBy("system");
                item.setUpdatedAt(LocalDateTime.now());
                menuItemMapper.updateById(item);

                allResults.append("最终评分：辣度=").append(spiciness)
                        .append(", 口味=").append(taste)
                        .append(", 蛋白=").append(protein)
                        .append(", 脂肪=").append(fat).append("\n\n");

                log.info("菜品[{}]评分完成：辣度={}, 口味={}, 蛋白={}, 脂肪={}",
                        item.getName(), spiciness, taste, protein, fat);

                Thread.sleep(1000);

            } catch (Exception e) {
                log.error("菜品[{}]评分失败：{}", item.getName(), e.getMessage());
                allResults.append("【").append(item.getName()).append("】评分失败：").append(e.getMessage()).append("\n\n");
            }
        }
        try {
            java.nio.file.Files.write(
                    java.nio.file.Paths.get("deepseek-results.txt"),
                    allResults.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8)
            );
            log.info("DeepSeek 评分结果已保存到 deepseek-results.txt");
        } catch (Exception e) {
            log.error("保存评分结果失败：{}", e.getMessage());
        }
    }

// ... existing code ...

    private int normalizeScore(int score) {
        if (score <= 30) return 10;
        if (score <= 70) return 50;
        return 90;
    }
}
