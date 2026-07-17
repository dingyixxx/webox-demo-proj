package com.example.weboxbackend.controller;


import com.example.weboxbackend.dto.RecommendationRequest;
import com.example.weboxbackend.dto.Result;
import com.example.weboxbackend.service.RecommendationService;
import com.example.weboxbackend.vo.RecommendationVO;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping
    public Result<List<RecommendationVO>> getRecommendations(HttpServletRequest request,
                                                             @RequestBody RecommendationRequest recommendationRequest) {
        Long userId = (Long) request.getAttribute("userId");
        List<RecommendationVO> list = recommendationService.getRecommendations(userId, recommendationRequest);
        return Result.success(list);
    }


}
