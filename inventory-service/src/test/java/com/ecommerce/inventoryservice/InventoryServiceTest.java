package com.ecommerce.inventoryservice.service;

import com.ecommerce.inventoryservice.entity.Inventory;
import com.ecommerce.inventoryservice.exception.InventoryNotFoundException;
import com.ecommerce.inventoryservice.repository.InventoryRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryService Unit Tests")
class InventoryServiceTest {

    @Mock private InventoryRepository inventoryRepository;
    @Mock private KafkaTemplate<String, Object> kafkaTemplate;
    @InjectMocks private InventoryService inventoryService;

    private Inventory sampleInventory;

    @BeforeEach
    void setUp() {
        sampleInventory = Inventory.builder()
                .id(1L).productId(10L).productSku("PROD-001")
                .quantity(100).reservedQuantity(0)
                .lowStockThreshold(10)
                .build();
    }

    @Test
    @DisplayName("Should reserve stock successfully")
    void shouldReserveStock() {
        sampleInventory.reserve(5);

        assertThat(sampleInventory.getReservedQuantity()).isEqualTo(5);
        assertThat(sampleInventory.getAvailableQuantity()).isEqualTo(95);
    }

    @Test
    @DisplayName("Should throw when insufficient stock")
    void shouldThrowOnInsufficientStock() {
        assertThatThrownBy(() -> sampleInventory.reserve(150))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    @DisplayName("Should correctly detect low stock")
    void shouldDetectLowStock() {
        sampleInventory.reserve(95);   // only 5 left

        assertThat(sampleInventory.isLowStock()).isTrue();
        assertThat(sampleInventory.getAvailableQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should restock and increase quantity")
    void shouldRestock() {
        when(inventoryRepository.findByProductId(10L)).thenReturn(Optional.of(sampleInventory));
        when(inventoryRepository.save(any())).thenReturn(sampleInventory);

        inventoryService.restock(10L, 50);

        assertThat(sampleInventory.getQuantity()).isEqualTo(150);
    }

    @Test
    @DisplayName("checkAvailability returns true when enough stock")
    void shouldReturnTrueWhenAvailable() {
        when(inventoryRepository.findByProductId(10L)).thenReturn(Optional.of(sampleInventory));
        assertThat(inventoryService.checkAvailability(10L, 50)).isTrue();
    }

    @Test
    @DisplayName("checkAvailability returns false when not enough stock")
    void shouldReturnFalseWhenNotAvailable() {
        when(inventoryRepository.findByProductId(10L)).thenReturn(Optional.of(sampleInventory));
        assertThat(inventoryService.checkAvailability(10L, 200)).isFalse();
    }
}
