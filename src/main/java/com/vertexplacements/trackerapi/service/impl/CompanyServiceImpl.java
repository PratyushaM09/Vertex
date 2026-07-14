package com.vertexplacements.trackerapi.service.impl;

import com.vertexplacements.trackerapi.dto.CompanyRequestDTO;
import com.vertexplacements.trackerapi.dto.CompanyResponseDTO;
import com.vertexplacements.trackerapi.dto.CompanyStatsDTO;
import com.vertexplacements.trackerapi.entity.Company;
import com.vertexplacements.trackerapi.entity.User;
import com.vertexplacements.trackerapi.exception.ResourceNotFoundException;
import com.vertexplacements.trackerapi.repository.CompanyRepository;
import com.vertexplacements.trackerapi.service.CompanyService;
import com.vertexplacements.trackerapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final UserService userService;

    @Override
    @Transactional(readOnly = true)
    public List<CompanyResponseDTO> getAllCompanies(String ownerEmail) {
        Long ownerId = currentUserId(ownerEmail);
        return companyRepository.findByOwnerIdAndDeletedAtIsNull(ownerId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyResponseDTO> filterCompanies(String ownerEmail, String name, Double minCtc) {
        Long ownerId = currentUserId(ownerEmail);
        String normalizedName = (name == null || name.isBlank()) ? null : name.trim();
        double normalizedMinCtc = (minCtc == null) ? 0.0 : minCtc;
        return companyRepository.findByFilters(ownerId, normalizedName, normalizedMinCtc).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyResponseDTO getCompanyById(String ownerEmail, Long id) {
        Long ownerId = currentUserId(ownerEmail);
        Company company = companyRepository.findByIdAndOwnerIdAndDeletedAtIsNull(id, ownerId)
                .orElseThrow(() -> ResourceNotFoundException.forCompany(id));
        return toResponseDTO(company);
    }

    @Override
    public CompanyResponseDTO createCompany(String ownerEmail, CompanyRequestDTO dto) {
        User owner = userService.getUserEntityByEmail(ownerEmail);

        Company company = Company.builder()
                .name(dto.getName().trim())
                .ctc(dto.getCtc())
                .eligibilityCriteria(dto.getEligibilityCriteria().trim())
                .visitDate(dto.getVisitDate())
                .owner(owner)
                .build();
        Company saved = companyRepository.save(company);
        return toResponseDTO(saved);
    }

    @Override
    public CompanyResponseDTO updateCompany(String ownerEmail, Long id, CompanyRequestDTO dto) {
        Long ownerId = currentUserId(ownerEmail);
        Company company = companyRepository.findByIdAndOwnerIdAndDeletedAtIsNull(id, ownerId)
                .orElseThrow(() -> ResourceNotFoundException.forCompany(id));

        company.setName(dto.getName().trim());
        company.setCtc(dto.getCtc());
        company.setEligibilityCriteria(dto.getEligibilityCriteria().trim());
        company.setVisitDate(dto.getVisitDate());

        Company saved = companyRepository.save(company);
        return toResponseDTO(saved);
    }

    @Override
    public void deleteCompany(String ownerEmail, Long id) {
        Long ownerId = currentUserId(ownerEmail);
        Company company = companyRepository.findByIdAndOwnerIdAndDeletedAtIsNull(id, ownerId)
                .orElseThrow(() -> ResourceNotFoundException.forCompany(id));
        company.setDeletedAt(LocalDateTime.now());
        companyRepository.save(company);
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyStatsDTO getStats(String ownerEmail) {
        Long ownerId = currentUserId(ownerEmail);
        long total = companyRepository.countByOwnerIdAndDeletedAtIsNull(ownerId);
        double highest = companyRepository.findHighestCtcByOwnerId(ownerId).orElse(0.0);
        long active = companyRepository.countByOwnerIdAndDeletedAtIsNullAndVisitDateGreaterThanEqual(ownerId, LocalDate.now());

        return CompanyStatsDTO.builder()
                .totalCompanies(total)
                .highestCtc(highest)
                .activeDrives(active)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyResponseDTO> getTrash(String ownerEmail) {
        Long ownerId = currentUserId(ownerEmail);
        return companyRepository.findByOwnerIdAndDeletedAtIsNotNull(ownerId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public CompanyResponseDTO restoreCompany(String ownerEmail, Long id) {
        Long ownerId = currentUserId(ownerEmail);
        Company company = companyRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> ResourceNotFoundException.forCompany(id));
        if (company.getDeletedAt() == null) {
            throw new ResourceNotFoundException("Company is not in trash: " + id);
        }
        company.setDeletedAt(null);
        Company saved = companyRepository.save(company);
        return toResponseDTO(saved);
    }

    @Override
    public void permanentlyDeleteCompany(String ownerEmail, Long id) {
        Long ownerId = currentUserId(ownerEmail);
        Company company = companyRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> ResourceNotFoundException.forCompany(id));
        if (company.getDeletedAt() == null) {
            throw new ResourceNotFoundException("Company must be moved to trash before it can be permanently deleted: " + id);
        }
        companyRepository.deleteById(id);
    }

    private Long currentUserId(String email) {
        return userService.getUserEntityByEmail(email).getId();
    }

    private CompanyResponseDTO toResponseDTO(Company company) {
        return CompanyResponseDTO.builder()
                .id(company.getId())
                .name(company.getName())
                .ctc(company.getCtc())
                .eligibilityCriteria(company.getEligibilityCriteria())
                .visitDate(company.getVisitDate())
                .applicationCount(company.getApplications() == null ? 0 : company.getApplications().size())
                .deletedAt(company.getDeletedAt())
                .build();
    }
}