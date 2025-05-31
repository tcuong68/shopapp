package com.devteria.identity_service.controller;

import com.devteria.identity_service.dto.request.PaymentRequest;
import com.devteria.identity_service.dto.response.ApiResponse;
import com.devteria.identity_service.dto.response.PaymentResponse;
import com.devteria.identity_service.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VNPayController {
    @Autowired
    VNPayService vnPayService;
    @GetMapping("/vnpay/create")
    public ApiResponse<PaymentResponse> createPayment(HttpServletRequest request) {
        return ApiResponse.<PaymentResponse>builder()
                .result(vnPayService.createPaymentUrl(request))
                .message("Payment URL created successfully")
                .build();
    }

    @GetMapping("/vnpay/callback")
    public ApiResponse<String> paymentCallback(HttpServletRequest request) {
        vnPayService.processPaymentCallback(request);

        return ApiResponse.<String>builder()
                .result("Payment processed successfully")
                .message("Payment processed successfully")
                .build();
    }
} 