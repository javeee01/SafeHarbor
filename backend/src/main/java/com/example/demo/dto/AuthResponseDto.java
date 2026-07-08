package com.example.demo.dto;

public record AuthResponseDto(
        String accessToken,
        String refreshToken,
        String username,
        String role,
        String fullName
) {
}
