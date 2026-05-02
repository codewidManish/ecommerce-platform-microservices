package com.ecommerce.inventoryservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(InventoryNotFoundException.class)
    public ResponseEntity<Map<String, Object>> onNotFound(InventoryNotFoundException ex) {
        return err(HttpStatus.NOT_FOUND, ex.getMessage(), "INVENTORY_NOT_FOUND");
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> onIllegalState(IllegalStateException ex) {
        return err(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), "INSUFFICIENT_STOCK");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> onGeneric(Exception ex) {
        log.error("Unhandled: {}", ex.getMessage(), ex);
        return err(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", "INTERNAL_ERROR");
    }

    private ResponseEntity<Map<String, Object>> err(HttpStatus s, String m, String c) {
        return ResponseEntity.status(s).body(Map.of(
            "status", s.value(), "message", m,
            "errorCode", c, "timestamp", LocalDateTime.now().toString()));
    }
}
