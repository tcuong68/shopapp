package com.devteria.identity_service.mapper;

import com.devteria.identity_service.dto.response.OrderItemResponse;
import com.devteria.identity_service.entity.OrderItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {
    OrderItemResponse toOrderItemResponse(OrderItem orderItem);
}
