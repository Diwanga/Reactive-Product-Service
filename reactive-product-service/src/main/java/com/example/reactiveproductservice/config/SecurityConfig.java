package com.example.reactiveproductservice.config;

import com.example.reactiveproductservice.security.JwtAuthenticationWebFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Reactive Security Configuration
 * 
 * Configures Spring Security for WebFlux (reactive) with JWT and role-based authorization
 */
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity  // Enable method-level security
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationWebFilter jwtAuthenticationWebFilter;
    
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                // Disable CSRF for REST API
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                
                // Add JWT filter before authorization
                .addFilterAt(jwtAuthenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                
                // Configure authorization rules
                .authorizeExchange(exchanges -> exchanges
                        // Public endpoints - no authentication required
                        .pathMatchers("/api/auth/**").permitAll()
                        
                        // Product GET endpoints - authenticated users only
                        .pathMatchers(HttpMethod.GET, "/api/products/**").authenticated()
                        
                        // Product POST - authenticated users only
                        .pathMatchers(HttpMethod.POST, "/api/products").authenticated()
                        
                        // Product PUT, DELETE - ADMIN only
                        .pathMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
                        
                        // All other endpoints require authentication
                        .anyExchange().authenticated()
                )
                
                // Disable form login (we use JWT)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                
                // Disable HTTP Basic
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                
                .build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
