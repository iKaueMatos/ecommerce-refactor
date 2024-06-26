package com.commerce.backend.service;

import com.commerce.backend.core.error.exception.InvalidArgumentException;
import com.commerce.backend.core.error.exception.ResourceNotFoundException;
import com.commerce.backend.modules.product.application.converter.ProductDetailsResponseConverter;
import com.commerce.backend.modules.product.application.converter.ProductResponseConverter;
import com.commerce.backend.modules.product.application.converter.ProductVariantResponseConverter;
import com.commerce.backend.modules.product.application.useCases.dto.ProductDetailsResponse;
import com.commerce.backend.modules.product.application.useCases.dto.ProductResponse;
import com.commerce.backend.modules.product.application.useCases.dto.ProductVariantResponse;
import com.commerce.backend.modules.product.domain.service.ProductServiceImpl;
import com.commerce.backend.modules.product.infra.cache.ProductCacheService;
import com.commerce.backend.modules.product.infra.cache.ProductVariantCacheService;
import com.commerce.backend.modules.product.infra.entity.Product;
import com.commerce.backend.modules.product.infra.entity.ProductCategory;
import com.commerce.backend.modules.product.infra.entity.ProductVariant;
import com.commerce.backend.modules.product.infra.repository.ProductRepository;
import com.commerce.backend.modules.product.infra.repository.ProductVariantRepository;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
class ProductServiceImplTest {

    @InjectMocks
    private ProductServiceImpl productService;

    @Mock
    private ProductCacheService productCacheService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private ProductVariantCacheService productVariantCacheService;

    @Mock
    private ProductResponseConverter productResponseConverter;

    @Mock
    private ProductVariantResponseConverter productVariantResponseConverter;

    @Mock
    private ProductDetailsResponseConverter productDetailsResponseConverter;

    private Faker faker;


    @BeforeEach
    public void setUp() {
        faker = new Faker();
    }

    @Test
    void it_should_find_product_by_url() {


        String url = faker.internet().domainSuffix();

        Product product = new Product();

        ProductDetailsResponse productDetailsResponseExpected = new ProductDetailsResponse();

        given(productCacheService.findByUrl(url)).willReturn(product);
        given(productDetailsResponseConverter.apply(product)).willReturn(productDetailsResponseExpected);

        // when
        ProductDetailsResponse productDetailsResponseResult = productService.findByUrl(url);

        // then
        verify(productDetailsResponseConverter).apply(product);
        then(productDetailsResponseResult).isEqualTo(productDetailsResponseExpected);

    }

    @Test
    void it_should_throw_exception_when_no_product_found_by_url() {
        String url = faker.internet().domainSuffix();

        given(productCacheService.findByUrl(url)).willReturn(null);

        // when, then
        assertThatThrownBy(() -> productService.findByUrl(url))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(String.format("Product not found with the url %s", url));
    }

    @Test
    void it_should_find_product__variant_by_id() {
        Long id = faker.number().randomNumber();

        ProductVariant productVariant = new ProductVariant();
        given(productVariantCacheService.findById(id)).willReturn(productVariant);
        ProductVariant productVariantResult = productService.findProductVariantById(id);
        then(productVariantResult).isEqualTo(productVariant);
    }

