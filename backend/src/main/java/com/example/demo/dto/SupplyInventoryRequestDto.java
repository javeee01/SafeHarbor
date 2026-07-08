package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record SupplyInventoryRequestDto(
        @NotBlank String itemName,
        @NotBlank String category,
        @PositiveOrZero Integer availableQuantity,
        @PositiveOrZero Integer criticalThreshold,
        @NotBlank String unit
) {
}
