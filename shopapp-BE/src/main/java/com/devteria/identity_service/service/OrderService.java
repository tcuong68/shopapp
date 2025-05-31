package com.devteria.identity_service.service;

import com.devteria.identity_service.dto.request.OrderCreationRequest;
import com.devteria.identity_service.dto.request.OrderSearchRequest;
import com.devteria.identity_service.dto.response.CartResponse;
import com.devteria.identity_service.dto.response.OrderItemResponse;
import com.devteria.identity_service.dto.response.OrderResponse;
import com.devteria.identity_service.dto.response.PagedData;
import com.devteria.identity_service.entity.Orders;
import com.devteria.identity_service.entity.OrderItem;
import com.devteria.identity_service.entity.Payment;
import com.devteria.identity_service.enums.OrderStatus;
import com.devteria.identity_service.mapper.OrderItemMapper;
import com.devteria.identity_service.mapper.OrderMapper;
import com.devteria.identity_service.repository.CartRepository;
import com.devteria.identity_service.repository.OrderRepository;
import com.devteria.identity_service.repository.ProductVariantRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EnableMethodSecurity
public class OrderService {
    OrderRepository orderRepository;
    CartRepository cartRepository;
    ProductVariantRepository productVariantRepository;
    OrderMapper orderMapper;
    CartService cartService;
    UserService userService;
    OrderItemMapper orderItemMapper;


    @Transactional
    public OrderResponse createOrderFromCart(OrderCreationRequest request) {
        CartResponse cartResponse = cartService.getCart();
        Orders order = new Orders();
        order.setUser(userService.getCurrentUser());
        order.setNote(request.getNote());
        order.setAddress(request.getAddress());
        order.setStatus(OrderStatus.PENDING.name());
        order.setPhoneNumber(request.getPhoneNumber());
        orderRepository.save(order);
        Payment payment = new Payment();
        payment.setMethod(request.getPaymentMethod());
//        payment.setOrder(order);
        order.setPayment(payment);

        List<OrderItem> OrderItems = cartResponse.getItems().stream().map(cartItemResponse -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setQuantity(cartItemResponse.getQuantity());
            orderItem.setPrice(cartItemResponse.getPrice());
            orderItem.setOrder(order);
            orderItem.setProductVariant(productVariantRepository.findById(cartItemResponse.getVariantId()).orElseThrow(() -> new RuntimeException("Variant not found")));
            return orderItem;
        }).collect(Collectors.toList());

        order.setOrderItems(OrderItems);
        order.setTotalPrice(cartResponse.getSubTotal());
        orderRepository.save(order);
        cartRepository.deleteById(cartResponse.getId());

        OrderResponse orderResponse = orderMapper.toOrderResponse(order);
        orderResponse.setFirstName(order.getUser().getFirstName());
        orderResponse.setLastName(order.getUser().getLastName());

        List<OrderItemResponse> items = new ArrayList<>();
        for (OrderItem orderItem : order.getOrderItems()) {
            OrderItemResponse item  = orderItemMapper.toOrderItemResponse(orderItem);
            item.setTotalPrice(orderItem.getPrice()*orderItem.getQuantity());
            item.setProductName(orderItem.getProductVariant().getProduct().getName());
            items.add(item);
        }
        orderResponse.setItems(items);

