package com.example.olca.verification.service;

import com.example.olca.verification.dto.VerificationRunEvidence;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class VerificationEvidenceReader {

    public VerificationRunEvidence read(Path projectRoot, String runId) {
        Path resultPath = projectRoot
                .resolve("verification-runs")
                .resolve(runId)
                .resolve("result.json");

        if (!Files.exists(resultPath)) {
            throw new IllegalStateException("Verification result not found: " + resultPath);
        }

        try {
            String resultJson = Files.readString(resultPath);
            return new VerificationRunEvidence(
                    readString(resultJson, "runId"),
                    readString(resultJson, "result"),
                    readBoolean(resultJson, "success"),
                    readInt(resultJson, "exitCode"),
                    readString(resultJson, "mode"),
                    readString(resultJson, "script"),
                    readString(resultJson, "image"),
                    readString(resultJson, "dockerfile"),
                    readString(resultJson, "network"),
                    readString(resultJson, "startedAt"),
                    readString(resultJson, "finishedAt"),
                    Map.of(
                            "dockerBuild", readString(resultJson, "dockerBuild"),
                            "stdout", readString(resultJson, "stdout"),
                            "stderr", readString(resultJson, "stderr"),
                            "summary", readString(resultJson, "summary")
                    )
            );
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read verification result: " + resultPath, e);
        }
    }

    private String readString(String json, String fieldName) {
        Matcher matcher = Pattern.compile("\\\"" + Pattern.quote(fieldName) + "\\\"\\s*:\\s*\\\"([^\\\"]*)\\\"")
                .matcher(json);
        if (!matcher.find()) {
            throw new IllegalStateException("Missing string field in verification result: " + fieldName);
        }
        return matcher.group(1);
    }

    private boolean readBoolean(String json, String fieldName) {
        Matcher matcher = Pattern.compile("\\\"" + Pattern.quote(fieldName) + "\\\"\\s*:\\s*(true|false)")
                .matcher(json);
        if (!matcher.find()) {
            throw new IllegalStateException("Missing boolean field in verification result: " + fieldName);
        }
        return Boolean.parseBoolean(matcher.group(1));
    }

    private int readInt(String json, String fieldName) {
        Matcher matcher = Pattern.compile("\\\"" + Pattern.quote(fieldName) + "\\\"\\s*:\\s*(-?\\d+)")
                .matcher(json);
        if (!matcher.find()) {
            throw new IllegalStateException("Missing integer field in verification result: " + fieldName);
        }
        return Integer.parseInt(matcher.group(1));
    }
}