package com.example.demo.dto;

import java.time.LocalDateTime;

public record DisasterIncidentResponseDto(
        Long id,
        String title,
        String description,
        String incidentType,
        String severityLevel,
        Double latitude,
        Double longitude,
        String status,
        LocalDateTime reportedAt,
        String assignedResponderUsername
) {
}
