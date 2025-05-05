package org.example.nordicnestshop.service.impl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.nordicnestshop.dto.product.CreateProductDto;
import org.example.nordicnestshop.dto.product.ProductFullDto;
import org.example.nordicnestshop.dto.product.ProductSearchResponseDto;
import org.example.nordicnestshop.dto.product.UpdateProductDto;
import org.example.nordicnestshop.exception.ElementNotFoundException;
import org.example.nordicnestshop.exception.IncorrectArgumentException;
import org.example.nordicnestshop.mapper.ProductMapper;
import org.example.nordicnestshop.model.product.Attribute;
import org.example.nordicnestshop.model.product.Product;
import org.example.nordicnestshop.model.product.ProductImage;
import org.example.nordicnestshop.repository.AttributeRepository;
import org.example.nordicnestshop.repository.CategoryRepository;
import org.example.nordicnestshop.repository.ProductImageRepository;
import org.example.nordicnestshop.repository.ProductRepository;
import org.example.nordicnestshop.repository.specification.SpecificationProvider;
import org.example.nordicnestshop.repository.specification.impl.AttributesSpecificationProvider;
import org.example.nordicnestshop.service.AttributeService;
import org.example.nordicnestshop.service.ProductService;
import org.example.nordicnestshop.service.amazon.s3.S3Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final AttributeRepository attributeRepository;
    private final ProductMapper productMapper;
    private final AttributeService attributeService;
    private final S3Service s3Service;
    private final List<SpecificationProvider> specificationProviders;

    @Transactional
    @Override
    public ProductFullDto create(CreateProductDto createProductDto) {
        ifCategoryExist(createProductDto.getCategoryIds());

        if (createProductDto.getAttributes() != null) {
            createProductDto.getAttributes().forEach(a -> {
                a.setKey(a.getKey().toLowerCase());
                a.setValue(a.getValue().toLowerCase());
            });
        }

        Product product = productMapper.toEntity(createProductDto);
        product.setImages(uploadImages(createProductDto.getImages(), product));

        return productMapper.toFullDto(productRepository.save(product));
    }

    @Transactional
    @Override
    public ProductFullDto update(Long id, UpdateProductDto updateProductDto) {
        ifCategoryExist(updateProductDto.getCategoryIds());

        if (updateProductDto.getAttributes() != null) {
            updateProductDto.getAttributes().forEach(a -> {
                a.setKey(a.getKey().toLowerCase());
                a.setValue(a.getValue().toLowerCase());
            });
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ElementNotFoundException("Can't find Product with current ID: " + id));

        if (updateProductDto.getImages() != null && !updateProductDto.getImages().isEmpty()) {
            deleteImages(product.getImages());
            product.getImages().clear();
            product.getImages().addAll(uploadImages(updateProductDto.getImages(), product));
        }

        productMapper.updateEntity(updateProductDto, product);

        return productMapper.toFullDto(productRepository.save(product));
    }

    @Override
    public ProductFullDto getById(Long id) {
        return productMapper.toFullDto(productRepository.findById(id)
                .orElseThrow(() ->
                        new ElementNotFoundException("Can't find Product with current ID: " + id)));
    }

    @Override
    public ProductSearchResponseDto getAllByCategoryIdsAndAttributes(Map<String, String> attributes,
                                                                     Pageable pageable) {
        Specification<Product> specification = getSpecification(attributes);

        Page<Product> products = productRepository.findAll(specification, pageable);
        Map<Long, List<ProductImage>> productImages = productImageRepository
                .findAllByProductIn(products.getContent())
                .stream()
                .collect(Collectors.groupingBy(a -> a.getProduct().getId()));

        Map<Long, List<Attribute>> productAttributes = attributeRepository
                .findAllByProductIn(products.getContent())
                .stream()
                .collect(Collectors.groupingBy(a -> a.getProduct().getId()));

        products.forEach(p -> {
            p.setAttributes(new HashSet<>(productAttributes
                    .getOrDefault(p.getId(), Collections.emptyList())));
            p.setImages(productImages.get(p.getId()));
        });

        return formResult(products);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ElementNotFoundException("Can't find Product with current ID: " + id));

        deleteImages(product.getImages());
        productRepository.delete(product);
    }

    private void ifCategoryExist(List<Long> categoryIds) {
        if (categoryIds == null) {
            return;
        }

        long existingCount = categoryRepository.countByIdIn(categoryIds);
        if (existingCount != categoryIds.size()) {
            throw new ElementNotFoundException("Some categories do not exist.");
        }
    }

    private List<ProductImage> uploadImages(List<MultipartFile> images, Product product) {
        List<String> urls = s3Service.uploadFiles(images);

        return urls.stream()
                .map(url -> {
                    ProductImage productImage = new ProductImage();
                    productImage.setImageUrl(url);
                    productImage.setProduct(product);
                    return productImage;
                })
                .toList();
    }

    private void deleteImages(List<ProductImage> images) {
        List<String> urls = images.stream().map(ProductImage::getImageUrl).toList();
        s3Service.deleteFiles(urls);
    }

    private ProductSearchResponseDto formResult(Page<Product> products) {
        return new ProductSearchResponseDto(
                products.map(productMapper::toDto),
                attributeService.getAvailableAttributes(products),
                products.stream().map(Product::getPrice)
                        .max(Comparator.naturalOrder()).orElse(BigDecimal.ZERO),
                products.stream().map(Product::getPrice)
                        .min(Comparator.naturalOrder()).orElse(BigDecimal.ZERO));
    }

    private Map<String, List<String>> convertAttributesRequest(Map<String, String> attributes) {
        return attributes.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> Arrays.asList(entry.getValue().toLowerCase().split(","))
                ));
    }

    private Specification<Product> getSpecification(Map<String, String> attributes) {
        attributes.remove("page_size");
        attributes.remove("page_number");
        Specification<Product> specification = Specification.where(null);
        try {
            for (SpecificationProvider specificationProvider : specificationProviders) {
                if (specificationProvider.getName().equals("attributes")) {
                    continue;
                }
                if (attributes.get(specificationProvider.getName()) != null) {
                    specificationProvider.parseValues(attributes.remove(specificationProvider
                            .getName()));
                    specification = specification.and(specificationProvider.getSpecification());
                }
            }

            AttributesSpecificationProvider attributesSpecificationProvider =
                    new AttributesSpecificationProvider();
            attributesSpecificationProvider.setAttributes(convertAttributesRequest(attributes));
            specification = specification.and(attributesSpecificationProvider.getSpecification());
        } catch (NumberFormatException e) {
            throw new IncorrectArgumentException("Incorrect input format. " + e.getMessage(), e);
        }
        return specification;
    }
}
