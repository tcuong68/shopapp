package com.devteria.identity_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {
    Long id;
    String firstName;
    String lastName;
    String phoneNumber;
    String address;
    String status;
    Long shippingFee;
    String note;
    Long totalPrice;
    String payment;
    List<OrderItemResponse> items;
}
