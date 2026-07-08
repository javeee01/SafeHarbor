package com.example.demo.dto;

public record PersonnelAccountResponseDto(
        Long id,
        String username,
        String fullName,
        String role,
        String contactNumber,
        String assignedRegion,
        boolean isActive
) {
}
