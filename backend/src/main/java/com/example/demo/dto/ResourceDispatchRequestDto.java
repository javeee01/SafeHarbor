package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ResourceDispatchRequestDto(
        @NotNull Long targetIncidentId,
        @NotNull Long inventoryItemId,
        @NotNull @Positive Integer dispatchedQuantity
) {
}
