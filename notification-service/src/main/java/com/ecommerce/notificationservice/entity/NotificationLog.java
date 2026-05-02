package com.ecommerce.notificationservice.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "notification_logs")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificationLog {

    @Id
    private String id;

    @Indexed
    private Long userId;

    private String channel;          // EMAIL, SMS, PUSH

    private String notificationType; // ORDER_CONFIRMED, WELCOME, etc.

    private String recipient;        // email address or phone number

    private String subject;

    @Indexed
    private String status;           // SENT, FAILED, PENDING

    private String errorMessage;

    @Indexed
    private LocalDateTime createdAt;
}
