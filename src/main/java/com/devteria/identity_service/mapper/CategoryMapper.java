package com.devteria.identity_service.mapper;

import com.devteria.identity_service.dto.request.CategoryRequest;
import com.devteria.identity_service.dto.response.CategoryResponse;
import com.devteria.identity_service.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    @Mapping(target = "parentId", ignore = true)
    CategoryResponse categoryToCategoryResponse(Category category);

    Category categoryRequestToCategory(CategoryRequest request);
}
