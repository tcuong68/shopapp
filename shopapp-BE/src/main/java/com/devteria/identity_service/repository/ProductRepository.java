package com.devteria.identity_service.repository;

import com.devteria.identity_service.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByCategory_Slug(Pageable pageable, String slug);
    @Query("""
        SELECT DISTINCT p FROM Product p
        JOIN p.variants v
        WHERE (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
          AND (:category IS NULL OR (p.category.slug) like (:category))
          AND (:size IS NULL OR LOWER(v.size) = LOWER(:size))
          AND (:color IS NULL OR LOWER(v.color) = LOWER(:color))
          AND (:minPrice IS NULL OR v.price >= :minPrice)
          AND (:maxPrice IS NULL OR v.price <= :maxPrice)
    """)
    Page<Product> searchProducts(
            @Param("name") String name,
            @Param("category") String category,
            @Param("size") String size,
            @Param("color") String color,
            @Param("minPrice") Long minPrice,
            @Param("maxPrice") Long maxPrice,
             Pageable pageable
    );
}

