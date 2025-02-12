package org.example.nordicnestshop.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Size(min = 3, max = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 2048)
    @Size(max = 2048)
    @Pattern(regexp = "^(https?|ftp)://.*$", message = "Invalid image URL format.")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryType type;

    public enum CategoryType {
        TYPE, ROOM, DESIGN
    }
}
