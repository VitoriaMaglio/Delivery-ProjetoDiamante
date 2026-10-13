package com.review_service.controller;

import com.review_service.dto.RankingResponse;
import com.review_service.repository.ReviewSummaryRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewSummaryRepository repository;

    public ReviewController(ReviewSummaryRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/ranking")
    public List<RankingResponse> ranking() {
        return repository.findAll().stream()
                .map(s -> new RankingResponse(s.getDishId(), s.getDishName(),
                        Math.round(s.getAverage() * 10.0) / 10.0, s.getCount()))
                .sorted(Comparator.comparingDouble(RankingResponse::average).reversed())
                .toList();
    }

}