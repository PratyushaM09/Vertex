package com.vertexplacements.trackerapi.service;

import com.vertexplacements.trackerapi.dto.CompanyRequestDTO;
import com.vertexplacements.trackerapi.dto.CompanyResponseDTO;
import com.vertexplacements.trackerapi.dto.CompanyStatsDTO;

import java.util.List;

public interface CompanyService {

    List<CompanyResponseDTO> getAllCompanies(String ownerEmail);

    List<CompanyResponseDTO> filterCompanies(String ownerEmail, String name, Double minCtc);

    CompanyResponseDTO getCompanyById(String ownerEmail, Long id);

    CompanyResponseDTO createCompany(String ownerEmail, CompanyRequestDTO dto);

    CompanyResponseDTO updateCompany(String ownerEmail, Long id, CompanyRequestDTO dto);

    void deleteCompany(String ownerEmail, Long id);

    CompanyStatsDTO getStats(String ownerEmail);
}
