package org.example.nordicnestshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.nordicnestshop.dto.user.UpdateUserInfoDto;
import org.example.nordicnestshop.dto.user.UpdateUserRoleDto;
import org.example.nordicnestshop.dto.user.UserDto;
import org.example.nordicnestshop.exception.ElementNotFoundException;
import org.example.nordicnestshop.mapper.UserMapper;
import org.example.nordicnestshop.model.User;
import org.example.nordicnestshop.repository.UserRepository;
import org.example.nordicnestshop.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    @Override
    public UserDto updateUserRole(Long id, UpdateUserRoleDto updateUserRoleDto) {
        User user = userRepository.findUserById(id).orElseThrow(
                () -> new ElementNotFoundException("Can't find user by id: " + id));
        userMapper.updateRole(user, updateUserRoleDto);
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public UserDto updateUserInfo(User user, UpdateUserInfoDto updateUserInfoDto) {
        userMapper.updateInfo(user, updateUserInfoDto);
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public Page<UserDto> getAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        User user = userRepository.findUserById(id).orElseThrow(
                () -> new ElementNotFoundException("Can't find user by id: " + id));

        userRepository.delete(user);
    }
}
