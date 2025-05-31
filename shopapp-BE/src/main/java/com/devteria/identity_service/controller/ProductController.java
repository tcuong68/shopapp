package com.devteria.identity_service.controller;

import com.devteria.identity_service.dto.request.ProductCreationRequest;
import com.devteria.identity_service.dto.request.ProductSearchRequest;
import com.devteria.identity_service.dto.request.ProductUpdateRequest;
import com.devteria.identity_service.dto.response.ApiResponse;
import com.devteria.identity_service.dto.response.PagedData;
import com.devteria.identity_service.dto.response.ProductResponse;
import com.devteria.identity_service.service.ImageStorageService;
import com.devteria.identity_service.service.ProductService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/products")
@Slf4j
//@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ProductController {
    @Autowired
    private ProductService productService;
    @Autowired
    private ImageStorageService imageStorageService;


    @PostMapping("/search")
    public ApiResponse<PagedData<ProductResponse>> searchProduct(@RequestBody @Valid ProductSearchRequest request,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "10") int size,
                                                                @RequestParam(defaultValue = "id") String sortBy,
                                                                @RequestParam(defaultValue = "asc") String sortDir){
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        PagedData<ProductResponse> pagedData = productService.searchProduct(request,pageable);

        return ApiResponse.<PagedData<ProductResponse>>builder()
                .result(pagedData)
                .build();
    }

        @GetMapping()
        public ApiResponse<PagedData<ProductResponse>> getAllProducts(@RequestParam(defaultValue = "0") int page,
                                                                      @RequestParam(defaultValue = "10") int size,
                                                                      @RequestParam(defaultValue = "id") String sortBy,
                                                                      @RequestParam(defaultValue = "asc") String sortDir,
                                                                      @RequestParam(required = false) String slug) {
            Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                    Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

            Pageable pageable = PageRequest.of(page, size, sort);

            PagedData<ProductResponse> pagedData = productService.getAllProducts(pageable, slug);

            return ApiResponse.<PagedData<ProductResponse>>builder()
                    .result(pagedData)
                    .message("Products retrieved successfully")
                    .build();
        }

    @GetMapping("/detail/{productId}")
    public ApiResponse<ProductResponse> getProduct(@PathVariable long productId) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.getProductById(productId))
                .build();
    }
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ProductResponse> createProduct(@RequestPart("product") @Valid ProductCreationRequest request,
                                                      @RequestPart("images") MultipartFile[] images) {
        List<String> imageUrls = Arrays.stream(images)
                .map(imageStorageService::saveImageAndReturnUrl)
                .collect(Collectors.toList());

        request.setImageUrls(imageUrls);

        return ApiResponse.<ProductResponse>builder()
                .result(productService.createProduct(request))
                .build();
    }
    @PutMapping("/{productId}")
    public ApiResponse<ProductResponse> updateProduct(@RequestPart("product") ProductUpdateRequest request,
                                                      @PathVariable long productId,
                                                      @RequestPart(value = "images", required = false) MultipartFile[] images) {
        List<String> imageUrls = new ArrayList<>();
        if (images != null && images.length > 0) {
            imageUrls = Arrays.stream(images)
                    .map(imageStorageService::saveImageAndReturnUrl)
                    .collect(Collectors.toList());
        }
        request.setImageUrls(imageUrls);

        return ApiResponse.<ProductResponse>builder()
                .result(productService.updateProduct(request, productId))
                .build();
    }

    @DeleteMapping("/{productId}")
    public ApiResponse<Void> deleteProduct(@PathVariable long productId) {
        productService.deleteProduct(productId);
        return ApiResponse.<Void>builder()
                .message("Product deleted successfully")
                .build();
    }
}
