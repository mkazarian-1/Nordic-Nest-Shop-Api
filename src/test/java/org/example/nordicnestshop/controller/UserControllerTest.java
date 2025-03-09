package org.example.nordicnestshop.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import org.example.nordicnestshop.dto.user.UpdateUserInfoDto;
import org.example.nordicnestshop.dto.user.UpdateUserRoleDto;
import org.example.nordicnestshop.dto.user.UserDto;
import org.example.nordicnestshop.model.enums.UserRole;
import org.example.nordicnestshop.repository.UserRepository;
import org.example.nordicnestshop.test.utils.CustomPageImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerTest {
    private static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeAll
    static void beforeAll(@Autowired WebApplicationContext applicationContext) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("Should return current user info")
    @WithUserDetails(value = "Lanot@gmail.com")
    @Sql(scripts = "classpath:users/add-user.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:users/delete-users.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getCurrentUserInfo_Success() throws Exception {
        // When
        MvcResult mvcResult = mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        UserDto actual = objectMapper
                .readValue(mvcResult.getResponse().getContentAsString(), UserDto.class);
        Assertions.assertNotNull(actual);
        Assertions.assertEquals("Lanot@gmail.com", actual.getEmail());
    }

    @Test
    @DisplayName("Should update user role")
    @WithUserDetails(value = "Lanot@gmail.com-MANAGER")
    @Sql(scripts = "classpath:users/add-user.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:users/delete-users.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateUserRole_Success() throws Exception {
        UpdateUserRoleDto requestDto = new UpdateUserRoleDto();
        requestDto.setRoles(Set.of(UserRole.ADMIN,UserRole.USER));
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When
        MvcResult mvcResult = mockMvc.perform(put("/users/3/role")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        UserDto actual = objectMapper
                .readValue(mvcResult.getResponse().getContentAsString(), UserDto.class);
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(
                Set.of(UserRole.ADMIN,UserRole.USER), actual.getRoles());
    }

    @Test
    @DisplayName("Should return 403 for unauthorized access to update role")
    @WithUserDetails(value = "Lanot@gmail.com")
    @Sql(scripts = "classpath:users/add-user.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:users/delete-users.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateUserRole_Unauthorized() throws Exception {
        UpdateUserRoleDto requestDto = new UpdateUserRoleDto();
        requestDto.setRoles(Set.of(UserRole.ADMIN,UserRole.USER));
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When / Then
        mockMvc.perform(put("/users/2/role")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 404 for non-existent user when updating role")
    @WithUserDetails(value = "Lanot@gmail.com-MANAGER")
    @Sql(scripts = "classpath:users/add-user.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:users/delete-users.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateUserRole_NotFound() throws Exception {
        UpdateUserRoleDto requestDto = new UpdateUserRoleDto();
        requestDto.setRoles(Set.of(UserRole.ADMIN,UserRole.USER));
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When / Then
        mockMvc.perform(put("/users/999/role")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 for invalid input when updating current user info")
    @WithUserDetails(value = "Lanot@gmail.com")
    @Sql(scripts = "classpath:users/add-user.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:users/delete-users.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateUserInfo_BadRequest() throws Exception {
        UpdateUserInfoDto requestDto = new UpdateUserInfoDto();
        requestDto.setSecondName("");
        requestDto.setFirstName("");
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When / Then
        mockMvc.perform(put("/users/me")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Sql(scripts = {
            "classpath:users/delete-users.sql",
            "classpath:users/add-user.sql",
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:users/delete-users.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithUserDetails(value = "Lanot@gmail.com-MANAGER")
    void getAll_CorrectData_Success() throws Exception {
        //When
        MvcResult mvcResult = mockMvc
                .perform(
                        get("/users")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andReturn();

        //Then
        CustomPageImpl<UserDto> actual = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                objectMapper.getTypeFactory()
                        .constructParametricType(CustomPageImpl.class, UserDto.class)
        );

        Assertions.assertEquals(2, actual.getContent().size());
    }

    @Test
    @DisplayName("Should update current user info")
    @Sql(scripts = "classpath:users/add-user.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:users/delete-users.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithUserDetails(value = "Lanot@gmail.com")
    void updateUserInfo_Success() throws Exception {
        UpdateUserInfoDto requestDto = new UpdateUserInfoDto();
        requestDto.setFirstName("John");
        requestDto.setSecondName("Doe");
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When
        MvcResult mvcResult = mockMvc.perform(put("/users/me")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        UserDto actual = objectMapper
                .readValue(mvcResult.getResponse().getContentAsString(), UserDto.class);
        Assertions.assertNotNull(actual);
        Assertions.assertEquals("John", actual.getFirstName());
        Assertions.assertEquals("Doe", actual.getSecondName());
    }

    @Test
    @DisplayName("Should update current user info")
    @Sql(scripts = "classpath:users/add-user.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:users/delete-users.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithUserDetails(value = "Lanot@gmail.com-MANAGER")
    void deleteUser_CorrectData_Success() throws Exception {
        Assertions.assertTrue(userRepository.findById(3L).isPresent());
        mockMvc.perform(
                delete("/users/3")
        ).andExpect(status().isNoContent()).andReturn();
        Assertions.assertTrue(userRepository.findById(3L).isEmpty());
    }

    @Test
    @DisplayName("Should update current user info")
    @Sql(scripts = "classpath:users/add-user.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:users/delete-users.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithUserDetails(value = "Lanot@gmail.com-MANAGER")
    void deleteUser_InCorrectData_NotFound() throws Exception {
        mockMvc.perform(
                delete("/users/30")
        ).andExpect(status().isNotFound()).andReturn();
    }
}
