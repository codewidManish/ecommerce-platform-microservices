package com.ecommerce.cartservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(CartNotFoundException.class)
    public ResponseEntity<Map<String, Object>> onCartNotFound(CartNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), "CART_NOT_FOUND");
    }

    @ExceptionHandler(CartItemNotFoundException.class)
    public ResponseEntity<Map<String, Object>> onItemNotFound(CartItemNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), "CART_ITEM_NOT_FOUND");
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Map<String, Object>> onProductNotFound(ProductNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), "PRODUCT_NOT_FOUND");
    }

    @ExceptionHandler(ProductNotAvailableException.class)
    public ResponseEntity<Map<String, Object>> onNotAvailable(ProductNotAvailableException ex) {
        return error(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), "PRODUCT_NOT_AVAILABLE");
    }

    @ExceptionHandler(InvalidCartOperationException.class)
    public ResponseEntity<Map<String, Object>> onInvalid(InvalidCartOperationException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage(), "INVALID_OPERATION");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> onValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", 400);
        body.put("message", "Validation failed");
        body.put("errorCode", "VALIDATION_ERROR");
        body.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> onGeneric(Exception ex) {
        log.error("Unhandled: {}", ex.getMessage(), ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", "INTERNAL_ERROR");
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus s, String m, String c) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", s.value());
        body.put("message", m);
        body.put("errorCode", c);
        body.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(s).body(body);
    }
}
