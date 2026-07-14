package com.vertexplacements.trackerapi.dto;

import com.vertexplacements.trackerapi.entity.ApplicationStatus;
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
public class ApplicationResponseDTO {
    private Long id;
    private String studentName;
    private String studentRoll;
    private ApplicationStatus status;
    private LocalDate applyDate;
    private Long companyId;
    private String companyName;
    private Double companyCtc;
    private LocalDateTime deletedAt;

}
