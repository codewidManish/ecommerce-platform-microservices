package com.ecommerce.cartservice.controller;

import com.ecommerce.cartservice.dto.*;
import com.ecommerce.cartservice.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping cart management (Redis-backed)")
@SecurityRequirement(name = "Bearer Authentication")
public class CartController {

    private final CartService cartService;

    /** userId comes from gateway-injected header X-User-Id */
    private Long extractUserId(String userIdHeader) {
        return Long.parseLong(userIdHeader);
    }

    @GetMapping
    @Operation(summary = "Get current user's cart")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(ApiResponse.success(cartService.getCart(extractUserId(userId)), "Cart retrieved"));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody AddCartItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success(cartService.addItem(extractUserId(userId), request), "Item added to cart"));
    }

    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(cartService.removeItem(extractUserId(userId), productId), "Item removed"));
    }

    @PatchMapping("/items/{productId}")
    @Operation(summary = "Update item quantity")
    public ResponseEntity<ApiResponse<CartResponse>> updateQuantity(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long productId,
            @RequestParam int quantity) {
        return ResponseEntity.ok(ApiResponse.success(cartService.updateItemQuantity(extractUserId(userId), productId, quantity), "Quantity updated"));
    }

    @DeleteMapping
    @Operation(summary = "Clear entire cart")
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @RequestHeader("X-User-Id") String userId) {
        cartService.clearCart(extractUserId(userId));
        return ResponseEntity.ok(ApiResponse.success(null, "Cart cleared"));
    }
}
