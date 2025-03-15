package org.example.nordicnestshop.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.example.nordicnestshop.dto.validator.FieldMatch;

@Getter
@Setter
@FieldMatch(first = "password", second = "repeatPassword",message = "Passwords do not match")
public class UserRegistrationRequestDto {
    @NotBlank
    @Email
    private String email;
    @NotBlank
    @Size(max = 255)
    private String password;
    @NotBlank
    @Size(max = 255)
    private String repeatPassword;
    @NotBlank
    @Size(max = 255)
    private String firstName;
    @NotBlank
    @Size(max = 255)
    private String secondName;
}
