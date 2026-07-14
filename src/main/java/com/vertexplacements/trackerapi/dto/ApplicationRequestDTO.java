package com.vertexplacements.trackerapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationRequestDTO {

    @NotBlank(message = "Student name is required")
    private String studentName;

    @NotBlank(message = "Roll number is required")
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Roll number must be alphanumeric")
    private String studentRoll;

    @NotNull(message = "companyId is required")
    private Long companyId;
}
