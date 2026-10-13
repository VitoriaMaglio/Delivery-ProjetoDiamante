package com.order_service.config;

import com.order_service.entity.Dish;
import com.order_service.repository.DishRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    private final DishRepository dishRepository;

    public DataLoader(DishRepository dishRepository) {
        this.dishRepository = dishRepository;
    }

    @Override
    public void run(String... args) {
        if (dishRepository.count() > 0) return;

        dishRepository.saveAll(List.of(
                new Dish("Promo Burger", "Promotional burger with cheddar", new BigDecimal("29.90"), 10),
                new Dish("House Burger", "Brioche bun", new BigDecimal("39.90"), 50),
                new Dish("Veggie Bowl", "Grilled vegetables and quinoa", new BigDecimal("34.90"), 50),
                new Dish("Margherita Pizza", "Tomato, mozzarella and basil", new BigDecimal("44.90"), 50),
                new Dish("Chocolate Brownie", "Brownie with vanilla ice cream", new BigDecimal("19.90"), 50)
        ));
    }
}
