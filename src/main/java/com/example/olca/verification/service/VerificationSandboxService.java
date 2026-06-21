package com.example.olca.verification.service;

import com.example.olca.verification.dto.VerificationRunEvidence;
import com.example.olca.verification.dto.VerificationSandboxRequest;
import com.example.olca.verification.dto.VerificationSandboxResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VerificationSandboxService {

    private static final DateTimeFormatter RUN_ID_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);

    private final VerificationEvidenceReader evidenceReader;

    public Mono<VerificationSandboxResponse> run(VerificationSandboxRequest request) {
        return Mono.fromCallable(() -> runBlocking(request))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private VerificationSandboxResponse runBlocking(VerificationSandboxRequest request) {
        String mode = request.normalizedMode();
        int repeat = request.normalizedRepeat();
        Path projectRoot = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        Path script = projectRoot.resolve("scripts/dev/verify-in-docker.sh");

        List<VerificationRunEvidence> runs = new ArrayList<>();

        for (int attempt = 1; attempt <= repeat; attempt++) {
            String runId = newRunId(mode, attempt);
            int exitCode = runScript(projectRoot, script, mode, runId);
            VerificationRunEvidence evidence = evidenceReader.read(projectRoot, runId);
            runs.add(evidence);

            if (exitCode != 0 || !evidence.success()) {
                return new VerificationSandboxResponse(false, mode, repeat, runs.size(), List.copyOf(runs));
            }
        }

        return new VerificationSandboxResponse(true, mode, repeat, runs.size(), List.copyOf(runs));
    }

    private int runScript(Path projectRoot, Path script, String mode, String runId) {
        ProcessBuilder processBuilder = new ProcessBuilder("bash", script.toString(), mode)
                .directory(projectRoot.toFile())
                .redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT);

        Map<String, String> environment = processBuilder.environment();
        environment.put("VERIFY_RUN_ID", runId);

        try {
            Process process = processBuilder.start();
            return process.waitFor();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to start verification sandbox script: " + script, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Verification sandbox script was interrupted.", e);
        }
    }

    private String newRunId(String mode, int attempt) {
        return RUN_ID_TIME_FORMATTER.format(Instant.now()) + "-" + mode + "-attempt-" + attempt;
    }
}
