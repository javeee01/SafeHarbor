package com.example.demo.dto;

public record SupplyInventoryResponseDto(
        Long id,
        String itemName,
        String category,
        Integer availableQuantity,
        Integer reservedQuantity,
        Integer criticalThreshold,
        String unit
) {
}
