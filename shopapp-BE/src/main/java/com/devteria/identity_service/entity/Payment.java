package com.devteria.identity_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Payment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    String method;

    Long amount;

    String transactionStatus;

    String vnpTxnRef; // Mã giao dịch VNPAY

    String vnpResponseCode; // Mã response từ VNPAY

    String bankCode; // BankCode

    String VnpTransactionNo;

    String paymentTime; // Thời gian VNPAY confirm thanh toán


    @OneToOne
    @JoinColumn(name = "order_id")
    Orders order;
}
