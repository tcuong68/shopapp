package com.devteria.identity_service.service;

import com.devteria.identity_service.dto.request.CategoryRequest;
import com.devteria.identity_service.dto.response.CategoryResponse;
import com.devteria.identity_service.dto.response.PagedData;
import com.devteria.identity_service.entity.Category;
import com.devteria.identity_service.mapper.CategoryMapper;
import com.devteria.identity_service.repository.CategoryRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EnableMethodSecurity
public class CategoryService {
    CategoryRepository categoryRepository;
    CategoryMapper categoryMapper;


    public CategoryResponse createCategory(CategoryRequest request) {
        Category category = categoryMapper.categoryRequestToCategory(request);
//        if(request.getParentId() != null ) {
//            Category parent = categoryRepository.findById(request.getParentId())
//                    .orElseThrow(() -> new RuntimeException("Parent not found"));
//            category.setParent(parent);
//        }
        CategoryResponse categoryResponse = categoryMapper.categoryToCategoryResponse(categoryRepository.save(category));
        if(category.getParent() != null) {
            categoryResponse.setParentId(category.getParent().getId());
        }
        return categoryResponse;
    }

    public PagedData<CategoryResponse> getAllCategories(Pageable pageable) {
        Page<Category> categoryPage = categoryRepository.findAll(pageable);

        List<CategoryResponse> responses = categoryPage.getContent().stream()
                .map(category -> {
                    CategoryResponse response = categoryMapper.categoryToCategoryResponse(category);
                    if (category.getParent() != null) {
                        response.setParentId(category.getParent().getId());
                    }
                    return response;
                })
                .collect(Collectors.toList());

        return PagedData.<CategoryResponse>builder()
                .content(responses)
                .pageNo(categoryPage.getNumber())
                .pageSize(categoryPage.getSize())
                .totalElements(categoryPage.getTotalElements())
                .totalPages(categoryPage.getTotalPages())
                .last(categoryPage.isLast())
                .build();
    }

    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        CategoryResponse response = categoryMapper.categoryToCategoryResponse(category);
        if (category.getParent() != null) {
            response.setParentId(category.getParent().getId());
        }
        return response;
    }

    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setSlug(request.getSlug());
//
//        if (request.getParentId() != null
//                && (category.getParent() == null || !category.getParent().getId().equals(request.getParentId()))
//                && !category.getId().equals(request.getParentId())) {
//
//            Category parent = categoryRepository.findById(request.getParentId())
//                    .orElseThrow(() -> new RuntimeException("Parent not found"));
//            category.setParent(parent);
//        }

        Category updated = categoryRepository.save(category);
        CategoryResponse response = categoryMapper.categoryToCategoryResponse(updated);
        if (updated.getParent() != null) {
            response.setParentId(updated.getParent().getId());
        }
        return response;
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

}
