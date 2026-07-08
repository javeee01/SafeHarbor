package com.example.demo.dto;

public record ReliefShelterResponseDto(
        Long id,
        String shelterName,
        String locationAddress,
        Integer capacity,
        Integer currentOccupancy,
        String managerName,
        boolean isActive
) {
}
