package com.review_service.dto;

public record ReviewMessage(Long dishId, String dishName, int rating, String comment) {}
