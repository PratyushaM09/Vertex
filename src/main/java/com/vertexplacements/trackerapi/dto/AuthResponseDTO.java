package com.vertexplacements.trackerapi.dto;

import com.vertexplacements.trackerapi.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {
    private String token;
    private Long id;
    private String fullName;
    private String email;
    private String rollNumber;
    private UserRole role;
}