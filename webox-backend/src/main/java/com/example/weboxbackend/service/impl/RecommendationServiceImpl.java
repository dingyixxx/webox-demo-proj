package com.example.weboxbackend.service.impl;

import com.example.weboxbackend.dto.RecommendationRequest;
import com.example.weboxbackend.exception.BusinessException;
import com.example.weboxbackend.mapper.CartMapper;
import com.example.weboxbackend.mapper.DailyMenuMapper;
import com.example.weboxbackend.mapper.UserMapper;
import com.example.weboxbackend.pojo.User;
import com.example.weboxbackend.service.RecommendationService;
import com.example.weboxbackend.vo.MenuVO;
import com.example.weboxbackend.vo.RecommendationVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationServiceImpl.class);

    private final DailyMenuMapper dailyMenuMapper;
    private final UserMapper userMapper;
    private final CartMapper cartMapper;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${shenduqiusuo:}")
    private String shenduqiusuo;

    public RecommendationServiceImpl(DailyMenuMapper dailyMenuMapper,
                                     UserMapper userMapper,
                                     CartMapper cartMapper,
                                     RestTemplate restTemplate,
                                     ObjectMapper objectMapper) {
        this.dailyMenuMapper = dailyMenuMapper;
        this.userMapper = userMapper;
        this.cartMapper = cartMapper;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<RecommendationVO> getRecommendations(Long userId, RecommendationRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BusinessException(404, "用户不存在");
        }

        List<MenuVO> todayMenu = dailyMenuMapper.selectTodayMenu(LocalDate.now(), null);
        if (todayMenu == null || todayMenu.isEmpty()) {
            throw new BusinessException(400, "今日暂无菜单");
        }

        List<String> userAllergens = user.getAllergens();
        List<MenuVO> filteredMenu = todayMenu.stream()
                .filter(item -> {
                    if (userAllergens == null || userAllergens.isEmpty()) {
                        return true;
                    }
                    List<String> itemAllergens = item.getAllergens();
                    if (itemAllergens == null || itemAllergens.isEmpty()) {
                        return true;
                    }
                    return Collections.disjoint(userAllergens, itemAllergens);
                })
                .collect(Collectors.toList());

        if (filteredMenu.isEmpty()) {
            throw new BusinessException(400, "没有符合您过敏原要求的菜品");
        }

        String menuJson = buildMenuJson(filteredMenu);

        String prompt = String.format(
                "你是一个智能餐饮推荐助手。根据用户的自然语言需求，从以下菜品列表中推荐 3-5 个最匹配的菜品。\n\n" +
                        "用户偏好设置：\n" +
                        "- 过敏原（需排除）：%s\n" +
                        "- 菜系偏好：%s\n" +
                        "- 辣度偏好：%d（10=不辣，50=微辣，90=辣）\n" +
                        "- 口味偏好：%d（10=清淡，50=适中，90=重口）\n" +
                        "- 蛋白偏好：%d（10=低，50=中，90=高）\n" +
                        "- 脂肪偏好：%d（10=低，50=中，90=高）\n\n" +
                        "用户需求：%s\n\n" +
                        "可选菜品列表：\n%s\n\n" +
                        "请严格按照以下 JSON 格式返回，不要返回其他内容：\n" +
                        "[{\"menuItemId\": 1, \"reason\": \"推荐理由\"}]\n" +
                        "其中 menuItemId 必须是上述列表中的真实 ID，reason 用中文简要说明推荐理由（20字以内）。",
                userAllergens == null ? "无" : String.join("、", userAllergens),
                user.getCategories() == null ? "无偏好" : String.join("、", user.getCategories()),
                user.getFlavorSpicinessLevel(),
                user.getFlavorTasteLevel(),
                user.getFlavorProteinLevel(),
                user.getFlavorFatLevel(),
                request.getPrompt(),
                menuJson
        );

        try {
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "deepseek-chat");
            requestBody.put("messages", Collections.singletonList(message));
            requestBody.put("temperature", 0.3);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + shenduqiusuo);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://api.deepseek.com/v1/chat/completions",
                    requestEntity,
                    String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());
            String content = root.path("choices").path(0).path("message").path("content").asText();

            JsonNode recommendations = objectMapper.readTree(content);

            Map<Long, MenuVO> menuMap = filteredMenu.stream()
                    .collect(Collectors.toMap(MenuVO::getMenuItemId, m -> m));

            List<RecommendationVO> result = new ArrayList<>();
            for (JsonNode node : recommendations) {
                Long menuItemId = node.path("menuItemId").asLong();
                String reason = node.path("reason").asText();

                MenuVO menu = menuMap.get(menuItemId);
                if (menu != null) {
                    RecommendationVO vo = new RecommendationVO();
                    vo.setMenuItemId(menu.getMenuItemId());
                    vo.setName(menu.getName());
                    vo.setDescription(menu.getDescription());
                    vo.setImage(menu.getImage());
                    vo.setCategory(menu.getCategory());
                    vo.setAllergens(menu.getAllergens());
                    vo.setPrice(menu.getPrice());
                    vo.setReason(reason);
                    result.add(vo);
                }
            }

            return result;

        } catch (Exception e) {
            log.error("AI 推荐失败：{}", e.getMessage());
            throw new BusinessException(500, "AI 推荐服务暂时不可用");
        }
    }


    private String buildMenuJson(List<MenuVO> menuList) {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < menuList.size(); i++) {
            MenuVO item = menuList.get(i);
            sb.append("  {\"menuItemId\": ").append(item.getMenuItemId())
                    .append(", \"name\": \"").append(item.getName()).append("\"")
                    .append(", \"category\": \"").append(item.getCategory()).append("\"")
                    .append(", \"spicinessLevel\": ").append(item.getFlavorSpicinessLevel())
                    .append(", \"tasteLevel\": ").append(item.getFlavorTasteLevel())
                    .append(", \"proteinLevel\": ").append(item.getFlavorProteinLevel())
                    .append(", \"fatLevel\": ").append(item.getFlavorFatLevel())
                    .append(", \"allergens\": ").append(item.getAllergens() == null ? "[]" : item.getAllergens())
                    .append("}");
            if (i < menuList.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]");
        return sb.toString();
    }
}
