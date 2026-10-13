package com.order_service.service;

import com.order_service.client.PaymentClient;
import com.order_service.dto.OrderRequest;
import com.order_service.entity.CustomerOrder;
import com.order_service.entity.Dish;
import com.order_service.exception.BadRequestException;
import com.order_service.exception.NotFoundException;
import com.order_service.exception.OutOfStockException;
import com.order_service.exception.PaymentFailedException;
import com.order_service.repository.CustomerOrderRepository;
import com.order_service.repository.DishRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class OrderService {

    private final DishRepository dishRepository;
    private final CustomerOrderRepository orderRepository;
    private final PaymentClient paymentClient;

    public OrderService(DishRepository dishRepository,
                        CustomerOrderRepository orderRepository,
                        PaymentClient paymentClient) {
        this.dishRepository = dishRepository;
        this.orderRepository = orderRepository;
        this.paymentClient = paymentClient;
    }

    @Transactional
    public CustomerOrder createOrder(OrderRequest request) {
        if (request.dishId() == null || request.quantity() == null || request.quantity() < 1) {
            throw new BadRequestException("Quantity must be at least 1");
        }

        Dish dish = dishRepository.findByIdForUpdate(request.dishId())
                .orElseThrow(() -> new NotFoundException("Dish not found"));

        if (dish.getStock() < request.quantity()) {
            throw new OutOfStockException("Dish out of stock");
        }

        dish.setStock(dish.getStock() - request.quantity());
        dishRepository.save(dish);

        BigDecimal total = dish.getPrice().multiply(BigDecimal.valueOf(request.quantity()));

        try {
            paymentClient.charge(total);
        } catch (Exception e) {
            throw new PaymentFailedException("Payment service unavailable");
        }

        CustomerOrder order = new CustomerOrder();
        order.setDishId(dish.getId());
        order.setQuantity(request.quantity());
        order.setTotalPrice(total);
        order.setStatus("CONFIRMED");
        order.setCreatedAt(LocalDateTime.now());

        return orderRepository.save(order);
    }

    public CustomerOrder findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found"));
    }
}
