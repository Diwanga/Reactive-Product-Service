package com.example.reactiveproductservice.service;

import com.example.reactiveproductservice.dto.ProductRequest;
import com.example.reactiveproductservice.model.Product;
import com.example.reactiveproductservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    
    private final ProductRepository productRepository;
    
    /**
     * Get all products - returns Flux (0 to N items)
     */
    public Flux<Product> getAllProducts() {
        log.info("Fetching all products");
        return productRepository.findAll()
                .doOnNext(product -> log.debug("Found product: {}", product.getName()))
                .doOnComplete(() -> log.info("Finished fetching all products"));
    }
    
    /**
     * Get product by ID - returns Mono (0 or 1 item)
     */
    public Mono<Product> getProductById(Long id) {
        log.info("Fetching product with id: {}", id);
        return productRepository.findById(id)
                .doOnSuccess(product -> {
                    if (product != null) {
                        log.info("Found product: {}", product.getName());
                    } else {
                        log.warn("Product not found with id: {}", id);
                    }
                });
    }
    
    /**
     * Create new product
     */
    public Mono<Product> createProduct(ProductRequest request) {
        log.info("Creating new product: {}", request.getName());
        
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        
        return productRepository.save(product)
                .doOnSuccess(saved -> log.info("Product created with id: {}", saved.getId()));
    }
    
    /**
     * Update existing product
     */
    public Mono<Product> updateProduct(Long id, ProductRequest request) {
        log.info("Updating product with id: {}", id);
        
        return productRepository.findById(id)
                .flatMap(existingProduct -> {
                    existingProduct.setName(request.getName());
                    existingProduct.setDescription(request.getDescription());
                    existingProduct.setPrice(request.getPrice());
                    existingProduct.setQuantity(request.getQuantity());
                    existingProduct.setUpdatedAt(LocalDateTime.now());
                    return productRepository.save(existingProduct);
                })
                .doOnSuccess(updated -> log.info("Product updated: {}", updated.getName()))
                .switchIfEmpty(Mono.error(new RuntimeException("Product not found with id: " + id)));
    }
    
    /**
     * Delete product
     */
    public Mono<Void> deleteProduct(Long id) {
        log.info("Deleting product with id: {}", id);
        
        return productRepository.findById(id)
                .flatMap(product -> productRepository.delete(product)
                        .doOnSuccess(v -> log.info("Product deleted: {}", product.getName())))
                .switchIfEmpty(Mono.error(new RuntimeException("Product not found with id: " + id)));
    }
    
    /**
     * Search products by name
     */
    public Flux<Product> searchProductsByName(String name) {
        log.info("Searching products with name containing: {}", name);
        return productRepository.findByNameContainingIgnoreCase(name);
    }
    
    /**
     * Get products under certain price
     */
    public Flux<Product> getProductsUnderPrice(java.math.BigDecimal price) {
        log.info("Fetching products under price: {}", price);
        return productRepository.findByPriceLessThan(price);
    }
}
