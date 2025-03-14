package org.example.nordicnestshop.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegistrationResponseDto {
    private Long id;
    private String email;
    private String firstName;
    private String secondName;
}
