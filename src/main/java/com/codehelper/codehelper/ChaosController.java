
package com.codehelper.codehelper;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chaos")
public class ChaosController {

    private final ChaosFailureService chaosFailureService;
    private final CheckController checkController;

    public ChaosController(
            ChaosFailureService chaosFailureService,
            CheckController checkController) {
        this.chaosFailureService = chaosFailureService;
        this.checkController = checkController;
    }

    @PostMapping("/arm")
    public ResponseEntity<Map<String, Object>> armFailure(
            @RequestBody(required = false) Map<String, Integer> request) {

        if (!chaosFailureService.isSelfHealingReady()) {
            return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "Pass the Chaos Lab self-healing test first."
            ));
        }

        int count = request == null ? 1 : request.getOrDefault("count", 1);
        int armed = chaosFailureService.arm(count);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "armed", armed,
                "message", "Failure injector ready."
        ));
    }

    @PostMapping("/arm-rate-limit")
    public ResponseEntity<Map<String, Object>> armRateLimit(
            @RequestBody(required = false) Map<String, Integer> request) {

        if (!chaosFailureService.isSelfHealingReady()) {
            return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "Pass the Chaos Lab self-healing test first."
            ));
        }

        int count = request == null ? 1 : request.getOrDefault("count", 1);
        int armed = chaosFailureService.armRateLimit(count);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "armed", armed,
                "message", "Rate-limit injector ready."
        ));
    }

    @PostMapping("/run-test")
    public ResponseEntity<Map<String, Object>> runTest() {

        chaosFailureService.setSelfHealingReady(false);
        chaosFailureService.arm(1);

        try {
            ResponseEntity<Map<String, Object>> response =
                    checkController.checkCode(Map.of(
                            "code", "public class RecoveryTest { public static void main(String[] args) { System.out.println(\"OK\"); } }",
                            "language", "java",
                            "chaosTest", "true"
                    ));

            Map<String, Object> body = response.getBody();

            boolean recovered =
                    response.getStatusCode().is2xxSuccessful()
                    && body != null
                    && Boolean.TRUE.equals(body.get("recovered"));

            chaosFailureService.setSelfHealingReady(recovered);

            if (recovered) {
                return ResponseEntity.ok(Map.of(
                        "status", "RECOVERED",
                        "message", "Self-healing completed successfully."
                ));
            }

            return ResponseEntity.status(503).body(Map.of(
                    "status", "FAILED",
                    "message", "Self-healing could not be completed. Check the server configuration."
            ));

        } catch (Exception exception) {
            chaosFailureService.setSelfHealingReady(false);

            return ResponseEntity.status(503).body(Map.of(
                    "status", "FAILED",
                    "message", "Self-healing could not be completed. Check the server configuration."
            ));
        }
    }

    @GetMapping("/readiness")
    public Map<String, Object> readiness() {
        boolean ready = chaosFailureService.isSelfHealingReady();

        return Map.of(
                "ready", ready,
                "message", ready ? "READY" : "LOCKED"
        );
    }
}
