package org.example.nordicnestshop.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.example.nordicnestshop.dto.product.ProductDto;
import org.example.nordicnestshop.dto.product.ProductFullDto;
import org.example.nordicnestshop.dto.product.ProductSearchResponseDto;
import org.example.nordicnestshop.dto.product.attribute.AttributeDto;
import org.example.nordicnestshop.model.product.Product;
import org.example.nordicnestshop.repository.ProductRepository;
import org.example.nordicnestshop.test.utils.CustomPageImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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

@SpringBootTest(webEnvironment =
        SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductControllerTest {

    private static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @TestConfiguration
    static class TestS3Config {
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

    @BeforeAll
    static void beforeAll(
            @Autowired WebApplicationContext applicationContext) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    @Sql(scripts = "classpath:category/add-categories-without-id.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:product/delete-product-and-categories.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void createProduct_whenValidRequest_thenReturnCreatedProduct() throws Exception {
        List<Long> categoryIds = List.of(1L, 2L);

        // Create test images
        MockMultipartFile image1 = new MockMultipartFile(
                "images",
                "test-image-1.jpg",
                "image/jpeg",
                "test image content 1".getBytes()
        );

        MockMultipartFile image2 = new MockMultipartFile(
                "images",
                "test-image-2.jpg",
                "image/jpeg",
                "test image content 2".getBytes()
        );

        // Create the request payload
        String title = "Test Product";
        String description = "This is a test product description";
        String article = "TEST-123";
        BigDecimal price = new BigDecimal("99.99");

        // Act & Assert
        MvcResult mvcResult = mockMvc.perform(multipart("/products")
                        .file(image1)
                        .file(image2)
                        .param("title", title)
                        .param("description", description)
                        .param("article", article)
                        .param("price", price.toString())
                        .param("categoryIds", categoryIds.stream()
                                .map(String::valueOf).toArray(String[]::new))
                        .param("attributes[0].key", "color")
                        .param("attributes[0].value", "red")
                        .param("attributes[1].key", "size")
                        .param("attributes[1].value", "medium")
                        .param("attributes[2].key", "material")
                        .param("attributes[2].value", "cotton")
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andReturn();

        // Deserialize response
        ProductFullDto actualProduct = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                ProductFullDto.class
        );

        // Verify response
        Assertions.assertNotNull(actualProduct);
        Assertions.assertNotNull(actualProduct.getId());
        Assertions.assertEquals(title, actualProduct.getTitle());
        Assertions.assertEquals(description, actualProduct.getDescription());
        Assertions.assertEquals(article, actualProduct.getArticle());
        Assertions.assertEquals(price, actualProduct.getPrice());

        // Verify categories
        Assertions.assertEquals(2, actualProduct.getCategoryIds().size());

        // Verify attributes
        Assertions.assertEquals(3, actualProduct.getAttributes().size());

        // Verify that attributes contain expected key-value pairs
        Map<String, String> attributeMap = actualProduct.getAttributes().stream()
                .collect(Collectors.toMap(AttributeDto::getKey, AttributeDto::getValue));
        Assertions.assertEquals("red", attributeMap.get("color"));
        Assertions.assertEquals("medium", attributeMap.get("size"));
        Assertions.assertEquals("cotton", attributeMap.get("material"));

        // Verify images
        Assertions.assertEquals(2, actualProduct.getImages().size());

        // Verify product is stored in DB
        Optional<Product> storedProduct = productRepository.findById(actualProduct.getId());
        Assertions.assertTrue(storedProduct.isPresent());
        Assertions.assertEquals(title, storedProduct.get().getTitle());
    }

    @Test
    @WithMockUser(username = "user", authorities = {"USER"})
    void createProduct_whenUnauthorized_thenReturnForbidden() throws Exception {
        // Act & Assert - User without ADMIN role should get 403 Forbidden
        mockMvc.perform(multipart("/products")
                        .param("title", "Test Product")
                        .param("description", "Test Description")
                        .param("article", "TEST-123")
                        .param("price", "99.99")
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void createProduct_whenInvalidRequest_thenReturnBadRequest() throws Exception {
        // Act & Assert - Missing required fields should get 400 Bad Request
        mockMvc.perform(multipart("/products")
                        // Missing title
                        .param("description", "Test Description")
                        .param("article", "TEST-123")
                        .param("price", "99.99")
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());

        // Price below minimum
        mockMvc.perform(multipart("/products")
                        .param("title", "Test Product")
                        .param("description", "Test Description")
                        .param("article", "TEST-123")
                        .param("price", "0.00")
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    @Sql(scripts = "classpath:product/add-product-and-category.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:product/delete-product-and-categories.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateProduct_whenValidRequestWithOutImg_thenReturnCreatedProduct() throws Exception {
        List<Long> categoryIds = List.of(1L);

        // Create the request payload
        String title = "New Test Product";
        String description = "New This is a test product description";
        String article = "New TEST-123";
        BigDecimal price = new BigDecimal("109.99");

        // Act & Assert
        MvcResult mvcResult = mockMvc.perform(multipart("/products/4")
                        .param("title", title)
                        .param("description", description)
                        .param("article", article)
                        .param("price", price.toString())
                        .param("categoryIds", categoryIds.stream()
                                .map(String::valueOf).toArray(String[]::new))
                        .param("attributes[0].key", "color")
                        .param("attributes[0].value", "red")
                        .param("attributes[1].key", "size")
                        .param("attributes[1].value", "medium")
                        .with(csrf())
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn();

        // Deserialize response
        ProductFullDto actualProduct = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                ProductFullDto.class
        );

        // Verify response
        Assertions.assertNotNull(actualProduct);
        Assertions.assertEquals(4, actualProduct.getId());
        Assertions.assertEquals(title, actualProduct.getTitle());
        Assertions.assertEquals(description, actualProduct.getDescription());
        Assertions.assertEquals(article, actualProduct.getArticle());
        Assertions.assertEquals(price, actualProduct.getPrice());

        // Verify categories
        Assertions.assertEquals(1, actualProduct.getCategoryIds().size());

        // Verify attributes
        Assertions.assertEquals(2, actualProduct.getAttributes().size());

        // Verify that attributes contain expected key-value pairs
        Map<String, String> attributeMap = actualProduct.getAttributes().stream()
                .collect(Collectors.toMap(AttributeDto::getKey, AttributeDto::getValue));
        Assertions.assertEquals("red", attributeMap.get("color"));
        Assertions.assertEquals("medium", attributeMap.get("size"));

        // Verify images
        Assertions.assertEquals(1, actualProduct.getImages().size());

        // Verify product is stored in DB
        Optional<Product> storedProduct = productRepository.findById(actualProduct.getId());
        Assertions.assertTrue(storedProduct.isPresent());
        Assertions.assertEquals(title, storedProduct.get().getTitle());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    @Sql(scripts = "classpath:product/add-product-and-category.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:product/delete-product-and-categories.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateProduct_whenValidRequest_thenReturnCreatedProduct() throws Exception {
        List<Long> categoryIds = List.of(1L);

        // Create test images
        MockMultipartFile image1 = new MockMultipartFile(
                "images",
                "test-image-1.jpg",
                "image/jpeg",
                "test image content 1".getBytes()
        );

        // Create the request payload
        String title = "New Test Product";
        String description = "New This is a test product description";
        String article = "New TEST-123";
        BigDecimal price = new BigDecimal("109.99");

        // Act & Assert
        MvcResult mvcResult = mockMvc.perform(multipart("/products/4")
                        .file(image1)
                        .param("title", title)
                        .param("description", description)
                        .param("article", article)
                        .param("price", price.toString())
                        .param("categoryIds", categoryIds.stream()
                                .map(String::valueOf).toArray(String[]::new))
                        .param("attributes[0].key", "color")
                        .param("attributes[0].value", "red")
                        .param("attributes[1].key", "size")
                        .param("attributes[1].value", "medium")
                        .with(csrf())
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn();

        // Deserialize response
        ProductFullDto actualProduct = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                ProductFullDto.class
        );

        // Verify response
        Assertions.assertNotNull(actualProduct);
        Assertions.assertEquals(4, actualProduct.getId());
        Assertions.assertEquals(title, actualProduct.getTitle());
        Assertions.assertEquals(description, actualProduct.getDescription());
        Assertions.assertEquals(article, actualProduct.getArticle());
        Assertions.assertEquals(price, actualProduct.getPrice());

        // Verify categories
        Assertions.assertEquals(1, actualProduct.getCategoryIds().size());

        // Verify attributes
        Assertions.assertEquals(2, actualProduct.getAttributes().size());

        // Verify that attributes contain expected key-value pairs
        Map<String, String> attributeMap = actualProduct.getAttributes().stream()
                .collect(Collectors.toMap(AttributeDto::getKey, AttributeDto::getValue));
        Assertions.assertEquals("red", attributeMap.get("color"));
        Assertions.assertEquals("medium", attributeMap.get("size"));

        // Verify images
        Assertions.assertEquals(1, actualProduct.getImages().size());

        // Verify product is stored in DB
        Optional<Product> storedProduct = productRepository.findById(actualProduct.getId());
        Assertions.assertTrue(storedProduct.isPresent());
        Assertions.assertEquals(title, storedProduct.get().getTitle());
    }

    @Test
    @Sql(scripts = "classpath:product/add-product-and-category.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:product/delete-product-and-categories.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void deleteProduct_whenValidRequest_NoContent() throws Exception {
        Assertions.assertTrue(productRepository.findById(2L).isPresent());
        mockMvc.perform(
                delete("/products/2")
        ).andExpect(status().isNoContent()).andReturn();
        Assertions.assertTrue(productRepository.findById(2L).isEmpty());
    }

    @Test
    @Sql(scripts = "classpath:product/add-product-and-category.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:product/delete-product-and-categories.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void deleteProduct_noExist_thenReturnCreatedProduct() throws Exception {
        mockMvc.perform(
                delete("/products/20")
        ).andExpect(status().isNotFound());
    }

    @Test
    @Sql(scripts = "classpath:product/add-product-and-category.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:product/delete-product-and-categories.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getProductById_Success() throws Exception {
        //When
        MvcResult mvcResult = mockMvc
                .perform(
                        get("/products/3")
                )
                .andExpect(status().isOk())
                .andReturn();

        //Then
        ProductFullDto actual = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                ProductFullDto.class);

        Assertions.assertEquals("Very Chear3", actual.getTitle());
        Assertions.assertEquals(1, actual.getAttributes().size());
    }

    @Test
    @Sql(scripts = "classpath:product/add-product-and-category.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:product/delete-product-and-categories.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getProductById_NotFound() throws Exception {
        //When
        MvcResult mvcResult = mockMvc
                .perform(
                        get("/products/30")
                )
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    @Sql(scripts = "classpath:product/add-product-and-category.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:product/delete-product-and-categories.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getAllByCategoryIdsAndAttributes_categoriesFilter_Success() throws Exception {
        MvcResult mvcResult = mockMvc
                .perform(
                        get("/products/search")
                                .param("page_number", "0")
                                .param("page_size", "10")
                                .param("categoryIds", "1,2")
                )
                .andExpect(status().isOk())
                .andReturn();

        JsonNode rootNode = objectMapper.readTree(mvcResult.getResponse().getContentAsString());

        CustomPageImpl<ProductDto> productsPage = objectMapper.treeToValue(
                rootNode.get("products"), new TypeReference<>() {
                }
        );

        Map<String, List<String>> availableAttributes = objectMapper.treeToValue(
                rootNode.get("availableAttributes"), new TypeReference<>() {
                }
        );

        BigDecimal minPrice = new BigDecimal(rootNode.get("minPrice").asText());
        BigDecimal maxPrice = new BigDecimal(rootNode.get("maxPrice").asText());

        ProductSearchResponseDto responseDto =
                new ProductSearchResponseDto(productsPage, availableAttributes, minPrice, maxPrice);

        Assertions.assertEquals(1, responseDto.getProducts().getContent().size());
    }

    @Test
    @Sql(scripts = "classpath:product/add-product-and-category.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:product/delete-product-and-categories.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getAllByCategoryIdsWithSameTypeAndAttributes_categoriesFilter_Success() throws Exception {
        MvcResult mvcResult = mockMvc
                .perform(
                        get("/products/search")
                                .param("page_number", "0")
                                .param("page_size", "10")
                                .param("categoryIds", "4,2")
                )
                .andExpect(status().isOk())
                .andReturn();

        JsonNode rootNode = objectMapper.readTree(mvcResult.getResponse().getContentAsString());

        CustomPageImpl<ProductDto> productsPage = objectMapper.treeToValue(
                rootNode.get("products"), new TypeReference<>() {
                }
        );

        Map<String, List<String>> availableAttributes = objectMapper.treeToValue(
                rootNode.get("availableAttributes"), new TypeReference<>() {
                }
        );

        BigDecimal minPrice = new BigDecimal(rootNode.get("minPrice").asText());
        BigDecimal maxPrice = new BigDecimal(rootNode.get("maxPrice").asText());

        ProductSearchResponseDto responseDto =
                new ProductSearchResponseDto(productsPage, availableAttributes, minPrice, maxPrice);

        Assertions.assertEquals(2, responseDto.getProducts().getContent().size());
    }

    @Test
    @Sql(scripts = "classpath:product/add-product-and-category.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:product/delete-product-and-categories.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getAllByCategoryIdsAndAttributes_defaultFilter_Success() throws Exception {
        MvcResult mvcResult = mockMvc
                .perform(
                        get("/products/search")
                                .param("page_number", "0")
                                .param("page_size", "10")
                )
                .andExpect(status().isOk())
                .andReturn();

        JsonNode rootNode = objectMapper.readTree(mvcResult.getResponse().getContentAsString());

        CustomPageImpl<ProductDto> productsPage = objectMapper.treeToValue(
                rootNode.get("products"), new TypeReference<>() {
                }
        );

        Map<String, List<String>> availableAttributes = objectMapper.treeToValue(
                rootNode.get("availableAttributes"), new TypeReference<>() {
                }
        );

        BigDecimal minPrice = new BigDecimal(rootNode.get("minPrice").asText());
        BigDecimal maxPrice = new BigDecimal(rootNode.get("maxPrice").asText());

        ProductSearchResponseDto responseDto
                = new ProductSearchResponseDto(
                        productsPage, availableAttributes, minPrice, maxPrice);

        Assertions.assertEquals(6, responseDto.getProducts().getContent().size());
    }

    @Test
    @Sql(scripts = "classpath:product/add-product-and-category.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:product/delete-product-and-categories.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getAllByCategoryIdsAndAttributes_allFilter1_Success() throws Exception {
        MvcResult mvcResult = mockMvc
                .perform(
                        get("/products/search")
                                .param("page_number", "0")
                                .param("page_size", "10")
                                .param("minPrice", "100")
                                .param("maxPrice", "200")
                                .param("categoryIds", "1")
                                .param("size", "l")
                )
                .andExpect(status().isOk())
                .andReturn();

        JsonNode rootNode = objectMapper.readTree(mvcResult.getResponse().getContentAsString());

        CustomPageImpl<ProductDto> productsPage = objectMapper.treeToValue(
                rootNode.get("products"), new TypeReference<>() {
                }
        );

        Map<String, List<String>> availableAttributes = objectMapper.treeToValue(
                rootNode.get("availableAttributes"), new TypeReference<>() {
                }
        );

        BigDecimal minPrice = new BigDecimal(rootNode.get("minPrice").asText());
        BigDecimal maxPrice = new BigDecimal(rootNode.get("maxPrice").asText());

        ProductSearchResponseDto responseDto
                = new ProductSearchResponseDto(
                        productsPage, availableAttributes, minPrice, maxPrice);

        Assertions.assertEquals(1, responseDto.getProducts().getContent().size());
    }

    @Test
    @Sql(scripts = "classpath:product/add-product-and-category.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:product/delete-product-and-categories.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getAllByCategoryIdsAndAttributes_allFilter2_Success() throws Exception {
        MvcResult mvcResult = mockMvc
                .perform(
                        get("/products/search")
                                .param("page_number", "0")
                                .param("page_size", "10")
                                .param("minPrice", "100")
                                .param("maxPrice", "200")
                                .param("categoryIds", "1")
                                .param("size", "l,m")
                )
                .andExpect(status().isOk())
                .andReturn();

        JsonNode rootNode = objectMapper.readTree(mvcResult.getResponse().getContentAsString());

        CustomPageImpl<ProductDto> productsPage = objectMapper.treeToValue(
                rootNode.get("products"), new TypeReference<>() {
                }
        );

        Map<String, List<String>> availableAttributes = objectMapper.treeToValue(
                rootNode.get("availableAttributes"), new TypeReference<>() {
                }
        );

        BigDecimal minPrice = new BigDecimal(rootNode.get("minPrice").asText());
        BigDecimal maxPrice = new BigDecimal(rootNode.get("maxPrice").asText());

        ProductSearchResponseDto responseDto
                = new ProductSearchResponseDto(
                        productsPage, availableAttributes, minPrice, maxPrice);

        Assertions.assertEquals(2, responseDto.getProducts().getContent().size());
    }

    @Test
    @Sql(scripts = "classpath:product/add-product-and-category.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:product/delete-product-and-categories.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getAllByCategoryIdsAndAttributes_priceFilter_Success() throws Exception {
        MvcResult mvcResult = mockMvc
                .perform(
                        get("/products/search")
                                .param("page_number", "0")
                                .param("page_size", "10")
                                .param("minPrice", "100")
                                .param("maxPrice", "200")
                )
                .andExpect(status().isOk())
                .andReturn();

        JsonNode rootNode = objectMapper.readTree(mvcResult.getResponse().getContentAsString());

        CustomPageImpl<ProductDto> productsPage = objectMapper.treeToValue(
                rootNode.get("products"), new TypeReference<>() {
                }
        );

        Map<String, List<String>> availableAttributes = objectMapper.treeToValue(
                rootNode.get("availableAttributes"), new TypeReference<>() {
                }
        );

        BigDecimal minPrice = new BigDecimal(rootNode.get("minPrice").asText());
        BigDecimal maxPrice = new BigDecimal(rootNode.get("maxPrice").asText());

        ProductSearchResponseDto responseDto =
                new ProductSearchResponseDto(productsPage, availableAttributes, minPrice, maxPrice);

        Assertions.assertEquals(5, responseDto.getProducts().getContent().size());
    }

    @Test
    @Sql(scripts = "classpath:product/add-product-and-category.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:product/delete-product-and-categories.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getAllByCategoryIdsAndAttributes_searchTextFilter_Success() throws Exception {
        MvcResult mvcResult = mockMvc
                .perform(
                        get("/products/search")
                                .param("page_number", "0")
                                .param("page_size", "10")
                                .param("minPrice", "100")
                                .param("maxPrice", "200")
                                .param("searchText", "Cat")
                )
                .andExpect(status().isOk())
                .andReturn();

        JsonNode rootNode = objectMapper.readTree(mvcResult.getResponse().getContentAsString());

        CustomPageImpl<ProductDto> productsPage = objectMapper.treeToValue(
                rootNode.get("products"), new TypeReference<>() {
                }
        );

        Map<String, List<String>> availableAttributes = objectMapper.treeToValue(
                rootNode.get("availableAttributes"), new TypeReference<>() {
                }
        );

        BigDecimal minPrice = new BigDecimal(rootNode.get("minPrice").asText());
        BigDecimal maxPrice = new BigDecimal(rootNode.get("maxPrice").asText());

        ProductSearchResponseDto responseDto
                = new ProductSearchResponseDto(
                        productsPage, availableAttributes, minPrice, maxPrice);

        Assertions.assertEquals(3, responseDto.getProducts().getContent().size());
    }

    @Test
    @Sql(scripts = "classpath:product/add-product-and-category.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:product/delete-product-and-categories.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getAllByCategoryIdsAndAttributes_BadRequest_NotSuccess() throws Exception {
        MvcResult mvcResult = mockMvc
                .perform(
                        get("/products/search")
                                .param("page_number", "0")
                                .param("page_size", "10")
                                .param("minPrice", "3grg00")
                                .param("maxPrice", "200")
                )
                .andExpect(status().isBadRequest())
                .andReturn();
    }
}
