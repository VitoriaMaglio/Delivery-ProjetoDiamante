package com.order_service.dto;

public record ReviewRequest(Long dishId, Integer rating, String comment) {}