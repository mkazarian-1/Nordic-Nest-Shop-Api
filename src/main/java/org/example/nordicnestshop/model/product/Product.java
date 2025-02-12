package org.example.nordicnestshop.model.product;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.example.nordicnestshop.model.Category;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Size(min = 3, max = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, unique = true)
    @Size(min = 3, max = 255)
    private String article;

    @Column(nullable = false, scale = 2, precision = 10)
    @DecimalMin(value = "0.01")
    private BigDecimal price;

    @OneToMany(mappedBy = "product",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<ProductImage> images;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "product_category",
            joinColumns = @JoinColumn(name = "product_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "category_id", nullable = false)
    )
    private Set<Category> categories;

    @OneToMany(mappedBy = "product",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private Set<Attribute> attributes;
}
