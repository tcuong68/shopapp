package com.devteria.identity_service.mapper;

import com.devteria.identity_service.dto.request.ProductCreationRequest;
import com.devteria.identity_service.dto.request.ProductUpdateRequest;
import com.devteria.identity_service.dto.response.ProductResponse;
import com.devteria.identity_service.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "category",  ignore = true)
    @Mapping(target = "variants",  ignore = true)
    Product toProduct(ProductCreationRequest request);

    @Mapping(target = "category",  ignore = true)
    @Mapping(target = "variants",  ignore = true)
    void updateProduct(ProductUpdateRequest request, @MappingTarget Product product);

    @Mapping(target = "variants",  ignore = true)
    ProductResponse toProductResponse(Product product);
}
