package com.devteria.identity_service.controller;

import com.devteria.identity_service.dto.request.ReviewProductRequest;
import com.devteria.identity_service.dto.response.ApiResponse;
import com.devteria.identity_service.dto.response.ReviewProductResponse;
import com.devteria.identity_service.service.ReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@Slf4j
public class ReviewController {
    @Autowired
    private ReviewService reviewService;

    @PostMapping
    public ApiResponse<ReviewProductResponse> createReview(@RequestBody ReviewProductRequest request) {
        return ApiResponse.<ReviewProductResponse>builder()
                .result(reviewService.createReviewProduct(request))
                .build();
    }

    @GetMapping("/product/{productId}")
    public ApiResponse<List<ReviewProductResponse>> getReviewProducts(@PathVariable("productId") Long productId) {
        return ApiResponse.<List<ReviewProductResponse>>builder()
                .result(reviewService.getReviewsByProduct(productId))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteReview(@PathVariable("id") Long id) {
        reviewService.deleteReview(id);
        return ApiResponse.<String>builder()
                .result("Delete successful!")
                .build();
    }
}
