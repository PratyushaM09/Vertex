package com.vertexplacements.trackerapi.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationRequestDTO {

    @NotNull(message = "companyId is required")
    private Long companyId;

    @Size(max = 50, message = "Roll number must be at most 50 characters")
    private String rollNumber;
}