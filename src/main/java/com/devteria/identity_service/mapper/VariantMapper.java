package com.devteria.identity_service.mapper;

import com.devteria.identity_service.dto.request.ProductVariantRequest;
import com.devteria.identity_service.dto.response.VariantResponse;
import com.devteria.identity_service.entity.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VariantMapper {
    ProductVariant toProductVariant(ProductVariantRequest request);
    VariantResponse toVariantResponse(ProductVariant productVariant);
}
