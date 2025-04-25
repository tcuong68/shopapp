package com.devteria.identity_service.mapper;

import com.devteria.identity_service.dto.response.CartResponse;
import com.devteria.identity_service.entity.Cart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartMapper {
    @Mapping(target = "items",  ignore = true)
    @Mapping(target = "subTotal",  ignore = true)
    CartResponse toCartResponse(Cart cart);
}
