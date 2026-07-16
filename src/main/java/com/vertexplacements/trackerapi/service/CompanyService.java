package com.vertexplacements.trackerapi.service;

import com.vertexplacements.trackerapi.dto.CompanyRequestDTO;
import com.vertexplacements.trackerapi.dto.CompanyResponseDTO;
import com.vertexplacements.trackerapi.dto.CompanyStatsDTO;

import java.util.List;

public interface CompanyService {

    List<CompanyResponseDTO> getAllCompanies();

    List<CompanyResponseDTO> filterCompanies(String name, Double minCtc);

    CompanyResponseDTO getCompanyById(Long id);

    CompanyResponseDTO createCompany(String officerEmail, CompanyRequestDTO dto);

    CompanyResponseDTO updateCompany(Long id, CompanyRequestDTO dto);

    void deleteCompany(Long id);

    CompanyStatsDTO getStats();

    List<CompanyResponseDTO> getTrash();

    CompanyResponseDTO restoreCompany(Long id);

    void permanentlyDeleteCompany(Long id);
}