package com.devteria.identity_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@ToString(exclude = {"products", "parent", "subCategories"}) // <-- sửa đúng các field vòng lặp
public class Category extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(nullable = false, unique = true)
    String name;

    @Column(nullable = false, unique = true)
    String slug;

    String description;

    @ManyToOne
    @JoinColumn(name = "parent_id") //  danh mục cha
    Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    List<Category> subCategories = new ArrayList<>(); // Danh mục con

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<Product> products = new ArrayList<>(); // Liên kết với sản phẩm
}


