package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record ReliefShelterRequestDto(
        @NotBlank String shelterName,
        @NotBlank String locationAddress,
        @NotNull @Positive Integer capacity,
        @PositiveOrZero Integer currentOccupancy,
        String managerName
) {
}
