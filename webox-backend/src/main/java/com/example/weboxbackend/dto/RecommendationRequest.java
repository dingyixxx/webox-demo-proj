package com.example.weboxbackend.dto;


import lombok.Data;

@Data
public class RecommendationRequest {
    private String prompt;  // 用户自然语言输入，如"我想吃点清淡的"
}
