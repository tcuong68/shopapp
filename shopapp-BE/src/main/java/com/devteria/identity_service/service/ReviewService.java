package com.devteria.identity_service.service;


import com.devteria.identity_service.dto.request.ReviewProductRequest;
import com.devteria.identity_service.dto.response.ApiResponse;
import com.devteria.identity_service.dto.response.ReviewProductResponse;
import com.devteria.identity_service.entity.Product;
import com.devteria.identity_service.entity.Review;
import com.devteria.identity_service.entity.User;
import com.devteria.identity_service.mapper.ReviewMapper;
import com.devteria.identity_service.repository.ProductRepository;
import com.devteria.identity_service.repository.ReviewRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EnableMethodSecurity
public class ReviewService {

    ReviewRepository reviewRepository;
    ProductRepository productRepository;
    UserService userService;
    ReviewMapper reviewMapper;

    public ReviewProductResponse createReviewProduct(ReviewProductRequest request) {
        Product product = productRepository.findById(request.getProductId()).orElseThrow(()->new RuntimeException("Product not found"));
        User user = userService.getCurrentUser();

        Review review = Review.builder()
                .product(product)
                .comment(request.getComment())
                .user(user)
                .rating(request.getRating())
                .build();

        reviewRepository.save(review);

        return reviewMapper.toReviewResponse(review);
    }

    public List<ReviewProductResponse> getReviewsByProduct(Long productId) {
        return reviewRepository.findByProductId(productId).stream()
                .map(reviewMapper::toReviewResponse)
                .collect(Collectors.toList());
    }

    public void deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }
}
