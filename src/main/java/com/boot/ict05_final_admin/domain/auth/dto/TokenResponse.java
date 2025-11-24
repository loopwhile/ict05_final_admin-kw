package com.boot.ict05_final_admin.domain.auth.dto;

public record TokenResponse(String accessToken, String refreshToken) {}