
package com.codehelper.codehelper;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/chaos")
public class CrashOutController {

    @PostMapping("/crash-out")
    public ResponseEntity<Map<String, Object>> crashOut(
            HttpServletRequest request) {

        String remoteAddress = request.getRemoteAddr();

        boolean localRequest =
                "127.0.0.1".equals(remoteAddress)
                || "::1".equals(remoteAddress)
                || "0:0:0:0:0:0:0:1".equals(remoteAddress);

        if (!localRequest) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of(
                            "success", false,
                            "message", "Crash-Out is restricted to the local machine."
                    )
            );
        }

        Thread shutdownThread = new Thread(() -> {
            try {
                Thread.sleep(1200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            System.exit(42);
        });

        shutdownThread.setName("controlled-crash-out");
        shutdownThread.setDaemon(false);
        shutdownThread.start();

        return ResponseEntity.accepted().body(
                Map.of(
                        "success", true,
                        "message", "Controlled Crash-Out accepted. The application will shut down."
                )
        );
    }
}
