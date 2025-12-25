package com.example.reactiveproductservice.handler;

import com.example.reactiveproductservice.dto.AuthRequest;
import com.example.reactiveproductservice.dto.AuthResponse;
import com.example.reactiveproductservice.dto.RegisterRequest;
import com.example.reactiveproductservice.model.User;
import com.example.reactiveproductservice.repository.UserRepository;
import com.example.reactiveproductservice.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Functional handler for authentication endpoints
 * Demonstrates FUNCTIONAL STYLE vs Annotated Controllers
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthHandler {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    /**
     * POST /api/auth/register
     * Register a new user
     */
    public Mono<ServerResponse> register(ServerRequest request) {
        return request.bodyToMono(RegisterRequest.class)
                .flatMap(registerRequest -> {
                    log.info("Registration attempt for username: {}", registerRequest.getUsername());
                    
                    // Check if username exists
                    return userRepository.existsByUsername(registerRequest.getUsername())
                            .flatMap(exists -> {
                                if (exists) {
                                    return ServerResponse.badRequest()
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .bodyValue(new AuthResponse(null, null, "Username already exists"));
                                }
                                
                                // Check if email exists
                                return userRepository.existsByEmail(registerRequest.getEmail())
                                        .flatMap(emailExists -> {
                                            if (emailExists) {
                                                return ServerResponse.badRequest()
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .bodyValue(new AuthResponse(null, null, "Email already exists"));
                                            }
                                            
                                            // Create new user
                                            User user = new User();
                                            user.setUsername(registerRequest.getUsername());
                                            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
                                            user.setEmail(registerRequest.getEmail());
                                            user.setRoles("ROLE_USER");
                                            user.setEnabled(true);
                                            user.setCreatedAt(LocalDateTime.now());
                                            user.setUpdatedAt(LocalDateTime.now());
                                            
                                            return userRepository.save(user)
                                                    .flatMap(savedUser -> {
                                                        log.info("User registered successfully: {}", savedUser.getUsername());
                                                        return ServerResponse.status(HttpStatus.CREATED)
                                                                .contentType(MediaType.APPLICATION_JSON)
                                                                .bodyValue(new AuthResponse(
                                                                        null,
                                                                        savedUser.getUsername(),
                                                                        "User registered successfully"
                                                                ));
                                                    });
                                        });
                            });
                })
                .onErrorResume(e -> {
                    log.error("Registration error", e);
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(new AuthResponse(null, null, "Registration failed: " + e.getMessage()));
                });
    }
    
    /**
     * POST /api/auth/login
     * Login and get JWT token
     */
    public Mono<ServerResponse> login(ServerRequest request) {
        return request.bodyToMono(AuthRequest.class)
                .flatMap(authRequest -> {
                    log.info("Login attempt for username: {}", authRequest.getUsername());
                    
                    return userRepository.findByUsername(authRequest.getUsername())
                            .flatMap(user -> {
                                // Check if user is enabled
                                if (!user.getEnabled()) {
                                    return ServerResponse.status(HttpStatus.FORBIDDEN)
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .bodyValue(new AuthResponse(null, null, "Account is disabled"));
                                }
                                
                                // Verify password
                                if (passwordEncoder.matches(authRequest.getPassword(), user.getPassword())) {
                                    // Generate JWT token
                                    String token = jwtUtil.generateToken(user.getUsername());
                                    
                                    log.info("Login successful for user: {}", user.getUsername());
                                    
                                    return ServerResponse.ok()
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .bodyValue(new AuthResponse(
                                                    token,
                                                    user.getUsername(),
                                                    "Login successful"
                                            ));
                                } else {
                                    log.warn("Invalid password for user: {}", user.getUsername());
                                    return ServerResponse.status(HttpStatus.UNAUTHORIZED)
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .bodyValue(new AuthResponse(null, null, "Invalid credentials"));
                                }
                            })
                            .switchIfEmpty(
                                    ServerResponse.status(HttpStatus.UNAUTHORIZED)
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .bodyValue(new AuthResponse(null, null, "Invalid credentials"))
                            );
                })
                .onErrorResume(e -> {
                    log.error("Login error", e);
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(new AuthResponse(null, null, "Login failed: " + e.getMessage()));
                });
    }
    
    /**
     * GET /api/auth/me
     * Get current user info (requires authentication)
     */
    public Mono<ServerResponse> getCurrentUser(ServerRequest request) {
        // In real app, extract username from JWT in security context
        // For now, return example response
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new AuthResponse(null, "current-user", "Token is valid"));
    }
}
