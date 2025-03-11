package org.example.nordicnestshop.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

@Tag(name = "Users", description = """
        Endpoints for managing user accounts, including retrieval,
        updates, role changes, and deletion.
        """)
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserMapper userMapper;
    private final UserService userService;

    @Operation(
            summary = "Get current user information",
            description = """
            Retrieves the details of the currently authenticated user.
            
            **Authorization:** Requires `USER` role.
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Successfully retrieved user details"),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized - User must be authenticated")
    })
    @PreAuthorize("hasAuthority('USER')")
    @GetMapping("/me")
    public UserDto getCurrentUserInfo() {
        User user = UserUtil.getAuthenticatedUser();
        return userMapper.toDto(user);
    }

    @Operation(
            summary = "Update current user information",
            description = """
            Updates the personal details of the currently authenticated user.
            
            **Authorization:** Requires `USER` role.
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Successfully updated user information"),
            @ApiResponse(responseCode = "400",
                    description = "Validation error in request body"),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized - User must be authenticated")
    })
    @PreAuthorize("hasAuthority('USER')")
    @PutMapping("/me")
    public UserDto updateUserInfo(
            @RequestBody @Valid UpdateUserInfoDto updateUserInfoDto) {
        User user = UserUtil.getAuthenticatedUser();
        return userService.updateUserInfo(user, updateUserInfoDto);
    }

    @Operation(
            summary = "Get all users",
            description = """
            Retrieves a paginated list of all registered users.
            
            **Authorization:** Requires `ADMIN` role.
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Successfully retrieved user list"),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden - Admin privileges required"),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized - User must be authenticated")
    })
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    public Page<UserDto> getAll(@Parameter(
            description = "Pagination and sorting parameters") Pageable pageable) {
        return userService.getAll(pageable);
    }

    @Operation(
            summary = "Update user role",
            description = """
            Modifies the role of a specific user.
            
            **Authorization:** Requires `ADMIN` role.
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Successfully updated user role"),
            @ApiResponse(responseCode = "400",
                    description = "Invalid role assignment"),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden - Admin privileges required"),
            @ApiResponse(responseCode = "404",
                    description = "User not found")
    })
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{id}/role")
    public UserDto updateUserRole(
            @Parameter(description = "ID of the user to update", example = "123")
            @PathVariable Long id,
            @RequestBody @Valid UpdateUserRoleDto updateUserRoleDto) {
        return userService.updateUserRole(id, updateUserRoleDto);
    }

    @Operation(
            summary = "Delete user",
            description = """
            Deletes a user by ID.

            **Authorization:** Requires `ADMIN` role.
            **Response:** Returns `204 No Content` if the deletion is successful.
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204",
                    description = "Successfully deleted user"),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden - Admin privileges required"),
            @ApiResponse(responseCode = "404",
                    description = "User not found")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteUser(
            @Parameter(description = "ID of the user to delete", example = "123")
            @PathVariable Long id) {
        userService.delete(id);
    }
}
