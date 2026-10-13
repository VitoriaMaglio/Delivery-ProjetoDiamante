package com.review_service.service;

import com.review_service.config.RabbitConfig;
import com.review_service.dto.ReviewMessage;
import com.review_service.entity.ReviewSummary;
import com.review_service.repository.ReviewSummaryRepository;
import jakarta.transaction.Transactional;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ReviewBuffer {

    private static class Acc {
        String dishName;
        long total;
        long count;
    }

    private final ConcurrentHashMap<Long, Acc> buffer = new ConcurrentHashMap<>();
    private final ReviewSummaryRepository repository;

    public ReviewBuffer(ReviewSummaryRepository repository) {
        this.repository = repository;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void receive(ReviewMessage message) {
        buffer.compute(message.dishId(), (id, acc) -> {
            if (acc == null) acc = new Acc();
            acc.dishName = message.dishName();
            acc.total += message.rating();
            acc.count += 1;
            return acc;
        });
    }

    @Scheduled(fixedRate = 5000)
    @Transactional
    public void flush() {
        for (Long dishId : new ArrayList<>(buffer.keySet())) {
            Acc acc = buffer.remove(dishId);
            if (acc == null) continue;

            ReviewSummary summary = repository.findById(dishId)
                    .orElseGet(() -> new ReviewSummary(dishId, acc.dishName));
            summary.setDishName(acc.dishName);
            summary.add(acc.total, acc.count);
            repository.save(summary);
        }
    }
}
