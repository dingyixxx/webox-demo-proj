package com.example.weboxbackend.service.impl;

import com.example.weboxbackend.dto.UpdatePreferencesRequest;
import com.example.weboxbackend.exception.BusinessException;
import com.example.weboxbackend.mapper.UserMapper;
import com.example.weboxbackend.pojo.User;
import com.example.weboxbackend.service.PreferencesService;
import com.example.weboxbackend.vo.PreferencesVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PreferencesServiceImpl implements PreferencesService {

    private final UserMapper userMapper;

    public PreferencesServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public PreferencesVO getPreferences(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BusinessException(404, "用户不存在");
        }

        PreferencesVO vo = new PreferencesVO();
        vo.setAllergens(user.getAllergens());
        vo.setCuisinePreferences(user.getCategories());
        vo.setSpicinessLevel(user.getFlavorSpicinessLevel());
        vo.setTasteLevel(user.getFlavorTasteLevel());
        vo.setProteinLevel(user.getFlavorProteinLevel());
        vo.setFatLevel(user.getFlavorFatLevel());
        vo.setPreferredMinPrice(user.getPreferredMinPrice());
        vo.setPreferredMaxPrice(user.getPreferredMaxPrice());
        return vo;
    }

    @Override
    public PreferencesVO updatePreferences(Long userId, UpdatePreferencesRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BusinessException(404, "用户不存在");
        }

        if (request.getAllergens() != null) {
            user.setAllergens(request.getAllergens());
        }
        if (request.getCuisinePreferences() != null) {
            user.setCategories(request.getCuisinePreferences());
        }
        if (request.getSpicinessLevel() != null) {
            user.setFlavorSpicinessLevel(request.getSpicinessLevel());
        }
        if (request.getTasteLevel() != null) {
            user.setFlavorTasteLevel(request.getTasteLevel());
        }
        if (request.getProteinLevel() != null) {
            user.setFlavorProteinLevel(request.getProteinLevel());
        }
        if (request.getFatLevel() != null) {
            user.setFlavorFatLevel(request.getFatLevel());
        }
        if (request.getPreferredMinPrice() != null) {
            user.setPreferredMinPrice(request.getPreferredMinPrice());
        }
        if (request.getPreferredMaxPrice() != null) {
            user.setPreferredMaxPrice(request.getPreferredMaxPrice());
        }

        user.setUpdatedBy(String.valueOf(userId));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        return getPreferences(userId);
    }
}
