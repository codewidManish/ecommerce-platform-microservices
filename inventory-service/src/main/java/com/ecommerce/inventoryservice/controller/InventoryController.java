package com.ecommerce.inventoryservice.controller;

import com.ecommerce.inventoryservice.dto.*;
import com.ecommerce.inventoryservice.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Stock management")
@SecurityRequirement(name = "Bearer Authentication")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get inventory for a product")
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventory(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getByProductId(productId), "Inventory retrieved"));
    }

    @GetMapping("/product/{productId}/check")
    @Operation(summary = "Check if quantity is available")
    public ResponseEntity<ApiResponse<Boolean>> checkAvailability(
            @PathVariable Long productId, @RequestParam int quantity) {
        return ResponseEntity.ok(ApiResponse.success(
                inventoryService.checkAvailability(productId, quantity), "Availability checked"));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create or update inventory [ADMIN]")
    public ResponseEntity<ApiResponse<InventoryResponse>> upsertInventory(
            @Valid @RequestBody UpsertInventoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.upsertInventory(request), "Inventory updated"));
    }

    @PostMapping("/product/{productId}/restock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Restock inventory [ADMIN]")
    public ResponseEntity<ApiResponse<InventoryResponse>> restock(
            @PathVariable Long productId, @RequestParam int quantity) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.restock(productId, quantity), "Restocked successfully"));
    }
}
