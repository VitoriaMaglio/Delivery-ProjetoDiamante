package com.order_service.client;


import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class PaymentClient {

    private final RestTemplate restTemplate;

    public PaymentClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Retryable(
            maxRetries = 4,
            delay = 200,
            multiplier = 2,
            jitter = 100,
            maxDelay = 2000
    )
    public Map<String, Object> charge(BigDecimal amount) {
        return restTemplate.postForObject(
                "http://PAYMENT-SERVICE/payments",
                Map.of("amount", amount),
                Map.class
        );
    }
}