    @Test
    void it_should_throw_exception_when_no_product__variant_found_by_id() {
        Long id = faker.number().randomNumber();

        given(productVariantCacheService.findById(id)).willReturn(null);
        assertThatThrownBy(() -> productService.findProductVariantById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(String.format("Could not find any product variant with the id %d", id));
    }

    @Test
    void it_should_get_all_product_variants() {
        Integer page = faker.number().randomDigitNotZero();
        Integer size = faker.number().randomDigitNotZero();
        String sort = faker.bool().bool() ? "lowest" : "highest";
        String category = faker.lorem().word();
        Float minPrice = (float) faker.number().randomNumber();
        Float maxPrice = minPrice + (float) faker.number().randomNumber();
        String color = faker.color().name();

        ProductVariant productVariant = new ProductVariant();
        List<ProductVariant> productVariantList = new ArrayList<>();
        productVariantList.add(productVariant);

        Page<ProductVariant> productVariantPage = new PageImpl<>(productVariantList);

        ProductVariantResponse productVariantResponseExpected = new ProductVariantResponse();

        given(productVariantRepository.findAll(any(Specification.class), any(PageRequest.class))).willReturn(productVariantPage);
        given(productVariantResponseConverter.apply(any(ProductVariant.class))).willReturn(productVariantResponseExpected);

        // when
        List<ProductVariantResponse> productVariantResponseList = productService.getAll(page, size, sort, category, minPrice, maxPrice, color);

        // then
        then(productVariantResponseList.size()).isEqualTo(productVariantList.size());
        productVariantResponseList.forEach(productVariantResponse -> then(productVariantResponse).isEqualTo(productVariantResponseExpected));
    }

    @Test
    void it_should_get_all_product_variants_with_no_sort() {


        Integer page = faker.number().randomDigitNotZero();
        Integer size = faker.number().randomDigitNotZero();
        String sort = null;
        String category = faker.lorem().word();
        Float minPrice = (float) faker.number().randomNumber();
        Float maxPrice = minPrice + (float) faker.number().randomNumber();
        String color = faker.color().name();

        ProductVariant productVariant = new ProductVariant();
        List<ProductVariant> productVariantList = new ArrayList<>();
        productVariantList.add(productVariant);

        Page<ProductVariant> productVariantPage = new PageImpl<>(productVariantList);
        ProductVariantResponse productVariantResponseExpected = new ProductVariantResponse();
        given(productVariantRepository.findAll(any(Specification.class), any(PageRequest.class))).willReturn(productVariantPage);
        given(productVariantResponseConverter.apply(any(ProductVariant.class))).willReturn(productVariantResponseExpected);
        List<ProductVariantResponse> productVariantResponseList = productService.getAll(page, size, sort, category, minPrice, maxPrice, color);

        then(productVariantResponseList.size()).isEqualTo(productVariantList.size());
        productVariantResponseList.forEach(productVariantResponse -> then(productVariantResponse).isEqualTo(productVariantResponseExpected));
    }

    @Test
    void it_should_throw_exception_when_invalid_sort() {
        Integer page = faker.number().randomDigitNotZero();
        Integer size = faker.number().randomDigitNotZero();
        String sort = faker.random().hex();
        String category = faker.lorem().word();
        Float minPrice = (float) faker.number().randomNumber();
        Float maxPrice = minPrice + (float) faker.number().randomNumber();
        String color = faker.color().name();

        assertThatThrownBy(() -> productService.getAll(page, size, sort, category, minPrice, maxPrice, color))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessage("Invalid sort parameter");
    }

    @Test
    void it_should_get_all_product_variant_count() {
        String category = faker.lorem().word();
        Float minPrice = (float) faker.number().randomNumber();
        Float maxPrice = minPrice + (float) faker.number().randomNumber();
        String color = faker.color().name();

        Long count = faker.number().randomNumber();

        given(productVariantRepository.count(any(Specification.class))).willReturn(count);
        Long countResult = productService.getAllCount(category, minPrice, maxPrice, color);

        then(countResult).isEqualTo(count);
    }


    @Test
    void it_should_get_all_related_products() {


        String url = faker.internet().domainSuffix();

        Product product = new Product();
        product.setId(faker.number().randomNumber());
        ProductCategory productCategory = new ProductCategory();
        productCategory.setName(faker.lorem().word());
        product.setProductCategory(productCategory);


        List<Product> productList = Stream.generate(Product::new)
                .limit(faker.number().randomDigitNotZero())
                .collect(Collectors.toList());

        given(productCacheService.findByUrl(url)).willReturn(product);
        given(productCacheService.getRelatedProducts(product.getProductCategory(), product.getId())).willReturn(productList);

        List<ProductResponse> productResponseList = productService.getRelatedProducts(url);
        then(productResponseList.size()).isEqualTo(productList.size());

    }

    @Test
    void it_should_throw_exception_when_all_related_products_not_found() {


        String url = faker.internet().domainSuffix();

        given(productCacheService.findByUrl(url)).willReturn(null);

        // when, then
        assertThatThrownBy(() -> productService.getRelatedProducts(url))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Related products not found");

    }

    @Test
    void it_should_get_all_newly_added_products() {


        List<Product> productList = Stream.generate(Product::new)
                .limit(faker.number().randomDigitNotZero())
                .collect(Collectors.toList());

        given(productCacheService.findTop8ByOrderByDateCreatedDesc()).willReturn(productList);

        // when
        List<ProductResponse> productResponseList = productService.getNewlyAddedProducts();

        // then
        then(productResponseList.size()).isEqualTo(productList.size());

    }

    @Test
    void it_should_throw_exception_when_all_newly_added_products_not_found() {


        given(productCacheService.findTop8ByOrderByDateCreatedDesc()).willReturn(Collections.emptyList());

        // when, then
        assertThatThrownBy(() -> productService.getNewlyAddedProducts())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Newly added products not found");

    }


    @Test
    void it_should_get_all_most_selling_products() {
        List<ProductVariant> productVariantList = Stream.generate(ProductVariant::new)
                .limit(faker.number().randomDigitNotZero())
                .collect(Collectors.toList());

        given(productVariantCacheService.findTop8ByOrderBySellCountDesc()).willReturn(productVariantList);
        List<ProductVariantResponse> productVariantResponseList = productService.getMostSelling();

        then(productVariantResponseList.size()).isEqualTo(productVariantList.size());
    }

    @Test
    void it_should_throw_exception_when_all_most_selling_products_not_found() {
        given(productVariantCacheService.findTop8ByOrderBySellCountDesc()).willReturn(Collections.emptyList());

        assertThatThrownBy(() -> productService.getMostSelling())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Most selling products not found");
    }

    @Test
    void it_should_get_all_interested_products() {
        List<Product> productList = Stream.generate(Product::new)
                .limit(faker.number().randomDigitNotZero())
                .collect(Collectors.toList());

        given(productCacheService.findTop8ByOrderByDateCreatedDesc()).willReturn(productList);
        List<ProductResponse> productResponseList = productService.getInterested();

        then(productResponseList.size()).isEqualTo(productList.size());
    }

    @Test
    void it_should_throw_exception_when_all_interested_products_not_found() {
        given(productCacheService.findTop8ByOrderByDateCreatedDesc()).willReturn(Collections.emptyList());

        assertThatThrownBy(() -> productService.getInterested())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Interested products not found");
    }

    @Test
    void it_should_get_searched_products() {
        String keyword = faker.lorem().word();
        Integer page = faker.number().randomDigitNotZero();
        Integer size = faker.number().randomDigitNotZero();

        List<Product> productList = Stream.generate(Product::new)
                .limit(faker.number().randomDigitNotZero())
                .collect(Collectors.toList());

        given(productRepository.findAllByNameContainingIgnoreCase(keyword, PageRequest.of(page, size))).willReturn(productList);
        given(productResponseConverter.apply(any(Product.class))).willReturn(new ProductResponse());
        List<ProductResponse> productResponseList = productService.searchProductDisplay(keyword, page, size);

        then(productResponseList.size()).isEqualTo(productList.size());
    }

    @Test
    void it_should_throw_exception_when_searched_products_with_null_page_and_null_size() {
        String keyword = faker.lorem().word();
        
        assertThatThrownBy(() -> productService.searchProductDisplay(keyword, null, faker.number().randomDigitNotZero()))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessage("Page and size are required");

        assertThatThrownBy(() -> productService.searchProductDisplay(keyword, faker.number().randomDigitNotZero(), null))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessage("Page and size are required");

    }


}