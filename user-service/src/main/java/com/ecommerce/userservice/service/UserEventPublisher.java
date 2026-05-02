package com.ecommerce.userservice.service;

import com.ecommerce.userservice.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String USER_EVENTS_TOPIC = "user-events";

    public void publishUserRegistered(User user) {
        var event = Map.of(
                "eventType", "USER_REGISTERED",
                "userId", user.getId(),
                "email", user.getEmail(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "timestamp", LocalDateTime.now().toString()
        );
        kafkaTemplate.send(USER_EVENTS_TOPIC, user.getId().toString(), event);
        log.info("Published USER_REGISTERED event for userId: {}", user.getId());
    }

    public void publishProfileUpdated(User user) {
        var event = Map.of(
                "eventType", "USER_PROFILE_UPDATED",
                "userId", user.getId(),
                "email", user.getEmail(),
                "timestamp", LocalDateTime.now().toString()
        );
        kafkaTemplate.send(USER_EVENTS_TOPIC, user.getId().toString(), event);
        log.info("Published USER_PROFILE_UPDATED event for userId: {}", user.getId());
    }
}
