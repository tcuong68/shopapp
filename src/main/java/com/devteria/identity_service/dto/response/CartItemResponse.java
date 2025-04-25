package com.devteria.identity_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartItemResponse {
    Long id;
    Long productId;
    Long variantId;
    String productName;
    int quantity;
    String size;
    String color;
    Long price;
    Long totalPrice;
}
