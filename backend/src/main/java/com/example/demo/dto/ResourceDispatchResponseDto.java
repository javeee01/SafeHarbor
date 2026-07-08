package com.example.demo.dto;

import java.time.LocalDateTime;

public record ResourceDispatchResponseDto(
        Long id,
        String incidentTitle,
        String itemName,
        Integer dispatchedQuantity,
        String dispatchStatus,
        LocalDateTime initiatedAt
) {
}
