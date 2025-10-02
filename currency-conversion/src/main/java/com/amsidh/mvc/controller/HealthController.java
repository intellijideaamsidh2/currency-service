package com.amsidh.mvc.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amsidh.mvc.service.FallbackOrchestrationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@Tag(name = "Health", description = "Service health and status endpoints")
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthController {

    private final FallbackOrchestrationService fallbackOrchestrationService;

    /**
     * Health check endpoint for fallback providers
     */
    @GetMapping("/providers")
    @Operation(summary = "Get fallback provider statuses", description = "Returns health/status of configured fallback providers")
    public ResponseEntity<List<FallbackOrchestrationService.ProviderStatus>> getProviderStatus() {
        log.debug("Received provider status health check request");
        List<FallbackOrchestrationService.ProviderStatus> statuses = fallbackOrchestrationService.getProviderStatuses();
        return ResponseEntity.ok(statuses);
    }
}