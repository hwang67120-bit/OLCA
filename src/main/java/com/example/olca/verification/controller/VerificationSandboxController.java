package com.example.olca.verification.controller;

import com.example.olca.verification.dto.VerificationSandboxRequest;
import com.example.olca.verification.dto.VerificationSandboxResponse;
import com.example.olca.verification.service.VerificationSandboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/verification")
@RequiredArgsConstructor
public class VerificationSandboxController {

    private final VerificationSandboxService verificationSandboxService;

    @PostMapping("/sandbox")
    public Mono<VerificationSandboxResponse> runSandbox(
            @RequestBody(required = false) VerificationSandboxRequest request
    ) {
        VerificationSandboxRequest safeRequest = request == null
                ? new VerificationSandboxRequest("all", 1)
                : request;

        return verificationSandboxService.run(safeRequest);
    }
}
