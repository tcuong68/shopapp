package com.devteria.identity_service.repository;

import com.devteria.identity_service.entity.Product;
import com.devteria.identity_service.entity.Review;
import com.devteria.identity_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    public List<Review> findByProductId(Long productId);
}
