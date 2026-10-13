package com.review_service.dto;

public record RankingResponse(Long dishId, String dishName, double average, long count) {}