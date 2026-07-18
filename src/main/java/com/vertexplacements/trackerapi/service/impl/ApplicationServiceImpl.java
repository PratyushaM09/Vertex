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
import  com.vertexplacements.trackerapi.entity.UserRole;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
        Application application = applicationRepository.findByIdAndOwnerIdAndDeletedAtIsNull(id, ownerId)
                .orElseThrow(() -> ResourceNotFoundException.forApplication(id));
        return toResponseDTO(application);
    }

    @Override
    public ApplicationResponseDTO createApplication(String ownerEmail, ApplicationRequestDTO dto) {
        User student = userService.getUserEntityByEmail(ownerEmail);

        // Companies are shared now — any active company can be applied to, not just "your own".
        Company company = companyRepository.findByIdAndDeletedAtIsNull(dto.getCompanyId())
                .orElseThrow(() -> ResourceNotFoundException.forCompany(dto.getCompanyId()));

        // Name and roll number come from the student's own profile, not the request body.
        Application application = Application.builder()
                .studentName(student.getFullName())
                .studentRoll(student.getRollNumber())
                .status(ApplicationStatus.APPLIED)
                .applyDate(LocalDate.now())
                .company(company)
                .owner(student)
                .build();

        Application saved = applicationRepository.save(application);
        return toResponseDTO(saved);

        // Roll number: use profile, or accept it once from the request and autosave it.
        if (student.getRollNumber() == null || student.getRollNumber().isBlank()) {
            if (dto.getRollNumber() == null || dto.getRollNumber().isBlank()) {
                throw new IllegalArgumentException("Add your roll number to apply");
            }
            student.setRollNumber(dto.getRollNumber().trim()); // managed entity — persisted on commit
        }

// One application per company per student.
        if (applicationRepository.existsByOwnerIdAndCompanyIdAndDeletedAtIsNull(
                student.getId(), company.getId())) {
            throw new IllegalArgumentException("You've already applied to this company");
        }
    }

    @Override
    public ApplicationResponseDTO updateStatus(String ownerEmail, Long id, StatusUpdateDTO dto) {
        Application application = findOwnedOrOfficer(ownerEmail, id);
        application.setStatus(dto.getStatus());
        return toResponseDTO(applicationRepository.save(application));
    }

    @Override
    public void deleteApplication(String ownerEmail, Long id) {
        Application application = findOwnedOrOfficer(ownerEmail, id);
        application.setDeletedAt(LocalDateTime.now());
        applicationRepository.save(application);
    }

    /** Officers can act on any active application; students only on their own. */
    private Application findOwnedOrOfficer(String email, Long id) {
        User caller = userService.getUserEntityByEmail(email);
        if (caller.getRole() == UserRole.PLACEMENT_OFFICER) {
            return applicationRepository.findByIdAndDeletedAtIsNull(id)
                    .orElseThrow(() -> ResourceNotFoundException.forApplication(id));
        }
        return applicationRepository.findByIdAndOwnerIdAndDeletedAtIsNull(id, caller.getId())
                .orElseThrow(() -> ResourceNotFoundException.forApplication(id));
    }



    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponseDTO> getTrash(String ownerEmail) {
        Long ownerId = currentUserId(ownerEmail);
        return applicationRepository.findDeletedByOwnerId(ownerId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public ApplicationResponseDTO restoreApplication(String ownerEmail, Long id) {
        Long ownerId = currentUserId(ownerEmail);
        Application application = applicationRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> ResourceNotFoundException.forApplication(id));
        if (application.getDeletedAt() == null) {
            throw new ResourceNotFoundException("Application is not in trash: " + id);
        }
        application.setDeletedAt(null);
        Application saved = applicationRepository.save(application);
        return toResponseDTO(saved);
    }

    @Override
    public void permanentlyDeleteApplication(String ownerEmail, Long id) {
        Long ownerId = currentUserId(ownerEmail);
        Application application = applicationRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> ResourceNotFoundException.forApplication(id));
        if (application.getDeletedAt() == null) {
            throw new ResourceNotFoundException("Application must be moved to trash before it can be permanently deleted: " + id);
        }
        applicationRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponseDTO> getAllApplicationsForOfficer() {
        return applicationRepository.findAllActiveWithCompanyAndOwner().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    private Long currentUserId(String email) {
        return userService.getUserEntityByEmail(email).getId();
    }

    private ApplicationResponseDTO toResponseDTO(Application application) {
        Company company = application.getCompany();
        User owner = application.getOwner();
        return ApplicationResponseDTO.builder()
                .id(application.getId())
                .studentName(application.getStudentName())
                .studentRoll(application.getStudentRoll())
                .studentEmail(owner != null ? owner.getEmail() : null)
                .status(application.getStatus())
                .applyDate(application.getApplyDate())
                .companyId(company != null ? company.getId() : null)
                .companyName(company != null ? company.getName() : "Unknown")
                .companyCtc(company != null ? company.getCtc() : null)
                .deletedAt(application.getDeletedAt())
                .build();
    }
}