package com.example.olca.ai.routing;

public record IntentRoutingDecision(
        IntentRoute route,
        ExecutionPolicy policy,
        String reason,
        String reasonLabel
) {
}
