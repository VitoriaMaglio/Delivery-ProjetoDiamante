package com.order_service.service;

import com.order_service.exception.BadRequestException;
import com.order_service.repository.DishRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class ChatService {

    private static final String SYSTEM_PROMPT = """
            You are a restaurant assistant for a delivery app.
            Answer in Portuguese, with short answers.
            Use ONLY the menu provided below to answer about dishes, ingredients, prices and availability.
            If the question is not about the restaurant or its menu, politely refuse
            and bring the conversation back to the restaurant.
            """;

    private final ChatClient chatClient;
    private final DishRepository dishRepository;

    public ChatService(ChatClient.Builder builder, DishRepository dishRepository) {
        this.chatClient = builder.defaultSystem(SYSTEM_PROMPT).build();
        this.dishRepository = dishRepository;
    }

    public String ask(String question) {
        if (question == null || question.isBlank()) {
            throw new BadRequestException("Question is required");
        }

        String menu = dishRepository.findAll().stream()
                .map(d -> "- %s: %s | price R$ %s | stock %d".formatted(
                        d.getName(), d.getDescription(), d.getPrice(), d.getStock()))
                .collect(Collectors.joining("\n"));

        return chatClient.prompt()
                .user(u -> u.text("Current menu:\n{menu}\n\nCustomer question: {question}")
                        .param("menu", menu)
                        .param("question", question))
                .call()
                .content();
    }
}