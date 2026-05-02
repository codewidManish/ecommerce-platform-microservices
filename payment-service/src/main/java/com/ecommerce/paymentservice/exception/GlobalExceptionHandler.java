package com.ecommerce.paymentservice.exception;

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

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<Map<String, Object>> onNotFound(PaymentNotFoundException ex) {
        return err(HttpStatus.NOT_FOUND, ex.getMessage(), "PAYMENT_NOT_FOUND");
    }

    @ExceptionHandler(PaymentAlreadyProcessedException.class)
    public ResponseEntity<Map<String, Object>> onAlreadyProcessed(PaymentAlreadyProcessedException ex) {
        return err(HttpStatus.CONFLICT, ex.getMessage(), "PAYMENT_ALREADY_PROCESSED");
    }

    @ExceptionHandler(PaymentRefundException.class)
    public ResponseEntity<Map<String, Object>> onRefundError(PaymentRefundException ex) {
        return err(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), "REFUND_ERROR");
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
