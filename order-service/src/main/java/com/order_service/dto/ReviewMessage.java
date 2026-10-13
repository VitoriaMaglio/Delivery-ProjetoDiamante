package com.order_service.dto;

public record ReviewMessage(Long dishId, String dishName, int rating, String comment) {}
