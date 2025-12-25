package com.example.reactiveproductservice.router;

import com.example.reactiveproductservice.handler.AuthHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * Functional Router Configuration
 * 
 * This demonstrates FUNCTIONAL ENDPOINTS (Router Functions)
 * vs the traditional @RestController approach used in ProductController
 * 
 * Benefits of Functional Approach:
 * - More type-safe
 * - Better for complex routing logic
 * - Easier to test (pure functions)
 * - Functional programming style
 */
@Configuration
public class AuthRouter {
    
    /**
     * Define authentication routes using functional style
     * 
     * Compare this with ProductController's @GetMapping, @PostMapping annotations!
     */
    @Bean
    public RouterFunction<ServerResponse> authRoutes(AuthHandler authHandler) {
        return route()
                // POST /api/auth/register
                .POST("/api/auth/register", 
                      accept(MediaType.APPLICATION_JSON), 
                      authHandler::register)
                
                // POST /api/auth/login
                .POST("/api/auth/login", 
                      accept(MediaType.APPLICATION_JSON), 
                      authHandler::login)
                
                // GET /api/auth/me
                .GET("/api/auth/me", 
                     accept(MediaType.APPLICATION_JSON), 
                     authHandler::getCurrentUser)
                
                .build();
    }
    
    /* 
     * COMPARISON:
     * 
     * ANNOTATED STYLE (ProductController):
     * @PostMapping("/api/products")
     * public Mono<Product> createProduct(@RequestBody ProductRequest request) { }
     * 
     * FUNCTIONAL STYLE (Above):
     * .POST("/api/auth/register", accept(JSON), authHandler::register)
     * 
     * Both work in Spring WebFlux! You can mix them in the same project.
     */
}
