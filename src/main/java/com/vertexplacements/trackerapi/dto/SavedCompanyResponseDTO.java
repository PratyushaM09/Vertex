package com.vertexplacements.trackerapi.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SavedCompanyResponseDTO {
    private Long companyId;
    private String name;
    private Double ctc;
    private String eligibilityCriteria;
    private LocalDate visitDate;
    private LocalDateTime savedAt;
}