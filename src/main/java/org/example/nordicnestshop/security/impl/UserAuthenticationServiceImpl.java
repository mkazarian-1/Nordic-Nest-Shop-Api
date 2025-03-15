package org.example.nordicnestshop.security.impl;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.example.nordicnestshop.dto.user.UserLoginRequestDto;
import org.example.nordicnestshop.dto.user.UserLoginResponseDto;
import org.example.nordicnestshop.dto.user.UserRegistrationRequestDto;
import org.example.nordicnestshop.dto.user.UserRegistrationResponseDto;
import org.example.nordicnestshop.exception.RegistrationException;
import org.example.nordicnestshop.mapper.UserMapper;
import org.example.nordicnestshop.model.User;
import org.example.nordicnestshop.model.enums.UserRole;
import org.example.nordicnestshop.repository.UserRepository;
import org.example.nordicnestshop.security.UserAuthenticationService;
import org.example.nordicnestshop.security.util.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAuthenticationServiceImpl implements UserAuthenticationService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    public UserRegistrationResponseDto register(UserRegistrationRequestDto requestDto) {
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new RegistrationException("User with this email already exist");
        }

        User user = userMapper.toUser(requestDto);
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        user.setRoles(Set.of(UserRole.USER));

        user = userRepository.save(user);

        return userMapper.toRegistrationResponseDto(user);
    }

    @Override
    public UserLoginResponseDto login(UserLoginRequestDto loginRequestDto) {
        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getEmail(), loginRequestDto.getPassword())
        );

        UserLoginResponseDto userLoginResponseDto = new UserLoginResponseDto();
        userLoginResponseDto.setToken(jwtUtil.generateToken(authentication.getName()));
        return userLoginResponseDto;
    }
}
