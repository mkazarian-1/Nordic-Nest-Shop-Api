package org.example.nordicnestshop.dto.category;

import lombok.Getter;
import lombok.Setter;
import org.example.nordicnestshop.model.Category;

@Getter
@Setter
public class CategoryDto {
    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private Category.CategoryType type;
}
