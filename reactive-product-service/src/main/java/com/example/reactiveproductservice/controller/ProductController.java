package com.example.reactiveproductservice.controller;

import com.example.reactiveproductservice.dto.ProductRequest;
import com.example.reactiveproductservice.model.Product;
import com.example.reactiveproductservice.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductService productService;
    
    /**
     * GET /api/products - Get all products
     * Returns: Flux<Product> (stream of products)
     */
    @GetMapping
    public Flux<Product> getAllProducts() {
        return productService.getAllProducts();
    }
    
    /**
     * GET /api/products/stream - Get all products as Server-Sent Events (SSE)
     * This demonstrates streaming capability of reactive!
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Product> streamAllProducts() {
        return productService.getAllProducts()
                .delayElements(java.time.Duration.ofSeconds(1)); // Delay to show streaming
    }
    
    /**
     * GET /api/products/{id} - Get product by ID
     * Returns: Mono<Product> (0 or 1 product)
     */
    @GetMapping("/{id}")
    public Mono<Product> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Product not found")));
    }
    
    /**
     * POST /api/products - Create new product
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Product> createProduct(@Valid @RequestBody ProductRequest request) {
        return productService.createProduct(request);
    }
    
    /**
     * PUT /api/products/{id} - Update product
     */
    @PutMapping("/{id}")
    public Mono<Product> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return productService.updateProduct(id, request);
    }
    
    /**
     * DELETE /api/products/{id} - Delete product
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteProduct(@PathVariable Long id) {
        return productService.deleteProduct(id);
    }
    
    /**
     * GET /api/products/search?name=laptop - Search products by name
     */
    @GetMapping("/search")
    public Flux<Product> searchProducts(@RequestParam String name) {
        return productService.searchProductsByName(name);
    }
    
    /**
     * GET /api/products/under-price?price=100 - Get products under certain price
     */
    @GetMapping("/under-price")
    public Flux<Product> getProductsUnderPrice(@RequestParam BigDecimal price) {
        return productService.getProductsUnderPrice(price);
    }
    
    /**
     * Exception Handler
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<String> handleNotFound(RuntimeException ex) {
        return Mono.just(ex.getMessage());
    }
}
