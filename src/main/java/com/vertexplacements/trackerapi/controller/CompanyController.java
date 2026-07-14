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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Tag(name = "Companies", description = "Manage your own recruiting companies and placement drives (private to your account)")
public class CompanyController {

    private final CompanyService companyService;

    @Operation(summary = "List your own companies, or filter by name / minimum CTC")
    @GetMapping
    public ResponseEntity<List<CompanyResponseDTO>> getCompanies(
            Authentication authentication,
            @Parameter(description = "Case-insensitive partial match on company name")
            @RequestParam(required = false) String name,
            @Parameter(description = "Minimum CTC in LPA")
            @RequestParam(required = false) Double minCtc) {

        String email = authentication.getName();
        if (name != null || minCtc != null) {
            return ResponseEntity.ok(companyService.filterCompanies(email, name, minCtc));
        }
        return ResponseEntity.ok(companyService.getAllCompanies(email));
    }

    @Operation(summary = "Get dashboard metrics for your own companies")
    @GetMapping("/stats")
    public ResponseEntity<CompanyStatsDTO> getStats(Authentication authentication) {
        return ResponseEntity.ok(companyService.getStats(authentication.getName()));
    }

    @Operation(summary = "Get a single company by id (must belong to you)")
    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponseDTO> getCompanyById(Authentication authentication, @PathVariable Long id) {
        return ResponseEntity.ok(companyService.getCompanyById(authentication.getName(), id));
    }

    @Operation(summary = "Register a new company for your own placement drive")
    @PostMapping
    public ResponseEntity<CompanyResponseDTO> createCompany(
            Authentication authentication, @Valid @RequestBody CompanyRequestDTO dto) {
        CompanyResponseDTO created = companyService.createCompany(authentication.getName(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Update one of your own companies")
    @PutMapping("/{id}")
    public ResponseEntity<CompanyResponseDTO> updateCompany(
            Authentication authentication, @PathVariable Long id, @Valid @RequestBody CompanyRequestDTO dto) {
        return ResponseEntity.ok(companyService.updateCompany(authentication.getName(), id, dto));
    }

    @Operation(summary = "Delete one of your own companies and its applications")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompany(Authentication authentication, @PathVariable Long id) {
        companyService.deleteCompany(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
