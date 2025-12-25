package com.example.reactiveproductservice.repository;

import com.example.reactiveproductservice.model.Product;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ProductRepository extends ReactiveCrudRepository<Product, Long> {
    
    // Custom query methods - Spring Data will implement these automatically!
    Flux<Product> findByNameContainingIgnoreCase(String name);
    
    Flux<Product> findByPriceLessThan(java.math.BigDecimal price);
}
