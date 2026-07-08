package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

public record PersonnelAccountRequestDto(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String fullName,
        @NotBlank String role,
        String contactNumber,
        String assignedRegion
) {
}
