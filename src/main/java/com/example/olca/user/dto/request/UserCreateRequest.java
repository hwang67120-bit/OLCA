package com.example.olca.user.dto.request;


import jakarta.validation.constraints.NotBlank;

public record UserCreateRequest(
        @NotBlank(message = "Username is required")
        String username
) {
}