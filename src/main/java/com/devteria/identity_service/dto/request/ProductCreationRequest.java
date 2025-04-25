package com.devteria.identity_service.dto.request;

import com.devteria.identity_service.entity.Category;
import com.devteria.identity_service.entity.ProductVariant;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor //tao constructor k co tham so
@AllArgsConstructor //tao constructor co tham so day du
@Builder //khong can day du cac truong van tao duoc Object
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductCreationRequest {
    String name;
    String description;
    Long price;
    String image_url;
    Set<ProductVariantRequest> variants;
    Long categoryId;
}
