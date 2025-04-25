package com.devteria.identity_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItemRequest {
    Long productVariantId;
    int quantity;
    BigDecimal price; // Gia tai thoi diem dat hang (tranh thay doi neu sau nay bi update)
}
