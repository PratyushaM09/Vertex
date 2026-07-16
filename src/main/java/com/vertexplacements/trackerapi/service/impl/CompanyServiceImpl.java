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
    public List<CompanyResponseDTO> getAllCompanies() {
        return companyRepository.findByDeletedAtIsNull().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyResponseDTO> filterCompanies(String name, Double minCtc) {
        String normalizedName = (name == null || name.isBlank()) ? null : name.trim();
        double normalizedMinCtc = (minCtc == null) ? 0.0 : minCtc;
        return companyRepository.findByFilters(normalizedName, normalizedMinCtc).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyResponseDTO getCompanyById(Long id) {
        Company company = companyRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> ResourceNotFoundException.forCompany(id));
        return toResponseDTO(company);
    }

    @Override
    public CompanyResponseDTO createCompany(String officerEmail, CompanyRequestDTO dto) {
        User officer = userService.getUserEntityByEmail(officerEmail);

        Company company = Company.builder()
                .name(dto.getName().trim())
                .ctc(dto.getCtc())
                .eligibilityCriteria(dto.getEligibilityCriteria().trim())
                .visitDate(dto.getVisitDate())
                .owner(officer)
                .build();
        Company saved = companyRepository.save(company);
        return toResponseDTO(saved);
    }

    @Override
    public CompanyResponseDTO updateCompany(Long id, CompanyRequestDTO dto) {
        Company company = companyRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> ResourceNotFoundException.forCompany(id));

        company.setName(dto.getName().trim());
        company.setCtc(dto.getCtc());
        company.setEligibilityCriteria(dto.getEligibilityCriteria().trim());
        company.setVisitDate(dto.getVisitDate());

        Company saved = companyRepository.save(company);
        return toResponseDTO(saved);
    }

    @Override
    public void deleteCompany(Long id) {
        Company company = companyRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> ResourceNotFoundException.forCompany(id));
        company.setDeletedAt(LocalDateTime.now());
        companyRepository.save(company);
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyStatsDTO getStats() {
        long total = companyRepository.countByDeletedAtIsNull();
        double highest = companyRepository.findHighestCtc().orElse(0.0);
        long active = companyRepository.countByDeletedAtIsNullAndVisitDateGreaterThanEqual(LocalDate.now());

        return CompanyStatsDTO.builder()
                .totalCompanies(total)
                .highestCtc(highest)
                .activeDrives(active)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyResponseDTO> getTrash() {
        return companyRepository.findByDeletedAtIsNotNull().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public CompanyResponseDTO restoreCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forCompany(id));
        if (company.getDeletedAt() == null) {
            throw new ResourceNotFoundException("Company is not in trash: " + id);
        }
        company.setDeletedAt(null);
        Company saved = companyRepository.save(company);
        return toResponseDTO(saved);
    }

    @Override
    public void permanentlyDeleteCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forCompany(id));
        if (company.getDeletedAt() == null) {
            throw new ResourceNotFoundException("Company must be moved to trash before it can be permanently deleted: " + id);
        }
        companyRepository.deleteById(id);
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