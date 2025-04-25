package com.devteria.identity_service.mapper;

import com.devteria.identity_service.dto.response.ReviewProductResponse;
import com.devteria.identity_service.entity.Review;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    ReviewProductResponse toReviewResponse(Review review);
}
