package org.example.nordicnestshop.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.nordicnestshop.dto.category.CategoryDto;
import org.example.nordicnestshop.model.Category;
import org.example.nordicnestshop.repository.CategoryRepository;
import org.example.nordicnestshop.test.utils.CustomPageImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {
    @TestConfiguration
    static class TestAppConfig {
        @Bean
        @Primary
        public S3Client s3Client() {
            S3Client mockS3Client = Mockito.mock(S3Client.class);

            // Mock the putObject method to do nothing
            when(mockS3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenReturn(PutObjectResponse.builder().build());

            // Mock the deleteObject method to do nothing
            when(mockS3Client.deleteObject(any(DeleteObjectRequest.class)))
                    .thenReturn(DeleteObjectResponse.builder().build());

            // For getObject or other methods if needed
            // when(mockS3Client.getObject(...)).thenReturn(...);

            return mockS3Client;
        }
    }

    private static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeAll
    static void beforeAll(
            @Autowired WebApplicationContext applicationContext) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    @Sql(scripts = "classpath:category/delete-categories.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @Test
    void createCategory_whenValidRequest_thenReturnCreatedCategory() throws Exception {
        // Arrange
        String title = "Test Category";
        String description = "Test Description";
        Category.CategoryType type = Category.CategoryType.DESIGN;

        // Create a real mock multipart file
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        // Act & Assert
        MvcResult mvcResult = mockMvc.perform(multipart("/categories")
                        .file(imageFile)
                        .param("title", title)
                        .param("description", description)
                        .param("type", type.toString())
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated()).andReturn();

        CategoryDto actual = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                CategoryDto.class);

        Assertions.assertEquals(title, actual.getTitle());
        Assertions.assertEquals(description, actual.getDescription());
        Assertions.assertEquals(type, actual.getType());
        Assertions.assertFalse(actual.getImageUrl().isBlank());
        Assertions.assertEquals(categoryRepository.findByTitle(title).get().getTitle(),
                actual.getTitle());
    }

    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    @Test
    void createCategory_withInvalidData_thenReturnBadRequest() throws Exception {
        // Arrange - empty title (which violates @NotBlank constraint)
        String title = "";
        String description = "Test Description";
        Category.CategoryType type = Category.CategoryType.DESIGN;

        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        // Act & Assert
        mockMvc.perform(multipart("/categories")
                        .file(imageFile)
                        .param("title", title)
                        .param("description", description)
                        .param("type", type.toString())
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"USER"})
    void createCategory_whenUnauthorized_thenReturnUnauthorized() throws Exception {
        // Test without the @WithMockUser annotation
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/categories")
                        .file(imageFile)
                        .param("title", "Test Title")
                        .param("description", "Test Description")
                        .param("type", Category.CategoryType.DESIGN.toString())
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isUnauthorized());
    }

    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    @Sql(scripts = "classpath:category/add-categories.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:category/delete-categories.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @Test
    void updateCategory_whenValidRequest_thenReturnCreatedCategory() throws Exception {
        // Arrange
        String title = "Test Category";
        String description = "Test Description";
        Category.CategoryType type = Category.CategoryType.DESIGN;

        // Create a real mock multipart file
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        // Act & Assert
        MvcResult mvcResult = mockMvc.perform(multipart("/categories/3")
                        .file(imageFile)
                        .param("title", title)
                        .param("description", description)
                        .param("type", type.toString())
                        .with(csrf())
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk()).andReturn();

        CategoryDto actual = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                CategoryDto.class);

        Assertions.assertEquals(title, actual.getTitle());
        Assertions.assertEquals(description, actual.getDescription());
        Assertions.assertEquals(type, actual.getType());
        Assertions.assertFalse(actual.getImageUrl().isBlank());
        Assertions.assertEquals(categoryRepository.findById(3L).get().getTitle(),
                actual.getTitle());
    }

    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    @Sql(scripts = "classpath:category/add-categories.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:category/delete-categories.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @Test
    void updateCategory_whenValidRequestWithOutImg_thenReturnCreatedCategory() throws Exception {
        // Arrange
        String title = "Test Category";
        String description = "Test Description";
        Category.CategoryType type = Category.CategoryType.DESIGN;

        // Act & Assert
        MvcResult mvcResult = mockMvc.perform(multipart("/categories/3")
                        .param("title", title)
                        .param("description", description)
                        .param("type", type.toString())
                        .with(csrf())
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk()).andReturn();

        CategoryDto actual = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                CategoryDto.class);

        Assertions.assertEquals(title, actual.getTitle());
        Assertions.assertEquals(description, actual.getDescription());
        Assertions.assertEquals(type, actual.getType());
        Assertions.assertEquals("https://nordic-nest-bucket.s3.amazonaws.com/cat3.webp",
                actual.getImageUrl());

        Assertions.assertEquals(categoryRepository.findById(3L).get().getTitle(),
                actual.getTitle());
    }

    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    @Sql(scripts = "classpath:category/add-categories.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:category/delete-categories.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @Test
    void updateCategory_noExistedCategory_thenReturnCreatedCategory() throws Exception {
        // Arrange
        String title = "Test Category";
        String description = "Test Description";
        Category.CategoryType type = Category.CategoryType.DESIGN;

        // Act & Assert
        MvcResult mvcResult = mockMvc.perform(multipart("/categories/100")
                        .param("title", title)
                        .param("description", description)
                        .param("type", type.toString())
                        .with(csrf())
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isNotFound()).andReturn();
    }

    @Test
    @Sql(scripts = "classpath:category/add-categories.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:category/delete-categories.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void deleteCar_CorrectData_Success() throws Exception {
        Assertions.assertTrue(categoryRepository.findById(3L).isPresent());
        mockMvc.perform(
                delete("/categories/3")
        ).andExpect(status().isNoContent()).andReturn();
        Assertions.assertTrue(categoryRepository.findById(3L).isEmpty());
    }

    @Test
    @Sql(scripts = "classpath:category/add-categories.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:category/delete-categories.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void deleteCar_NoExistCategory_Success() throws Exception {
        mockMvc.perform(
                delete("/cars/10")
        ).andExpect(status().isNotFound()).andReturn();
    }

    @Test
    @Sql(scripts = "classpath:category/add-categories.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:category/delete-categories.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getCategoryById_CorrectDate_Success() throws Exception {
        //When
        MvcResult mvcResult = mockMvc
                .perform(
                        get("/categories/3")
                )
                .andExpect(status().isOk())
                .andReturn();

        //Then
        CategoryDto actual = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                CategoryDto.class);

        Assertions.assertEquals("Sleeping room,Test category3", actual.getTitle());
    }

    @Test
    @Sql(scripts = "classpath:category/add-categories.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:category/delete-categories.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getCategoryById_WrongDate_NotSuccess() throws Exception {
        //When
        mockMvc
                .perform(
                        get("/categories/100")
                )
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    @DisplayName("""
            Should return empty with pageable without authorization
            """)
    void getAll_EmptyDb_Success() throws Exception {
        //When
        MvcResult mvcResult = mockMvc
                .perform(
                        get("/categories")
                                .param("page-number", "0")
                                .param("page-size", "10")
                )
                .andExpect(status().isOk())
                .andReturn();
        //Then
        CustomPageImpl<CategoryDto> actual = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                objectMapper.getTypeFactory()
                        .constructParametricType(CustomPageImpl.class, CategoryDto.class)
        );

        Assertions.assertEquals(0, actual.getContent().size());
    }

    @Test
    @DisplayName("""
            Should return all categories with pagination without authorization
            """)
    @Sql(scripts = "classpath:category/add-categories.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:category/delete-categories.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getAll_CorrectDate_Success() throws Exception {
        //When
        MvcResult mvcResult = mockMvc
                .perform(
                        get("/categories")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andReturn();
        //Then
        CustomPageImpl<CategoryDto> actual = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                objectMapper.getTypeFactory()
                        .constructParametricType(CustomPageImpl.class, CategoryDto.class)
        );

        Assertions.assertEquals(5, actual.getContent().size());
        Assertions.assertEquals("Sleeping room,Test category1",
                actual.getContent().getFirst().getTitle());
    }

    @Test
    @Sql(scripts = "classpath:category/add-categories.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:category/delete-categories.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getByTitle_CorrectDate_Success() throws Exception {
        //When
        MvcResult mvcResult = mockMvc
                .perform(
                        get("/categories/title")
                                .param("title",
                                        "Sleeping room,Test category1")
                )
                .andExpect(status().isOk())
                .andReturn();

        //Then
        CategoryDto actual = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                CategoryDto.class);

        Assertions.assertEquals("Sleeping room,Test category1", actual.getTitle());
    }

    @Test
    @Sql(scripts = "classpath:category/add-categories.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:category/delete-categories.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getByTitle_WrongDate_NotSuccess() throws Exception {
        //When
        mockMvc
                .perform(
                        get("/categories/title")
                                .param("title",
                                        "Wrong title")
                )
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    @DisplayName("""
            Should return all categories with type DESIGN
            """)
    @Sql(scripts = "classpath:category/add-categories.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:category/delete-categories.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getAllByStatus_CorrectDate_Success() throws Exception {
        //When
        MvcResult mvcResult = mockMvc
                .perform(
                        get("/categories/type")
                                .param("page", "0")
                                .param("size", "10")
                                .param("type", "DESIGN")
                )
                .andExpect(status().isOk())
                .andReturn();
        //Then
        CustomPageImpl<CategoryDto> actual = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                objectMapper.getTypeFactory()
                        .constructParametricType(CustomPageImpl.class, CategoryDto.class)
        );

        Assertions.assertEquals(2, actual.getContent().size());
        Assertions.assertEquals(Category.CategoryType.DESIGN,
                actual.getContent().getFirst().getType());
        Assertions.assertEquals(Category.CategoryType.DESIGN,
                actual.getContent().getLast().getType());
    }

    @Test
    @DisplayName("""
            Should return all categories with type DESIGN
            """)
    @Sql(scripts = "classpath:category/add-categories.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:category/delete-categories.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getAllByStatus_WrongType_NotSuccess() throws Exception {
        //When
        mockMvc.perform(
                        get("/categories/type")
                                .param("page", "0")
                                .param("size", "10")
                                .param("type", "DESI")
                )
                .andExpect(status().isBadRequest())
                .andReturn();
    }
}
