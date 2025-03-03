package org.example.nordicnestshop.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.nordicnestshop.dto.user.UpdateUserInfoDto;
import org.example.nordicnestshop.dto.user.UpdateUserRoleDto;
import org.example.nordicnestshop.dto.user.UserDto;
import org.example.nordicnestshop.mapper.UserMapper;
import org.example.nordicnestshop.model.User;
import org.example.nordicnestshop.security.util.UserUtil;
import org.example.nordicnestshop.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Users", description = "Endpoints for managing users")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserMapper userMapper;
    private final UserService userService;

    @Operation(summary = "Get current user information",
            description = "Retrieves the details of the currently authenticated user.")
    @PreAuthorize("hasAuthority('USER')")
    @GetMapping("/me")
    public UserDto getCurrentUserInfo() {
        User user = UserUtil.getAuthenticatedUser();
        return userMapper.toDto(user);
    }

    @Operation(summary = "Update current user information",
            description = "Updates the personal information of the currently authenticated user.")
    @PreAuthorize("hasAuthority('USER')")
    @PutMapping("/me")
    public UserDto updateUserInfo(@RequestBody @Valid UpdateUserInfoDto updateUserInfoDto) {
        User user = UserUtil.getAuthenticatedUser();
        return userService.updateUserInfo(user, updateUserInfoDto);
    }

    @Operation(summary = "Get all user information",
            description = """
                    Retrieves the details of the currently authenticated user.
                    Necessary role: ADMIN.
                    """)
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    public Page<UserDto> getAll(Pageable pageable) {
        return userService.getAll(pageable);
    }

    @Operation(summary = "Update user role",
            description = "Updates the role of a specific user. Necessary role: ADMIN.")
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{id}/role")
    public UserDto updateUserRole(@PathVariable Long id,
                                  @RequestBody @Valid UpdateUserRoleDto updateUserRoleDto) {
        return userService.updateUserRole(id, updateUserRoleDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Delete user",
            description = """
                    Return 204 status if delete went well
                    \nNecessary role: ADMIN
                    """)
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.delete(id);
    }
}
