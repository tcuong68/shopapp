package com.devteria.identity_service.repository;

import com.devteria.identity_service.entity.Orders;
import com.devteria.identity_service.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Long> {
    Page<Orders> findAllByUser(User user, Pageable pageable);



    @Query("""
    SELECT o FROM Orders o
    WHERE (:id IS NULL OR o.id = :id)
      AND (:status IS NULL OR LOWER(o.status) = LOWER(:status))
      AND (:created_at IS NULL OR DATE(o.createdAt) = DATE(:created_at))
      AND (:minTotalPrice IS NULL OR o.totalPrice >= :minTotalPrice)
      AND (:maxTotalPrice IS NULL OR o.totalPrice <= :maxTotalPrice)
""")
    Page<Orders> searchOrders(
            @Param("id") Long id,
            @Param("status") String status,
            @Param("created_at") Date created_at,
            @Param("minTotalPrice") Long minTotalPrice,
            @Param("maxTotalPrice") Long maxTotalPrice,
            Pageable pageable
    );
}
