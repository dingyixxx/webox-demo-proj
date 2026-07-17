package com.example.weboxbackend.service;

import com.example.weboxbackend.dto.RecommendationRequest;
import com.example.weboxbackend.vo.RecommendationVO;

import java.util.List;

public interface RecommendationService {
    List<RecommendationVO> getRecommendations(Long userId, RecommendationRequest request);

}
