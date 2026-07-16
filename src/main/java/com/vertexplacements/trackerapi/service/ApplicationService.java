package com.vertexplacements.trackerapi.service;

import com.vertexplacements.trackerapi.dto.ApplicationRequestDTO;
import com.vertexplacements.trackerapi.dto.ApplicationResponseDTO;
import com.vertexplacements.trackerapi.dto.StatusUpdateDTO;

import java.util.List;

public interface ApplicationService {

    List<ApplicationResponseDTO> getAllApplications(String ownerEmail);

    ApplicationResponseDTO getApplicationById(String ownerEmail, Long id);

    ApplicationResponseDTO createApplication(String ownerEmail, ApplicationRequestDTO dto);

    ApplicationResponseDTO updateStatus(String ownerEmail, Long id, StatusUpdateDTO dto);

    void deleteApplication(String ownerEmail, Long id);

    List<ApplicationResponseDTO> getTrash(String ownerEmail);

    ApplicationResponseDTO restoreApplication(String ownerEmail, Long id);

    void permanentlyDeleteApplication(String ownerEmail, Long id);

    List<ApplicationResponseDTO> getAllApplicationsForOfficer();
}