package com.vertexplacements.trackerapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyStatsDTO {
    private long totalCompanies;
    private double highestCtc;
    private long activeDrives;
}
