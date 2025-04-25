package com.devteria.identity_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class CartItem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    // Liên kết với Cart: Một giỏ hàng có nhiều CartItem
    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    Cart cart;

    // Liên kết với ProductVariant: Mỗi CartItem chỉ tham chiếu đến 1 ProductVariant
    @ManyToOne
    @JoinColumn(name = "product_variant_id", nullable = false)
    ProductVariant productVariant;

    // Số lượng sản phẩm trong giỏ hàng
    int quantity;
}
