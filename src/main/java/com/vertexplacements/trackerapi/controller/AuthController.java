package com.vertexplacements.trackerapi.controller;

import com.vertexplacements.trackerapi.dto.AuthResponseDTO;
import com.vertexplacements.trackerapi.dto.LoginRequestDTO;
import com.vertexplacements.trackerapi.dto.RegisterRequestDTO;
import com.vertexplacements.trackerapi.entity.User;
import com.vertexplacements.trackerapi.security.JwtService;
import com.vertexplacements.trackerapi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register a new account or log in to receive a JWT")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Operation(summary = "Create a new account and receive a JWT immediately")
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO dto) {
        User user = userService.registerUser(dto);
        String token = jwtService.generateToken(user.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(toAuthResponse(user, token));
    }

    @Operation(summary = "Log in with email and password to receive a JWT")
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        String normalizedEmail = dto.getEmail().trim().toLowerCase();

        // Throws BadCredentialsException on failure, handled by GlobalExceptionHandler -> 401
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, dto.getPassword())
        );

        User user = userService.getUserEntityByEmail(normalizedEmail);
        String token = jwtService.generateToken(user.getEmail());
        return ResponseEntity.ok(toAuthResponse(user, token));
    }

    private AuthResponseDTO toAuthResponse(User user, String token) {
        return AuthResponseDTO.builder()
                .token(token)
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .rollNumber(user.getRollNumber())
                .role(user.getRole())
                .build();
    }
}