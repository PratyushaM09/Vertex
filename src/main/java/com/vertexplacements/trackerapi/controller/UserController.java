package com.vertexplacements.trackerapi.controller;

import com.vertexplacements.trackerapi.dto.ChangePasswordRequestDTO;
import com.vertexplacements.trackerapi.dto.UpdateProfileRequestDTO;
import com.vertexplacements.trackerapi.dto.UserProfileResponseDTO;
import com.vertexplacements.trackerapi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "View and update your own profile (requires a valid JWT)")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get the currently logged-in user's profile")
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponseDTO> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(userService.getProfile(authentication.getName()));
    }

    @Operation(summary = "Update the currently logged-in user's display name")
    @PutMapping("/me")
    public ResponseEntity<UserProfileResponseDTO> updateMyProfile(
            Authentication authentication, @Valid @RequestBody UpdateProfileRequestDTO dto) {
        return ResponseEntity.ok(userService.updateProfile(authentication.getName(), dto));
    }

    @Operation(summary = "Change the currently logged-in user's password")
    @PutMapping("/me/password")
    public ResponseEntity<Void> changeMyPassword(
            Authentication authentication, @Valid @RequestBody ChangePasswordRequestDTO dto) {
        userService.changePassword(authentication.getName(), dto);
        return ResponseEntity.noContent().build();
    }
}
