package org.example.nordicnestshop.service;

import org.example.nordicnestshop.dto.user.UpdateUserInfoDto;
import org.example.nordicnestshop.dto.user.UpdateUserRoleDto;
import org.example.nordicnestshop.dto.user.UserDto;
import org.example.nordicnestshop.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserDto updateUserRole(Long id, UpdateUserRoleDto updateUserRoleDto);

    UserDto updateUserInfo(User user, UpdateUserInfoDto updateUserInfoDto);

    Page<UserDto> getAll(Pageable pageable);

    void delete(Long id);
}
