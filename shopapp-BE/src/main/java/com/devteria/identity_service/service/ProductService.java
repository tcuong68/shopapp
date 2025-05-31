package com.devteria.identity_service.service;

import com.devteria.identity_service.dto.request.ProductCreationRequest;
import com.devteria.identity_service.dto.request.ProductSearchRequest;
import com.devteria.identity_service.dto.request.ProductUpdateRequest;
import com.devteria.identity_service.dto.request.ProductVariantRequest;
import com.devteria.identity_service.dto.response.PagedData;
import com.devteria.identity_service.dto.response.ProductResponse;
import com.devteria.identity_service.entity.Product;
import com.devteria.identity_service.entity.ProductVariant;
import com.devteria.identity_service.mapper.ProductMapper;
import com.devteria.identity_service.mapper.VariantMapper;
import com.devteria.identity_service.repository.CategoryRepository;
import com.devteria.identity_service.repository.ProductRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EnableMethodSecurity
public class ProductService {
    ProductRepository productRepository;
    CategoryRepository categoryRepository;
    ProductMapper productMapper;
    VariantMapper variantMapper;

    public ProductResponse createProduct(ProductCreationRequest request) {
        Product product = productMapper.toProduct(request);

        product.setCategory(categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found")));

        Set<ProductVariant> variants = request.getVariants()
                .stream()
                .map(variantReq -> {
                    ProductVariant variant = variantMapper.toProductVariant(variantReq);
                    variant.setProduct(product);
                    return variant;
                }).collect(Collectors.toSet());

        product.setVariants(variants);

        if (request.getImageUrls() != null) {
            product.setImageUrls(request.getImageUrls());
        }

        Product savedProduct = productRepository.save(product);
        return productMapper.toProductResponse(savedProduct);
    }

    private String normalize(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }


    public PagedData<ProductResponse> searchProduct(ProductSearchRequest request,Pageable pageable) {
        Page<Product> productPage = productRepository.searchProducts(
                normalize(request.getName()),
                normalize(request.getCategory()),
                normalize(request.getSize()),
                normalize(request.getColor()),
                request.getMinPrice(),
                request.getMaxPrice(),
                pageable);
        List<ProductResponse> productResponseList = productPage.getContent().stream().map(product -> {
            ProductResponse response = productMapper.toProductResponse(product);
            response.setCategoryId(product.getCategory().getId());
            response.setCategoryName(product.getCategory().getName());

            if (product.getVariants() != null) {
                response.setVariants(
                        product.getVariants().stream()
                                .map(variantMapper::toVariantResponse)
                                .collect(Collectors.toSet())
                );
            }

            return response;
        }).toList();
        return PagedData.<ProductResponse>builder()
                .content(productResponseList)
                .pageNo(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .last(productPage.isLast())
                .build();
    }

    public PagedData<ProductResponse> getAllProducts(Pageable pageable, String slug) {
        Page<Product> productPage;
        if (slug != null && !slug.isEmpty()) {
            productPage = productRepository.findByCategory_Slug(pageable, slug);
        } else {
            productPage = productRepository.findAll(pageable);
        }
        List<ProductResponse> productResponseList = productPage.getContent().stream().map(product -> {
            // Dùng mapper để map phần cơ bản
            ProductResponse response = productMapper.toProductResponse(product);

            // Set thêm categoryId
            response.setCategoryId(product.getCategory().getId());
            response.setCategoryName(product.getCategory().getName());

            // Set thêm variants
            if (product.getVariants() != null) {
                response.setVariants(
                        product.getVariants().stream()
                                .map(variantMapper::toVariantResponse)
                                .collect(Collectors.toSet())
                );
            }

            return response;
        }).toList();

        return PagedData.<ProductResponse>builder()
                .content(productResponseList)
                .pageNo(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .last(productPage.isLast())
                .build();
    }



    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Map cơ bản
        ProductResponse response = productMapper.toProductResponse(product);

        // Set categoryId
        response.setCategoryId(product.getCategory().getId());

        // Set variants nếu có
        if (product.getVariants() != null) {
            response.setVariants(
                    product.getVariants().stream()
                            .map(variantMapper::toVariantResponse)
                            .collect(Collectors.toSet())
            );
        }

        return response;
    }

    public ProductResponse updateProduct(ProductUpdateRequest request, Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));

        productMapper.updateProduct(request, product);

        if (request.getCategoryId() != null) {
            product.setCategory(categoryRepository.findById(request.getCategoryId()).orElseThrow(() -> new RuntimeException("Category not found")));
        }

        // Update images
        if (request.getImageUrls() != null) {
            product.setImageUrls(request.getImageUrls());
        }

        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            // Clear old variants correctly
            Set<ProductVariant> oldVariants = product.getVariants();
            oldVariants.forEach(variant -> variant.setProduct(null)); // remove reverse reference
            oldVariants.clear();

            // Add new variants
            for (ProductVariantRequest variantReq : request.getVariants()) {
                ProductVariant variant = variantMapper.toProductVariant(variantReq);
                variant.setProduct(product);
                oldVariants.add(variant); // add to same set
            }
        }

//        product.setVariants(updatedVariants);

        Product updatedProduct = productRepository.save(product);
        return productMapper.toProductResponse(updatedProduct);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
