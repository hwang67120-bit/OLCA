package com.example.olca.verification.dto;

import java.util.Map;

public record VerificationRunEvidence(
        String runId,
        String result,
        boolean success,
        int exitCode,
        String mode,
        String script,
        String image,
        String dockerfile,
        String network,
        String startedAt,
        String finishedAt,
        Map<String, String> logs
) {
}
