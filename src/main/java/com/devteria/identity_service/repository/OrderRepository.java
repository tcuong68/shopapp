package com.devteria.identity_service.repository;

import com.devteria.identity_service.entity.Orders;
import com.devteria.identity_service.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Long> {
    public List<Orders> findAllByUser(User user, Pageable pageable);
    @Query("""
        SELECT o FROM Orders o
        JOIN o.user u
        WHERE (:username IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%')))
          AND (:status IS NULL OR LOWER(o.status) = LOWER(:status))
          AND (:phoneNumber IS NULL OR o.phoneNumber LIKE CONCAT('%', :phoneNumber, '%'))
          AND (:address IS NULL OR LOWER(o.address) LIKE LOWER(CONCAT('%', :address, '%')))
          AND (:minTotalPrice IS NULL OR o.totalPrice >= :minTotalPrice)
          AND (:maxTotalPrice IS NULL OR o.totalPrice <= :maxTotalPrice)
    """)
    Page<Orders> searchOrders(
            @Param("username") String username,
            @Param("status") String status,
            @Param("phoneNumber") String phoneNumber,
            @Param("address") String address,
            @Param("minTotalPrice") Long minTotalPrice,
            @Param("maxTotalPrice") Long maxTotalPrice,
            Pageable pageable
    );
}
