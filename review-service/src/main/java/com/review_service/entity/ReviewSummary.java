package com.review_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class ReviewSummary {

    @Id
    private Long dishId;
    private String dishName;
    private long totalRating;
    private long count;

    public ReviewSummary() {}

    public ReviewSummary(Long dishId, String dishName) {
        this.dishId = dishId;
        this.dishName = dishName;
    }

    public void add(long rating, long quantity) {
        this.totalRating += rating;
        this.count += quantity;
    }

    public double getAverage() {
        return count == 0 ? 0 : (double) totalRating / count;
    }


}
