package com.example.weboxbackend.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.weboxbackend.pojo.MenuItem;
import com.example.weboxbackend.pojo.MenuItemHistory;
import com.example.weboxbackend.pojo.User;
import com.example.weboxbackend.mapper.MenuItemHistoryMapper;
import com.example.weboxbackend.mapper.MenuItemMapper;
import com.example.weboxbackend.mapper.UserMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserMapper userMapper;
    private final MenuItemMapper menuItemMapper;
    private final MenuItemHistoryMapper menuItemHistoryMapper;
    private final ObjectMapper objectMapper;

    public DataInitializer(UserMapper userMapper,
                           MenuItemMapper menuItemMapper,
                           MenuItemHistoryMapper menuItemHistoryMapper,
                           ObjectMapper objectMapper) {
        this.userMapper = userMapper;
        this.menuItemMapper = menuItemMapper;
        this.menuItemHistoryMapper = menuItemHistoryMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {
        initDefaultUser();
        initMenuItems();
    }

    private void initDefaultUser() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, "root@qq.com")
                .eq(User::getIsDeleted, 0);
        User existing = userMapper.selectOne(wrapper);
        if (existing != null) {
            log.info("默认用户已存在，跳过初始化");
            return;
        }

        User user = new User();
        user.setEmail("root@qq.com");
        user.setPassword(Md5Util.encrypt("rootroot"));
        user.setName("root");
        user.setIsDeleted(0);
        user.setCreatedBy("system");
        user.setUpdatedBy("system");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.insert(user);
        log.info("默认用户 root@qq.com 初始化成功");
    }

    private void initMenuItems() throws Exception {
        LambdaQueryWrapper<MenuItem> countWrapper = new LambdaQueryWrapper<>();
        Long count = menuItemMapper.selectCount(countWrapper);
        if (count != null && count > 0) {
            log.info("菜品数据已存在（{}条），跳过初始化", count);
            return;
        }

        ClassPathResource resource = new ClassPathResource("sample-menu-items.json");
        try (InputStream is = resource.getInputStream()) {
            List<Map<String, Object>> items = objectMapper.readValue(is, new TypeReference<List<Map<String, Object>>>() {});

            for (Map<String, Object> item : items) {
                MenuItem menuItem = new MenuItem();
                menuItem.setName((String) item.get("name"));
                menuItem.setDescription((String) item.get("description"));
                menuItem.setImage((String) item.get("image"));
                menuItem.setCategory((String) item.get("category"));

                List<String> allergens = objectMapper.convertValue(
                        item.get("allergens"), new TypeReference<List<String>>() {});
                menuItem.setAllergens(allergens);

                menuItem.setIsDeleted(0);
                menuItem.setCreatedBy("system");
                menuItem.setUpdatedBy("system");
                menuItem.setCreatedAt(LocalDateTime.now());
                menuItem.setUpdatedAt(LocalDateTime.now());

                menuItemMapper.insert(menuItem);

                Number priceNum = (Number) item.get("price");
                int priceExpanded = priceNum.intValue() * 100;

                MenuItemHistory history = new MenuItemHistory();
                history.setMenuItemId(menuItem.getId());
                history.setPrice(priceExpanded);
                history.setValidFromDate(LocalDate.of(1700, 1, 1));
                history.setIsDeleted(0);
                history.setCreatedBy("system");
                history.setUpdatedBy("system");
                history.setCreatedAt(LocalDateTime.now());
                history.setUpdatedAt(LocalDateTime.now());

                menuItemHistoryMapper.insert(history);

                log.info("菜品[{}]及价格历史初始化成功, price={}", menuItem.getName(), priceExpanded);
            }
        }
        log.info("菜品数据初始化完成");
    }
}
