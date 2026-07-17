package com.example.weboxbackend.service;

import com.example.weboxbackend.dto.UpdatePreferencesRequest;
import com.example.weboxbackend.vo.PreferencesVO;

public interface PreferencesService {
    PreferencesVO getPreferences(Long userId);

    PreferencesVO updatePreferences(Long userId, UpdatePreferencesRequest request);
}
