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
        Long orderId= Long.parseLong(request.getParameter("orderId"));
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
        return  PaymentResponse.builder()
                .code("ok")
                .message("success")
                .paymentUrl(paymentUrl).build();
////        Map<String, String> vnpParams = new HashMap<>();
////        vnpParams.put("vnp_Version", "2.1.0");
////        vnpParams.put("vnp_Command", "pay");
////        vnpParams.put("vnp_TmnCode", vnpTmnCode);
//
////        vnpParams.put("vnp_CurrCode", "VND");
////        vnpParams.put("vnp_TxnRef", vnpTxnRef);
////        vnpParams.put("vnp_OrderInfo", "Thanh toan don hang " + order.getId());
////        vnpParams.put("vnp_OrderType", "other");
////        vnpParams.put("vnp_Locale", "vn");
////        vnpParams.put("vnp_ReturnUrl", returnUrl);
////        vnpParams.put("vnp_IpAddr", vnpIpAddr);
////        vnpParams.put("vnp_CreateDate", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
//
//        // 1. Sắp xếp theo alphabet
//        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
//        Collections.sort(fieldNames);
//
//        // 2. Build raw hashData (không encode gì hết)
//        StringBuilder hashData = new StringBuilder();
//        StringBuilder query = new StringBuilder();
//
//        for (String fieldName : fieldNames) {
//            String fieldValue = vnpParams.get(fieldName);
//            if (fieldValue != null && !fieldValue.isEmpty()) {
//                // Append vào chuỗi hashData (không encode)
//                if (hashData.length() > 0) {
//                    hashData.append('&');
//                }
//                hashData.append(fieldName).append('=').append(fieldValue);
//
//                // Xây dựng query string (với URL encode)
//                if (query.length() > 0) {
//                    query.append('&');
//                }
//                try {
//                    query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8.toString()))
//                            .append('=')
//                            .append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
//                } catch (UnsupportedEncodingException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        }
//
//        // In ra chuỗi hashData để debug
//        System.out.println("HashData: " + hashData.toString());
//
//        // 3. Tính SecureHash
//        String vnpSecureHash = hmacSHA512(vnpHashSecret, hashData.toString());
//        System.out.println("SecureHash: " + vnpSecureHash);
//
//        // 4. Thêm SecureHash vào query
//        try {
//            query.append('&')
//                    .append(URLEncoder.encode("vnp_SecureHash", StandardCharsets.UTF_8.toString()))
//                    .append('=')
//                    .append(URLEncoder.encode(vnpSecureHash, StandardCharsets.UTF_8.toString()));
//        } catch (UnsupportedEncodingException e) {
//            throw new RuntimeException(e);
//        }
//
//        // 5. Build final URL
//        String paymentUrl = vnpUrl + "?" + query.toString();
//
//        // 6. Save Payment
//        Payment payment = Payment.builder()
//                .method("VNPAY")
//                .amount(request.getAmount())
//                .transactionStatus("PENDING")
//                .vnpTxnRef(vnpTxnRef)
//                .order(order)
//                .build();
//        paymentRepository.save(payment);
//
//        return PaymentResponse.builder()
//                .paymentUrl(paymentUrl)
//                .build();
    }

    @Transactional
    public void processPaymentCallback(Map<String, String> queryParams) {
        String vnpTxnRef = queryParams.get("vnp_TxnRef");
        String vnpResponseCode = queryParams.get("vnp_ResponseCode");
        String vnpTransactionNo = queryParams.get("vnp_TransactionNo");
        String vnpBankCode = queryParams.get("vnp_BankCode");
        String vnpPayDate = queryParams.get("vnp_PayDate");

        Payment payment = paymentRepository.findByVnpTxnRef(vnpTxnRef)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setVnpResponseCode(vnpResponseCode);
        payment.setBankCode(vnpBankCode);
        payment.setPaymentTime(vnpPayDate);

        if ("00".equals(vnpResponseCode)) {
            payment.setTransactionStatus("COMPLETED");
            payment.getOrder().setStatus("CONFIRMED");
        } else {
            payment.setTransactionStatus("FAILED");
            payment.getOrder().setStatus("CANCELLED");
        }

        paymentRepository.save(payment);
    }


} 