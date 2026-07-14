package com.vertexplacements.trackerapi.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyRequestDTO {

    @NotBlank(message = "Company name is required")
    @Size(max = 150, message = "Company name must be at most 150 characters")
    private String name;

    @NotNull(message = "CTC is required")
    @Positive(message = "CTC must be a positive number")
    @Max(value = 500, message = "CTC seems unrealistically high (max 500 LPA)")
    private Double ctc;

    @NotBlank(message = "Eligibility criteria is required")
    @Size(max = 500, message = "Eligibility criteria must be at most 500 characters")
    private String eligibilityCriteria;

    @NotNull(message = "Visit date is required")
    private LocalDate visitDate;
}
