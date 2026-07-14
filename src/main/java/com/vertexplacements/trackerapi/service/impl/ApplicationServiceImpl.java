package com.vertexplacements.trackerapi.service.impl;

import com.vertexplacements.trackerapi.dto.ApplicationRequestDTO;
import com.vertexplacements.trackerapi.dto.ApplicationResponseDTO;
import com.vertexplacements.trackerapi.dto.StatusUpdateDTO;
import com.vertexplacements.trackerapi.entity.Application;
import com.vertexplacements.trackerapi.entity.ApplicationStatus;
import com.vertexplacements.trackerapi.entity.Company;
import com.vertexplacements.trackerapi.entity.User;
import com.vertexplacements.trackerapi.exception.ResourceNotFoundException;
import com.vertexplacements.trackerapi.repository.ApplicationRepository;
import com.vertexplacements.trackerapi.repository.CompanyRepository;
import com.vertexplacements.trackerapi.service.ApplicationService;
import com.vertexplacements.trackerapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final CompanyRepository companyRepository;
    private final UserService userService;

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponseDTO> getAllApplications(String ownerEmail) {
        Long ownerId = currentUserId(ownerEmail);
        return applicationRepository.findAllWithCompanyByOwnerId(ownerId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationResponseDTO getApplicationById(String ownerEmail, Long id) {
        Long ownerId = currentUserId(ownerEmail);
        Application application = applicationRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> ResourceNotFoundException.forApplication(id));
        return toResponseDTO(application);
    }

    @Override
    public ApplicationResponseDTO createApplication(String ownerEmail, ApplicationRequestDTO dto) {
        User owner = userService.getUserEntityByEmail(ownerEmail);

        // The company must belong to this same user — otherwise treat it as not found,
        // rather than revealing that a company with that id exists for someone else.
        Company company = companyRepository.findByIdAndOwnerId(dto.getCompanyId(), owner.getId())
                .orElseThrow(() -> ResourceNotFoundException.forCompany(dto.getCompanyId()));

        Application application = Application.builder()
                .studentName(dto.getStudentName().trim())
                .studentRoll(dto.getStudentRoll().trim().toUpperCase())
                .status(ApplicationStatus.APPLIED)
                .applyDate(LocalDate.now())
                .company(company)
                .owner(owner)
                .build();

        Application saved = applicationRepository.save(application);
        return toResponseDTO(saved);
    }

    @Override
    public ApplicationResponseDTO updateStatus(String ownerEmail, Long id, StatusUpdateDTO dto) {
        Long ownerId = currentUserId(ownerEmail);
        Application application = applicationRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> ResourceNotFoundException.forApplication(id));

        application.setStatus(dto.getStatus());
        Application saved = applicationRepository.save(application);
        return toResponseDTO(saved);
    }

    @Override
    public void deleteApplication(String ownerEmail, Long id) {
        Long ownerId = currentUserId(ownerEmail);
        if (!applicationRepository.existsByIdAndOwnerId(id, ownerId)) {
            throw ResourceNotFoundException.forApplication(id);
        }
        applicationRepository.deleteById(id);
    }

    private Long currentUserId(String email) {
        return userService.getUserEntityByEmail(email).getId();
    }

    private ApplicationResponseDTO toResponseDTO(Application application) {
        Company company = application.getCompany();
        return ApplicationResponseDTO.builder()
                .id(application.getId())
                .studentName(application.getStudentName())
                .studentRoll(application.getStudentRoll())
                .status(application.getStatus())
                .applyDate(application.getApplyDate())
                .companyId(company != null ? company.getId() : null)
                .companyName(company != null ? company.getName() : "Unknown")
                .companyCtc(company != null ? company.getCtc() : null)
                .build();
    }
}