        return orderResponse;
    }

    public PagedData<OrderResponse> getAllOrdersForCurrentUser(Pageable pageable) {
        Page<Orders> pageResult = orderRepository.findAllByUser(userService.getCurrentUser(), pageable);
        List<OrderResponse> responses = pageResult.stream().map(order -> {
            OrderResponse response = orderMapper.toOrderResponse(order);

            response.setFirstName(userService.getCurrentUser().getFirstName());
            response.setLastName(userService.getCurrentUser().getLastName());
            response.setItems(order.getOrderItems().stream().map(orderItem -> {
                OrderItemResponse item = orderItemMapper.toOrderItemResponse(orderItem);
                item.setTotalPrice(orderItem.getPrice()*orderItem.getQuantity());
                item.setProductName(orderItem.getProductVariant().getProduct().getName());
                return item;
            }).collect(Collectors.toList()));

            return response;
        }).collect(Collectors.toList());
        return PagedData.<OrderResponse>builder()
                .content(responses)
                .pageNo(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .last(pageResult.isLast())
                .build();
    }

    public PagedData<OrderResponse> searchOrders(OrderSearchRequest request, Pageable pageable) {
        Page<Orders> pageResult = orderRepository.searchOrders(
                request.getId(),
                blankToNull(request.getStatus()),
                request.getCreated_at(),
                request.getMinTotalPrice(),
                request.getMaxTotalPrice(),
                pageable
        );

        List<OrderResponse> responses = pageResult.getContent().stream().map(order -> {
            OrderResponse response = orderMapper.toOrderResponse(order);

            response.setFirstName(order.getUser().getFirstName());
            response.setLastName(order.getUser().getLastName());

            response.setItems(order.getOrderItems().stream().map(orderItem -> {
                OrderItemResponse item = orderItemMapper.toOrderItemResponse(orderItem);
                item.setTotalPrice(orderItem.getPrice()*orderItem.getQuantity());
                item.setProductName(orderItem.getProductVariant().getProduct().getName());
                return item;
            }).collect(Collectors.toList()));

            return response;
        }).collect(Collectors.toList());

        return PagedData.<OrderResponse>builder()
                .content(responses)
                .pageNo(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .last(pageResult.isLast())
                .build();
    }

    private String blankToNull(String input) {
        return (input == null || input.trim().isEmpty()) ? null : input;
    }


    public PagedData<OrderResponse> getAllOrders(Pageable pageable) {
        Page<Orders> ordersPage = orderRepository.findAll(pageable);

        List<OrderResponse> responses = ordersPage.getContent().stream().map(order -> {
            OrderResponse response = orderMapper.toOrderResponse(order);

            if(order.getPayment() != null)
                response.setPayment(order.getPayment().getMethod());

            response.setFirstName(order.getUser().getFirstName());
            response.setLastName(order.getUser().getLastName());

            response.setItems(order.getOrderItems().stream().map(orderItem -> {
                OrderItemResponse item = orderItemMapper.toOrderItemResponse(orderItem);
                item.setTotalPrice(orderItem.getPrice()*orderItem.getQuantity());
                item.setProductName(orderItem.getProductVariant().getProduct().getName());
                return item;
            }).collect(Collectors.toList()));

            return response;
        }).collect(Collectors.toList());

        return PagedData.<OrderResponse>builder()
                .content(responses)
                .pageNo(ordersPage.getNumber())
                .pageSize(ordersPage.getSize())
                .totalElements(ordersPage.getTotalElements())
                .totalPages(ordersPage.getTotalPages())
                .last(ordersPage.isLast())
                .build();
    }


    public OrderResponse getOrderById(Long id) {
        Orders order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
        OrderResponse orderResponse = orderMapper.toOrderResponse(order);
        orderResponse.setFirstName(order.getUser().getFirstName());
        orderResponse.setLastName(order.getUser().getLastName());

        List<OrderItemResponse> items = new ArrayList<>();
        for (OrderItem orderItem : order.getOrderItems()) {
            OrderItemResponse item  = orderItemMapper.toOrderItemResponse(orderItem);
            item.setTotalPrice(orderItem.getPrice()*orderItem.getQuantity());
            item.setProductName(orderItem.getProductVariant().getProduct().getName());
            items.add(item);
        }
        orderResponse.setItems(items);
        return orderResponse;
    }

    public OrderResponse updateOrder(Long id, String status){
        Orders order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        return orderMapper.toOrderResponse(orderRepository.save(order));
    }


    public void deleteOrder(Long id){
        orderRepository.deleteById(id);
    }
}
