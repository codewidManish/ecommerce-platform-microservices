package com.ecommerce.notificationservice.repository;

import com.ecommerce.notificationservice.entity.NotificationLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationLogRepository extends MongoRepository<NotificationLog, String> {
    List<NotificationLog> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<NotificationLog> findByStatus(String status);
}
