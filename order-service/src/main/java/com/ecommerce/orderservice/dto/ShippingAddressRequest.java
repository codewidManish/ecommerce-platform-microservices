package com.ecommerce.orderservice.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingAddressRequest {
    @NotBlank private String fullName;
    @NotBlank private String streetAddress;
    @NotBlank private String city;
    private String state;
    @NotBlank @Pattern(regexp = "\\d{6}") private String postalCode;
    @NotBlank @Size(min = 2, max = 2)     private String country;
    @NotBlank private String phone;
}
