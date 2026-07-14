package com.vertexplacements.trackerapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyResponseDTO {
    private Long id;
    private String name;
    private Double ctc;
    private String eligibilityCriteria;
    private LocalDate visitDate;
    private long applicationCount;
    private LocalDateTime deletedAt;

}
