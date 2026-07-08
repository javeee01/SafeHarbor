package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DisasterIncidentRequestDto(
        @NotBlank String title,
        @NotBlank String description,
        @NotBlank String incidentType,
        @NotBlank String severityLevel,
        @NotNull Double latitude,
        @NotNull Double longitude
) {
}
