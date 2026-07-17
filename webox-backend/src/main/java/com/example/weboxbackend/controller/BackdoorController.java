package com.example.weboxbackend.controller;

import com.example.weboxbackend.dto.Result;
import com.example.weboxbackend.service.BackdoorService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/backdoor")
public class BackdoorController {

    private final BackdoorService backdoorService;

    public BackdoorController(BackdoorService backdoorService) {
        this.backdoorService = backdoorService;
    }

    @PostMapping("/sync-daily-menu")
    public Result<Void> syncDailyMenu() {
        backdoorService.syncDailyMenu();
        return Result.success();
    }

    @PostMapping("/analyze-menu-scores")
    public Result<Void> analyzeMenuScores() {
        backdoorService.analyzeAndScoreMenuItems();
        return Result.success();
    }
}
