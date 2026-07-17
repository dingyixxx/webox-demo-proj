package com.example.weboxbackend.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdatePreferencesRequest {
    private List<String> allergens;
    private List<String> cuisinePreferences;
    private Integer spicinessLevel;
    private Integer tasteLevel;
    private Integer proteinLevel;
    private Integer fatLevel;
    private Integer preferredMinPrice;
    private Integer preferredMaxPrice;
}
