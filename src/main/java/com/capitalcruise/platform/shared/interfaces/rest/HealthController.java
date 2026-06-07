package com.capitalcruise.platform.shared.interfaces.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<HealthResource> health() {
        return ResponseEntity.ok(new HealthResource("UP", "capital-cruise-backend"));
    }

    public record HealthResource(String status, String service) {
    }
}
