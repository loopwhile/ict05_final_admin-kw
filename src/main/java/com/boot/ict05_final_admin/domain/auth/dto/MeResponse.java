package com.boot.ict05_final_admin.domain.auth.dto;

public record MeResponse(String username, boolean social, String nickname, String email) {}