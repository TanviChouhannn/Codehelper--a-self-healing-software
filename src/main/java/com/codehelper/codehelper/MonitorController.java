
package com.codehelper;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class MonitorController {

    @GetMapping("/api/monitor/status")
    public Map<String, String> getStatus() {
        return Map.of(
            "status", "Running",
            "server", "Online",
            "project", "CodeHelper"
        );
    }
}
