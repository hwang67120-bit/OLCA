package com.example.olca.verification.dto;

import java.util.List;

public record VerificationSandboxResponse(
        boolean success,
        String mode,
        int requestedRepeat,
        int completedRuns,
        List<VerificationRunEvidence> runs
) {
}
