package org.example.nordicnestshop.dto.user;

import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.example.nordicnestshop.model.enums.UserRole;

@Getter
@Setter
public class UserDto {
    private Long id;
    private String email;
    private String firstName;
    private String secondName;
    private Set<UserRole> roles;
}
