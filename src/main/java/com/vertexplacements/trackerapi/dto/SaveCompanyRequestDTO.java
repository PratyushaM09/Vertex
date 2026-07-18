package com.vertexplacements.trackerapi.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SaveCompanyRequestDTO {
    @NotNull(message = "companyId is required")
    private Long companyId;
}