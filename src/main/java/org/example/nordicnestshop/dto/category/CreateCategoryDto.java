package org.example.nordicnestshop.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.example.nordicnestshop.model.Category;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class CreateCategoryDto {
    @Size(min = 3, max = 255)
    @NotBlank
    private String title;

    @Size(max = 255)
    @NotBlank
    private String description;

    private MultipartFile image;

    private Category.CategoryType type;
}
