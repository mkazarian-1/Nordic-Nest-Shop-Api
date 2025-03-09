package org.example.nordicnestshop.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.nordicnestshop.dto.user.UserLoginRequestDto;
import org.example.nordicnestshop.dto.user.UserLoginResponseDto;
import org.example.nordicnestshop.dto.user.UserRegistrationRequestDto;
import org.example.nordicnestshop.dto.user.UserRegistrationResponseDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthenticationControllerTest {

    private static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(@Autowired WebApplicationContext applicationContext) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("Should register a new user successfully")
    @Sql(scripts = "classpath:users/add-user-without-id.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:users/delete-users.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void registerUser_CorrectData_Success() throws Exception {
        // Given
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("test@gmail.com");
        requestDto.setPassword("SecureP@ssword123");
        requestDto.setRepeatPassword("SecureP@ssword123");
        requestDto.setFirstName("John");
        requestDto.setSecondName("Doe");

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When
        MvcResult mvcResult = mockMvc.perform(
                        post("/auth/registration")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        // Then
        UserRegistrationResponseDto responseDto = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                UserRegistrationResponseDto.class
        );

        Assertions.assertNotNull(responseDto);
        Assertions.assertNotNull(responseDto.getId());
        Assertions.assertEquals(requestDto.getEmail(), responseDto.getEmail());
    }

    @Test
    @DisplayName("Should fail registration with duplicate email")
    @Sql(scripts = "classpath:users/add-user-without-id.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:users/delete-users.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void registerUser_DuplicateEmail_Failure() throws Exception {
        // Given
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("Lanot@gmail.com");
        requestDto.setPassword("Password123");
        requestDto.setRepeatPassword("Password123");
        requestDto.setFirstName("Jane");
        requestDto.setSecondName("Smith");

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When
        mockMvc.perform(
                        post("/auth/registration")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should login successfully and return JWT token")
    @Sql(scripts = "classpath:users/add-user-without-id.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:users/delete-users.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void login_CorrectCredentials_Success() throws Exception {
        // Given
        UserLoginRequestDto loginRequestDto = new UserLoginRequestDto();
        loginRequestDto.setEmail("Lanot@gmail.com");
        loginRequestDto.setPassword("password12345");

        String jsonRequest = objectMapper.writeValueAsString(loginRequestDto);

        // When
        MvcResult mvcResult = mockMvc.perform(
                        post("/auth/login")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        UserLoginResponseDto responseDto = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                UserLoginResponseDto.class
        );

        Assertions.assertNotNull(responseDto);
        Assertions.assertNotNull(responseDto.getToken());
    }

    @Test
    @DisplayName("Should fail login with incorrect credentials")
    @Sql(scripts = "classpath:users/add-user-without-id.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:users/delete-users.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void login_IncorrectCredentials_Failure() throws Exception {
        // Given
        UserLoginRequestDto loginRequestDto = new UserLoginRequestDto();
        loginRequestDto.setEmail("Lanot@gmail.com");
        loginRequestDto.setPassword("WrongPassword");

        String jsonRequest = objectMapper.writeValueAsString(loginRequestDto);

        // When
        mockMvc.perform(
                        post("/auth/login")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
