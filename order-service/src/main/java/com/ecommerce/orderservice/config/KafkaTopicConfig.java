package com.ecommerce.orderservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.support.converter.JsonMessageConverter;

/**
 * Kafka topic definitions.
 * These beans auto-create topics when the application starts.
 * In production, use Kafka Admin or Terraform to manage topics.
 */
@Configuration
public class KafkaTopicConfig {

    // Order lifecycle topics
    @Bean public NewTopic orderEventsTopic() {
        return TopicBuilder.name("order-events").partitions(3).replicas(1).build();
    }

    // Inventory saga topics
    @Bean public NewTopic inventoryEventsTopic() {
        return TopicBuilder.name("inventory-events").partitions(3).replicas(1).build();
    }
    @Bean public NewTopic inventoryCommandsTopic() {
        return TopicBuilder.name("inventory-commands").partitions(3).replicas(1).build();
    }
    @Bean public NewTopic inventoryAlertsTopic() {
        return TopicBuilder.name("inventory-alerts").partitions(1).replicas(1).build();
    }

    // Payment saga topics
    @Bean public NewTopic paymentEventsTopic() {
        return TopicBuilder.name("payment-events").partitions(3).replicas(1).build();
    }
    @Bean public NewTopic paymentCommandsTopic() {
        return TopicBuilder.name("payment-commands").partitions(3).replicas(1).build();
    }

    // Notification + user topics
    @Bean public NewTopic notificationEventsTopic() {
        return TopicBuilder.name("notification-events").partitions(3).replicas(1).build();
    }
    @Bean public NewTopic userEventsTopic() {
        return TopicBuilder.name("user-events").partitions(3).replicas(1).build();
    }
    @Bean public NewTopic productEventsTopic() {
        return TopicBuilder.name("product-events").partitions(3).replicas(1).build();
    }

    @Bean
    public JsonMessageConverter jsonMessageConverter() {
        return new JsonMessageConverter();
    }
}
