package com.devteria.identity_service.service;

import com.devteria.identity_service.configuration.VNPayConfig;
import com.devteria.identity_service.dto.request.PaymentRequest;
import com.devteria.identity_service.dto.response.PaymentResponse;
import com.devteria.identity_service.entity.Orders;
import com.devteria.identity_service.entity.Payment;
import com.devteria.identity_service.repository.OrderRepository;
import com.devteria.identity_service.repository.PaymentRepository;
import com.devteria.identity_service.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VNPayService {
    VNPayConfig vnPayConfig;
    PaymentRepository paymentRepository;
    OrderRepository orderRepository;

    public PaymentResponse createPaymentUrl(HttpServletRequest request) {
        Long orderId = Long.parseLong(request.getParameter("orderId"));
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Long amount = order.getTotalPrice();

        Map<String, String> vnpParamsMap = vnPayConfig.getVNPayConfig();
        vnpParamsMap.put("vnp_Amount", String.valueOf(amount * 100L));
        vnpParamsMap.put("vnp_IpAddr", VNPayUtil.getIpAddress(request));
        String queryUrl = VNPayUtil.getPaymentURL(vnpParamsMap, true);
        String hashData = VNPayUtil.getPaymentURL(vnpParamsMap, false);
        String vnpSecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getSecretKey(), hashData);
        queryUrl += "&vnp_SecureHash=" + vnpSecureHash;
        String paymentUrl = vnPayConfig.getVnp_Payurl() + "?" + queryUrl;

        String vnpTxnRef = vnpParamsMap.get("vnp_TxnRef");

        // Check xem payment đã tồn tại chưa
        Payment payment = paymentRepository.findByVnpTxnRef(vnpTxnRef)
                .orElse(new Payment());

        payment.setAmount(amount);
        payment.setVnpTxnRef(vnpTxnRef);
        payment.setOrder(order);
        payment.setTransactionStatus("PENDING");
        paymentRepository.save(payment);

        return PaymentResponse.builder()
                .code("ok")
                .message("success")
                .paymentUrl(paymentUrl).build();
    }

    @Transactional
    public void processPaymentCallback(HttpServletRequest request) {
        String vnpTxnRef = request.getParameter("vnp_TxnRef");
        String vnpResponseCode = request.getParameter("vnp_ResponseCode");
        String vnpTransactionNo = request.getParameter("vnp_TransactionNo");
        String vnpBankCode = request.getParameter("vnp_BankCode");
        String vnpPayDate = request.getParameter("vnp_PayDate");

        Payment payment = paymentRepository.findByVnpTxnRef(vnpTxnRef)
                .orElseThrow(() -> new RuntimeException("Payment not found with vnp_TxnRef: " + vnpTxnRef));

        payment.setVnpResponseCode(vnpResponseCode);
        payment.setBankCode(vnpBankCode);
        payment.setPaymentTime(vnpPayDate);
        payment.setVnpTransactionNo(vnpTransactionNo);

        if ("00".equals(vnpResponseCode)) {
            payment.setTransactionStatus("COMPLETED");
            payment.getOrder().setStatus("PROCESSING");
        } else {
            payment.setTransactionStatus("FAILED");
            payment.getOrder().setStatus("CANCELLED");
        }

        paymentRepository.save(payment);
    }


} 