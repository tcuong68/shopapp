package com.devteria.identity_service.repository;

import com.devteria.identity_service.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    Optional<Payment> findByVnpTxnRef(String vnpTxnRef);
} 