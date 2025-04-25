package com.devteria.identity_service.controller;

import com.devteria.identity_service.dto.request.OrderCreationRequest;
import com.devteria.identity_service.dto.request.OrderSearchRequest;
import com.devteria.identity_service.dto.response.ApiResponse;
import com.devteria.identity_service.dto.response.OrderResponse;
import com.devteria.identity_service.dto.response.PagedData;
import com.devteria.identity_service.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping("/checkout")
    public ApiResponse<OrderResponse> createOrder(@RequestBody OrderCreationRequest request) {
        OrderResponse orderResponse = orderService.createOrderFromCart(request);
        return ApiResponse.<OrderResponse>builder()
                .result(orderResponse)
                .build();
    }


    //admin
    @GetMapping("/admin")
    public ApiResponse<PagedData<OrderResponse>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        PagedData<OrderResponse> pagedData = orderService.getAllOrders(pageable);
        return ApiResponse.<PagedData<OrderResponse>>builder()
                .result(pagedData)
                .build();
    }

    @PostMapping("/admin/search")
    public ApiResponse<PagedData<OrderResponse>> searchOrders(@RequestBody OrderSearchRequest request,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "10") int size,
                                                              @RequestParam(defaultValue = "id") String sortBy,
                                                              @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        PagedData<OrderResponse> pagedData = orderService.searchOrders(request, pageable);

        return ApiResponse.<PagedData<OrderResponse>>builder()
                .result(pagedData)
                .build();
    }

    @GetMapping("/my-order")
    public ApiResponse<List<OrderResponse>> getAllOrdersForCurrentUser(Pageable pageable) {
        return ApiResponse.<List<OrderResponse>>builder()
                .result(orderService.getAllOrdersForCurrentUser(pageable))
                .build();
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderResponse> getOrderById(@PathVariable Long orderId) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.getOrderById(orderId))
                .build();
    }

}
