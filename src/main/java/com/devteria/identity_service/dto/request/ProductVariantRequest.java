package com.devteria.identity_service.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor //tao constructor k co tham so
@AllArgsConstructor //tao constructor co tham so day du
@Builder //khong can day du cac truong van tao duoc Object
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariantRequest {
    @NotBlank(message = "Size is required")
    String size;

    @NotBlank(message = "Color is required")
    String color;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price cannot be negative")
    Long price;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    Integer quantity;
}
