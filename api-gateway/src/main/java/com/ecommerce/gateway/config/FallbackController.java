package com.ecommerce.gateway.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/user-service")
    public ResponseEntity<Map<String, Object>> userServiceFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(buildFallbackResponse("user-service", "User Service is currently unavailable. Please try again later."));
    }

    @GetMapping("/product-service")
    public ResponseEntity<Map<String, Object>> productServiceFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(buildFallbackResponse("product-service", "Product Service is currently unavailable. Please try again later."));
    }

    @GetMapping("/cart-service")
    public ResponseEntity<Map<String, Object>> cartServiceFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(buildFallbackResponse("cart-service", "Cart Service is currently unavailable. Please try again later."));
    }

    @GetMapping("/order-service")
    public ResponseEntity<Map<String, Object>> orderServiceFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(buildFallbackResponse("order-service", "Order Service is currently unavailable. Please try again later."));
    }

    @GetMapping("/payment-service")
    public ResponseEntity<Map<String, Object>> paymentServiceFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(buildFallbackResponse("payment-service", "Payment Service is currently unavailable. Please try again later."));
    }

    @GetMapping("/inventory-service")
    public ResponseEntity<Map<String, Object>> inventoryServiceFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(buildFallbackResponse("inventory-service", "Inventory Service is currently unavailable. Please try again later."));
    }

    private Map<String, Object> buildFallbackResponse(String service, String message) {
        return Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 503,
                "error", "Service Unavailable",
                "service", service,
                "message", message
        );
    }
}
