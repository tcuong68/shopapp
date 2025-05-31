package com.devteria.identity_service.mapper;

import com.devteria.identity_service.dto.response.CartItemResponse;
import com.devteria.identity_service.entity.CartItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CartItemMapper {
    CartItemResponse toCartItemResponse(CartItem cartItem);
}
