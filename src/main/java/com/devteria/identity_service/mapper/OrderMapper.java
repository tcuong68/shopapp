package com.devteria.identity_service.mapper;

import com.devteria.identity_service.dto.response.OrderResponse;
import com.devteria.identity_service.entity.Orders;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    OrderResponse toOrderResponse(Orders order);
}
