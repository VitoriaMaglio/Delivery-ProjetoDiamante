package com.payment_service.controller;


import com.payment_service.dto.PaymentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);
    private final Random random = new Random();

    @Value("${server.port}")
    private int port;

    @PostMapping
    public ResponseEntity<Map<String, Object>> pay(@RequestBody PaymentRequest request) {
        if (random.nextBoolean()) {
            log.warn("Payment FAILED on instance {}", port);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Simulated payment failure", "instance", port));
        }
        log.info("Payment APPROVED on instance {} for amount {}", port, request.amount());
        return ResponseEntity.ok(Map.of("status", "APPROVED", "instance", port));
    }
}