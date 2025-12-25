package com.example.reactiveproductservice.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * JWT Authentication Filter
 * 
 * Intercepts requests, validates JWT tokens, and sets security context
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationWebFilter implements WebFilter {
    
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        
        // Skip authentication for public endpoints
        if (path.startsWith("/api/auth/")) {
            return chain.filter(exchange);
        }
        
        // Get Authorization header
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            try {
                // Validate token
                if (jwtUtil.validateToken(token)) {
                    String username = jwtUtil.getUsernameFromToken(token);
                    
                    log.debug("Valid JWT token for user: {}", username);
                    
                    // Load user with roles from database
                    return userDetailsService.findByUsername(username)
                            .flatMap(userDetails -> {
                                // Create authentication with actual roles
                                UsernamePasswordAuthenticationToken authentication = 
                                    new UsernamePasswordAuthenticationToken(
                                        userDetails.getUsername(), 
                                        null, 
                                        userDetails.getAuthorities()
                                    );
                                
                                log.debug("User {} has roles: {}", username, userDetails.getAuthorities());
                                
                                // Set authentication in security context and continue
                                return chain.filter(exchange)
                                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
                            })
                            .onErrorResume(e -> {
                                log.error("Error loading user: {}", e.getMessage());
                                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                                return exchange.getResponse().setComplete();
                            });
                }
            } catch (Exception e) {
                log.error("JWT validation error: {}", e.getMessage());
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        }
        
        // No valid token - continue without authentication
        // SecurityConfig will block if endpoint requires auth
        return chain.filter(exchange);
    }
}
