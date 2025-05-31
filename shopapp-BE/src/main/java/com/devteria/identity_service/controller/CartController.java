package com.devteria.identity_service.controller;

import com.devteria.identity_service.dto.request.AddToCartRequest;
import com.devteria.identity_service.dto.request.UpdateCartRequest;
import com.devteria.identity_service.dto.response.ApiResponse;
import com.devteria.identity_service.dto.response.CartResponse;
import com.devteria.identity_service.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
public class CartController {
    @Autowired
    private CartService cartService;

    @GetMapping
    ApiResponse<CartResponse> getCart() {
        return ApiResponse.<CartResponse>builder()
                .result(cartService.getCart())
                .build();
    }

    @PostMapping("/add")
    ApiResponse<CartResponse> addToCart(@RequestBody AddToCartRequest request) {
        return ApiResponse.<CartResponse>builder()
                .result(cartService.addToCart(request.getProductVariantId(), request.getQuantity()))
                .build();
    }

    @PutMapping("/update")
    ApiResponse<CartResponse> updateCart(@RequestBody UpdateCartRequest request) {
        return ApiResponse.<CartResponse>builder()
                .result(cartService.updateCart(request.getCartItemId(), request.getQuantity()))
                .build();
    }

    @DeleteMapping("/delete/{cartItemId}")
    ApiResponse<CartResponse> deleteCartItem(@PathVariable Long cartItemId) {
        return ApiResponse.<CartResponse>builder()
                .result(cartService.deleteCartItem(cartItemId))
                .build();
    }
}
