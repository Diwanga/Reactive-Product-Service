package com.example.reactiveproductservice.service;

import com.example.reactiveproductservice.dto.ProductRequest;
import com.example.reactiveproductservice.model.Product;
import com.example.reactiveproductservice.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void getAllProducts_shouldReturnAllProducts() {
        // Given
        Product product1 = createProduct(1L, "Laptop", BigDecimal.valueOf(1000));
        Product product2 = createProduct(2L, "Mouse", BigDecimal.valueOf(50));
        
        when(productRepository.findAll())
                .thenReturn(Flux.just(product1, product2));

        // When & Then - Using StepVerifier for reactive testing!
        StepVerifier.create(productService.getAllProducts())
                .expectNext(product1)
                .expectNext(product2)
                .verifyComplete();
    }

    @Test
    void getProductById_shouldReturnProduct() {
        // Given
        Product product = createProduct(1L, "Laptop", BigDecimal.valueOf(1000));
        when(productRepository.findById(1L))
                .thenReturn(Mono.just(product));

        // When & Then
        StepVerifier.create(productService.getProductById(1L))
                .expectNext(product)
                .verifyComplete();
    }

    @Test
    void getProductById_shouldReturnEmptyWhenNotFound() {
        // Given
        when(productRepository.findById(999L))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(productService.getProductById(999L))
                .verifyComplete();  // Completes with no items
    }

    @Test
    void createProduct_shouldSaveProduct() {
        // Given
        ProductRequest request = new ProductRequest(
                "New Laptop", 
                "Gaming laptop", 
                BigDecimal.valueOf(1500), 
                10
        );
        
        Product savedProduct = createProduct(1L, "New Laptop", BigDecimal.valueOf(1500));
        
        when(productRepository.save(any(Product.class)))
                .thenReturn(Mono.just(savedProduct));

        // When & Then
        StepVerifier.create(productService.createProduct(request))
                .expectNextMatches(product -> 
                        product.getName().equals("New Laptop") && 
                        product.getPrice().equals(BigDecimal.valueOf(1500))
                )
                .verifyComplete();
    }

    @Test
    void updateProduct_shouldUpdateExistingProduct() {
        // Given
        Product existingProduct = createProduct(1L, "Old Name", BigDecimal.valueOf(1000));
        ProductRequest request = new ProductRequest(
                "Updated Name", 
                "Updated desc", 
                BigDecimal.valueOf(1200), 
                20
        );
        
        when(productRepository.findById(1L))
                .thenReturn(Mono.just(existingProduct));
        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // When & Then
        StepVerifier.create(productService.updateProduct(1L, request))
                .expectNextMatches(product -> 
                        product.getName().equals("Updated Name") &&
                        product.getPrice().equals(BigDecimal.valueOf(1200))
                )
                .verifyComplete();
    }

    @Test
    void deleteProduct_shouldDeleteProduct() {
        // Given
        Product product = createProduct(1L, "Laptop", BigDecimal.valueOf(1000));
        
        when(productRepository.findById(1L))
                .thenReturn(Mono.just(product));
        when(productRepository.delete(product))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(productService.deleteProduct(1L))
                .verifyComplete();
    }

    private Product createProduct(Long id, String name, BigDecimal price) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setPrice(price);
        product.setQuantity(10);
        return product;
    }
}
