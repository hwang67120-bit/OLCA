package com.example.olca.verification.dto;

import java.util.Set;

public record VerificationSandboxRequest(
        String mode,
        Integer repeat
) {
    private static final Set<String> ALLOWED_MODES = Set.of("all", "backend", "rag");

    public String normalizedMode() {
        if (mode == null || mode.isBlank()) {
            return "all";
        }

        String normalized = mode.trim().toLowerCase();
        if (!ALLOWED_MODES.contains(normalized)) {
            throw new IllegalArgumentException("Unsupported verification mode: " + mode);
        }

        return normalized;
    }

    public int normalizedRepeat() {
        if (repeat == null) {
            return 1;
        }

        if (repeat < 1 || repeat > 4) {
            throw new IllegalArgumentException("repeat must be between 1 and 4");
        }

        return repeat;
    }
}
