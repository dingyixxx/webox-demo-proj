package com.example.weboxbackend.controller;

import com.example.weboxbackend.dto.Result;
import com.example.weboxbackend.dto.UpdatePreferencesRequest;
import com.example.weboxbackend.service.PreferencesService;
import com.example.weboxbackend.vo.PreferencesVO;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/users/me/preferences")
public class PreferencesController {

    private final PreferencesService preferencesService;

    public PreferencesController(PreferencesService preferencesService) {
        this.preferencesService = preferencesService;
    }

    @GetMapping
    public Result<PreferencesVO> getPreferences(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        PreferencesVO vo = preferencesService.getPreferences(userId);
        return Result.success(vo);
    }

    @PutMapping
    public Result<PreferencesVO> updatePreferences(HttpServletRequest request,
                                                   @RequestBody UpdatePreferencesRequest updatePreferencesRequest) {
        Long userId = (Long) request.getAttribute("userId");
        PreferencesVO vo = preferencesService.updatePreferences(userId, updatePreferencesRequest);
        return Result.success(vo);
    }
}
