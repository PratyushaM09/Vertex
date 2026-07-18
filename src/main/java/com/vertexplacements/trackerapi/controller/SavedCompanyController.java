package com.vertexplacements.trackerapi.controller;

import com.vertexplacements.trackerapi.dto.SaveCompanyRequestDTO;
import com.vertexplacements.trackerapi.dto.SavedCompanyResponseDTO;
import com.vertexplacements.trackerapi.entity.Company;
import com.vertexplacements.trackerapi.entity.SavedCompany;
import com.vertexplacements.trackerapi.entity.User;
import com.vertexplacements.trackerapi.exception.ResourceNotFoundException;
import com.vertexplacements.trackerapi.repository.CompanyRepository;
import com.vertexplacements.trackerapi.repository.SavedCompanyRepository;
import com.vertexplacements.trackerapi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/saved-companies")
@RequiredArgsConstructor
@Transactional
@Tag(name = "Saved Companies", description = "Your personal wishlist of companies")
public class SavedCompanyController {

    private final SavedCompanyRepository savedCompanyRepository;
    private final CompanyRepository companyRepository;
    private final UserService userService;

    @Operation(summary = "List your saved companies")
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<SavedCompanyResponseDTO>> list(Authentication auth) {
        User user = userService.getUserEntityByEmail(auth.getName());
        List<SavedCompanyResponseDTO> result = savedCompanyRepository
                .findActiveByUserId(user.getId()).stream()
                .map(this::toDTO)
                .toList();
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Save a company to your wishlist")
    @PostMapping
    public ResponseEntity<SavedCompanyResponseDTO> save(
            Authentication auth, @Valid @RequestBody SaveCompanyRequestDTO dto) {
        User user = userService.getUserEntityByEmail(auth.getName());
        Company company = companyRepository.findByIdAndDeletedAtIsNull(dto.getCompanyId())
                .orElseThrow(() -> ResourceNotFoundException.forCompany(dto.getCompanyId()));

        // Idempotent: saving twice just returns the existing entry.
        SavedCompany saved = savedCompanyRepository
                .findByUserIdAndCompanyId(user.getId(), company.getId())
                .orElseGet(() -> savedCompanyRepository.save(
                        SavedCompany.builder().user(user).company(company).build()));

        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(saved));
    }

    @Operation(summary = "Remove a company from your wishlist")
    @DeleteMapping("/{companyId}")
    public ResponseEntity<Void> remove(Authentication auth, @PathVariable Long companyId) {
        User user = userService.getUserEntityByEmail(auth.getName());
        savedCompanyRepository.findByUserIdAndCompanyId(user.getId(), companyId)
                .ifPresent(savedCompanyRepository::delete);
        return ResponseEntity.noContent().build();
    }

    private SavedCompanyResponseDTO toDTO(SavedCompany s) {
        Company c = s.getCompany();
        return SavedCompanyResponseDTO.builder()
                .companyId(c.getId())
                .name(c.getName())
                .ctc(c.getCtc())
                .eligibilityCriteria(c.getEligibilityCriteria())
                .visitDate(c.getVisitDate())
                .savedAt(s.getSavedAt())
                .build();
    }
}