package com.boot.ict05_final_admin.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class JoinRequest {
    @NotBlank private String name;
    @NotBlank private String phone;
    @Email @NotBlank private String email;
    @NotBlank private String password;
}
