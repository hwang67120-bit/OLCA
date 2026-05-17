package com.example.olca.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SessionCreateRequest(
        @NotNull(message = "User ID is required")
        Long userId,

        @NotBlank(message = "Title is required")
        String title
) {}