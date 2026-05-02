package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.dto.PlaceOrderRequest;
import com.ecommerce.orderservice.dto.OrderResponse;
import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.exception.OrderNotFoundException;
import com.ecommerce.orderservice.repository.OrderRepository;
import com.ecommerce.orderservice.saga.OrderSagaOrchestrator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderSagaOrchestrator sagaOrchestrator;
    @InjectMocks private OrderService orderService;

    private PlaceOrderRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = PlaceOrderRequest.builder()
                .items(List.of(
                    PlaceOrderRequest.OrderItemRequest.builder()
                        .productId(1L).productName("iPhone 15").productSku("APPL-IP15")
                        .quantity(1).unitPrice(new BigDecimal("99999.00"))
                        .build()
                ))
                .shippingAddress(PlaceOrderRequest.ShippingAddressRequest.builder()
                    .fullName("John Doe").streetAddress("123 MG Road").city("Bangalore")
                    .state("Karnataka").postalCode("560001").country("IN").phone("+919876543210")
                    .build())
                .build();
    }

    @Test
    @DisplayName("Should place order successfully and trigger saga")
    void shouldPlaceOrderSuccessfully() {
        Order savedOrder = Order.builder()
                .id(1L).orderNumber("ORD-20240101-1001")
                .userId(100L).status(Order.OrderStatus.PENDING)
                .subtotal(new BigDecimal("99999.00"))
                .taxAmount(new BigDecimal("17999.82"))
                .shippingAmount(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("117998.82"))
                .build();

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        doNothing().when(sagaOrchestrator).startOrderSaga(any());

        OrderResponse response = orderService.placeOrder(100L, validRequest);

        assertThat(response).isNotNull();
        assertThat(response.getOrderNumber()).isEqualTo("ORD-20240101-1001");
        assertThat(response.getStatus()).isEqualTo("PENDING");
        verify(sagaOrchestrator).startOrderSaga(any(Order.class));
    }

    @Test
    @DisplayName("Should throw OrderNotFoundException for unknown orderId")
    void shouldThrowWhenOrderNotFound() {
        when(orderRepository.findByIdAndUserIdWithDetails(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrder(99L, 1L))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Should cancel order when status is PENDING")
    void shouldCancelPendingOrder() {
        Order pendingOrder = Order.builder()
                .id(1L).orderNumber("ORD-20240101-1001").userId(1L)
                .status(Order.OrderStatus.PENDING)
                .subtotal(BigDecimal.TEN).taxAmount(BigDecimal.ONE)
                .shippingAmount(BigDecimal.ZERO).totalAmount(new BigDecimal("11"))
                .build();

        when(orderRepository.findByIdAndUserIdWithDetails(1L, 1L))
                .thenReturn(Optional.of(pendingOrder));
        when(orderRepository.save(any())).thenReturn(pendingOrder);

        OrderResponse result = orderService.cancelOrder(1L, 1L, "Changed mind");

        assertThat(result.getStatus()).isEqualTo("CANCELLED");
        verify(orderRepository).save(pendingOrder);
    }
}
