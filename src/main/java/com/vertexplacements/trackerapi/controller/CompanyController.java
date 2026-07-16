package com.vertexplacements.trackerapi.controller;

import com.vertexplacements.trackerapi.dto.CompanyRequestDTO;
import com.vertexplacements.trackerapi.dto.CompanyResponseDTO;
import com.vertexplacements.trackerapi.dto.CompanyStatsDTO;
import com.vertexplacements.trackerapi.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Tag(name = "Companies", description = "Shared placement drives. Every account can browse; only Placement Officers can manage them.")
public class CompanyController {

    private final CompanyService companyService;

    @Operation(summary = "List all companies, or filter by name / minimum CTC (open to every account)")
    @GetMapping
    public ResponseEntity<List<CompanyResponseDTO>> getCompanies(
            @Parameter(description = "Case-insensitive partial match on company name")
            @RequestParam(required = false) String name,
            @Parameter(description = "Minimum CTC in LPA")
            @RequestParam(required = false) Double minCtc) {

        if (name != null || minCtc != null) {
            return ResponseEntity.ok(companyService.filterCompanies(name, minCtc));
        }
        return ResponseEntity.ok(companyService.getAllCompanies());
    }

    @Operation(summary = "Get dashboard metrics across all companies (open to every account)")
    @GetMapping("/stats")
    public ResponseEntity<CompanyStatsDTO> getStats() {
        return ResponseEntity.ok(companyService.getStats());
    }

    @Operation(summary = "List deleted (trashed) companies — Placement Officers only")
    @PreAuthorize("hasRole('PLACEMENT_OFFICER')")
    @GetMapping("/trash")
    public ResponseEntity<List<CompanyResponseDTO>> getTrash() {
        return ResponseEntity.ok(companyService.getTrash());
    }

    @Operation(summary = "Get a single company by id (open to every account)")
    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponseDTO> getCompanyById(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.getCompanyById(id));
    }

    @Operation(summary = "Register a new company drive — Placement Officers only")
    @PreAuthorize("hasRole('PLACEMENT_OFFICER')")
    @PostMapping
    public ResponseEntity<CompanyResponseDTO> createCompany(
            Authentication authentication, @Valid @RequestBody CompanyRequestDTO dto) {
        CompanyResponseDTO created = companyService.createCompany(authentication.getName(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Update a company — Placement Officers only")
    @PreAuthorize("hasRole('PLACEMENT_OFFICER')")
    @PutMapping("/{id}")
    public ResponseEntity<CompanyResponseDTO> updateCompany(
            @PathVariable Long id, @Valid @RequestBody CompanyRequestDTO dto) {
        return ResponseEntity.ok(companyService.updateCompany(id, dto));
    }

    @Operation(summary = "Move a company to trash — Placement Officers only")
    @PreAuthorize("hasRole('PLACEMENT_OFFICER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Restore a company out of trash — Placement Officers only")
    @PreAuthorize("hasRole('PLACEMENT_OFFICER')")
    @PutMapping("/{id}/restore")
    public ResponseEntity<CompanyResponseDTO> restoreCompany(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.restoreCompany(id));
    }

    @Operation(summary = "Permanently delete a trashed company — Placement Officers only")
    @PreAuthorize("hasRole('PLACEMENT_OFFICER')")
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Void> permanentlyDeleteCompany(@PathVariable Long id) {
        companyService.permanentlyDeleteCompany(id);
        return ResponseEntity.noContent().build();
    }
}