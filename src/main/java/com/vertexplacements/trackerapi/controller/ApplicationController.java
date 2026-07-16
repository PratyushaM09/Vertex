package com.vertexplacements.trackerapi.controller;

import com.vertexplacements.trackerapi.dto.ApplicationRequestDTO;
import com.vertexplacements.trackerapi.dto.ApplicationResponseDTO;
import com.vertexplacements.trackerapi.dto.StatusUpdateDTO;
import com.vertexplacements.trackerapi.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;


import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Tag(name = "Applications", description = "Manage your own student applications and their status (private to your account)")
public class ApplicationController {

    private final ApplicationService applicationService;

    @Operation(summary = "List your own applications")
    @GetMapping
    public ResponseEntity<List<ApplicationResponseDTO>> getApplications(Authentication authentication) {
        return ResponseEntity.ok(applicationService.getAllApplications(authentication.getName()));
    }

    @Operation(summary = "List every student's applications — Placement Officers only")
    @PreAuthorize("hasRole('PLACEMENT_OFFICER')")
    @GetMapping("/all")
    public ResponseEntity<List<ApplicationResponseDTO>> getAllApplicationsForOfficer() {
        return ResponseEntity.ok(applicationService.getAllApplicationsForOfficer());
    }

    @Operation(summary = "List your deleted (trashed) applications")
    @GetMapping("/trash")
    public ResponseEntity<List<ApplicationResponseDTO>> getTrash(Authentication authentication) {
        return ResponseEntity.ok(applicationService.getTrash(authentication.getName()));
    }

    @Operation(summary = "Get one of your own applications by id")
    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponseDTO> getApplicationById(Authentication authentication, @PathVariable Long id) {
        return ResponseEntity.ok(applicationService.getApplicationById(authentication.getName(), id));
    }

    @Operation(summary = "Apply a student to one of your own open company drives")
    @PostMapping
    public ResponseEntity<ApplicationResponseDTO> createApplication(
            Authentication authentication, @Valid @RequestBody ApplicationRequestDTO dto) {
        ApplicationResponseDTO created = applicationService.createApplication(authentication.getName(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Update one of your own application's status")
    @PatchMapping("/{id}")
    public ResponseEntity<ApplicationResponseDTO> updateStatus(
            Authentication authentication, @PathVariable Long id, @Valid @RequestBody StatusUpdateDTO dto) {
        return ResponseEntity.ok(applicationService.updateStatus(authentication.getName(), id, dto));
    }

    @Operation(summary = "Move one of your own applications to trash")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(Authentication authentication, @PathVariable Long id) {
        applicationService.deleteApplication(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Restore an application out of trash")
    @PutMapping("/{id}/restore")
    public ResponseEntity<ApplicationResponseDTO> restoreApplication(Authentication authentication, @PathVariable Long id) {
        return ResponseEntity.ok(applicationService.restoreApplication(authentication.getName(), id));
    }

    @Operation(summary = "Permanently delete an application that's already in trash")
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Void> permanentlyDeleteApplication(Authentication authentication, @PathVariable Long id) {
        applicationService.permanentlyDeleteApplication(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}