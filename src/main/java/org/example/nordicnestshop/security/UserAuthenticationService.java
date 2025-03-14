package org.example.nordicnestshop.security;

import org.example.nordicnestshop.dto.user.UserLoginRequestDto;
import org.example.nordicnestshop.dto.user.UserLoginResponseDto;
import org.example.nordicnestshop.dto.user.UserRegistrationRequestDto;
import org.example.nordicnestshop.dto.user.UserRegistrationResponseDto;

public interface UserAuthenticationService {
    UserRegistrationResponseDto register(UserRegistrationRequestDto requestDto);

    UserLoginResponseDto login(UserLoginRequestDto loginRequestDto);
}
