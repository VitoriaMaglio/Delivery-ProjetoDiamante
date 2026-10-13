package com.order_service.service;

import com.order_service.config.RabbitConfig;
import com.order_service.dto.ReviewMessage;
import com.order_service.dto.ReviewRequest;
import com.order_service.entity.Dish;
import com.order_service.exception.BadRequestException;
import com.order_service.exception.NotFoundException;
import com.order_service.repository.DishRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {

    private final DishRepository dishRepository;
    private final RabbitTemplate rabbitTemplate;

    public ReviewService(DishRepository dishRepository, RabbitTemplate rabbitTemplate) {
        this.dishRepository = dishRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(ReviewRequest request) {
        if (request.dishId() == null || request.rating() == null
                || request.rating() < 1 || request.rating() > 5) {
            throw new BadRequestException("Rating must be between 1 and 5");
        }

        Dish dish = dishRepository.findById(request.dishId())
                .orElseThrow(() -> new NotFoundException("Dish not found"));

        ReviewMessage message = new ReviewMessage(
                dish.getId(), dish.getName(), request.rating(), request.comment());

        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY, message);
    }
}
