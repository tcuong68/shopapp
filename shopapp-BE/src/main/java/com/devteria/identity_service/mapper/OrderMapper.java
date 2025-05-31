package com.devteria.identity_service.mapper;

import com.devteria.identity_service.dto.response.OrderResponse;
import com.devteria.identity_service.entity.Orders;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(target = "firstName", ignore = true)
    @Mapping(target = "lastName", ignore = true)
    @Mapping(target = "phoneNumber", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "payment", ignore = true)
    OrderResponse toOrderResponse(Orders order);
}
