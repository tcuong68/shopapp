package com.devteria.identity_service.service;

import com.devteria.identity_service.config.VNPayConfig;
import com.devteria.identity_service.dto.request.PaymentRequest;
import com.devteria.identity_service.dto.response.PaymentResponse;
import com.devteria.identity_service.entity.Orders;
import com.devteria.identity_service.entity.Payment;
import com.devteria.identity_service.repository.OrderRepository;
import com.devteria.identity_service.repository.PaymentRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VNPayService {
    VNPayConfig vnPayConfig;
    PaymentRepository paymentRepository;
    OrderRepository orderRepository;

    public PaymentResponse createPaymentUrl(PaymentRequest request) {
        Orders order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        String vnpTxnRef = generateTransactionRef();
        String vnpIpAddr = vnPayConfig.getIpAddr();
        String vnpTmnCode = vnPayConfig.getTmnCode();
        String vnpHashSecret = vnPayConfig.getHashSecret();
        String vnpUrl = vnPayConfig.getVnpUrl();
        String returnUrl = vnPayConfig.getReturnUrl();

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnpTmnCode);
        vnpParams.put("vnp_Amount", String.valueOf(request.getAmount() * 100));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", vnpTxnRef);
        vnpParams.put("vnp_OrderInfo", "Thanh toan don hang " + order.getId());
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", returnUrl);
        vnpParams.put("vnp_IpAddr", vnpIpAddr);
        vnpParams.put("vnp_CreateDate", new Date().toString());

        List fieldNames = new ArrayList(vnpParams.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnpParams.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                try {
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnpSecureHash = hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnpSecureHash;
        String paymentUrl = vnPayConfig.getVnpUrl() + "?" + queryUrl;

        // Save payment information
        Payment payment = Payment.builder()
                .method("VNPAY")
                .amount(request.getAmount())
                .transactionStatus("PENDING")
                .vnpTxnRef(vnpTxnRef)
                .order(order)
                .build();
        paymentRepository.save(payment);

        return PaymentResponse.builder()
                .paymentUrl(paymentUrl)
                .build();
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

    private String generateTransactionRef() {
        return "TXN" + System.currentTimeMillis();
    }

    private String hmacSHA512(String key, String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] hmac = digest.digest((key + data).getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hmac) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
} 